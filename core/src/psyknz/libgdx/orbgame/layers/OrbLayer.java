package psyknz.libgdx.orbgame.layers;

import psyknz.libgdx.orbgame.misc.GamePalette;
import psyknz.libgdx.orbgame.play.*;
import psyknz.libgdx.orbgame.screens.PlayScreen2D;
import psyknz.libgdx.orbgame.tweenaccessors.SpriteTween;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Joint;

import aurelienribon.tweenengine.*;
import aurelienribon.tweenengine.equations.Elastic;

public class OrbLayer implements GameLayer {
	
	public static final float ORB_DIAMETER = 5;									// Size of the orbs in the game.
	public static final float END_IMPULSE = PlayScreen2D.PLAY_AREA_SIZE / 2;	// Force applied to orbs when the game ends.
	public static final float BASE_SPAWN_RATE = 1.0f;							// Starting rate at which new orbs should spawn.
	public static final int POINTS_PER_ORB = 10;								// Points received per orb scored.
	public static final float DELAY_BETWEEN_PLACEMENTS = 0.5f;					// Time in seconds for a new game to start.
	public static final float PLACEMENT_TWEEN_TIME = 2.0f;						// Time taken to place a ring of orbs.
	
	private AssetManager assets;		// Reference to the asset manager containing assets for orbs.
	public final TweenManager manager;	// Reference to the tween manager used to process orb layer tweens.
	private GamePalette palette;		// Reference to the palette used to colour the game.
	public final PlayController player;	// Reference to the play controller which allows for interaction with the play field.
	private AIPlayer ai;				// Reference to the AI playing the game, if any.
	
	public final World world;			// Reference to the box2d simulation world.
	private BodyDef orbBodyDef; 		// Default body definition for new orbs.
	private FixtureDef orbFixDef;		// Fixture definitons for orbs and their sensors.
	private CircleShape orbShape; 		// Shape information for orbs and their sensors.
	private Sprite orbSprite;			// Reference to the base sprite used to draw all other orbs.
	private OrbData border, magnet;		// References to the border and magnet object which are always present.
	public final Array<OrbData> orbs;	// Array containing references to data for all orbs active in the simulation.
	
	private Camera camera;			// Reference to the camera used to draw this layer.
	private float spawnDistance;	// Size of the visible play field and the distance at orbs spawn away from the magnet.
	private Tween spawnTimer;		// Tween used to control the orbs spawning.
	private float spawnRate;		//
	
	/**
	 * Creates a new layer for processing orb interactions.
	 * @param assets The asset manager which contains the textures needed to render the game field.
	 * @param palette The color palette which should be used to colour to game field.
	 */
	public OrbLayer(AssetManager assets, GamePalette palette) {
		this.assets = assets;	// Stores a reference to the asset manager holding resources for the orbs.
		this.palette = palette;	// Stores a reference to the palette used to generate the games elements.
		
		manager = new TweenManager();	// Stores a reference to the tween manager used to process this layers tweens.
		
		world = new World(new Vector2(0, 0), true);	// Creates the Box2D World space.
		new OrbCollisionProcessor(this);		 	// Creates a new collision processor to listen to box2d contcts.
		
		orbs = new Array<OrbData>();	// Initialises the array containing all orbs in the game.
		
		player = new PlayController(this);	// Creates a new player controller to allow interaction with this layer.
		
		orbBodyDef = new BodyDef(); 					// Creates the definition for orbs.
		orbBodyDef.type = BodyDef.BodyType.DynamicBody; // Sets orbs type to dynamic so that it is affected by forces.
		orbShape = new CircleShape(); 					// Creates the circle shape used to define orbs.
		orbShape.setRadius(ORB_DIAMETER / 2); 			// Sets the radius of all circles to ORB_DIAMETER.
		orbFixDef = new FixtureDef(); 					// Creates a new fixture definiton for orbs.
		orbFixDef.shape = orbShape; 					// Sets the fixtures shape to the predefined circle.
		orbFixDef.friction = 0.0f; 						// Sets the orbs friction.
		orbFixDef.density = 0.0f; 						// Sets the orbs density.
		
		orbSprite = new Sprite(assets.get("white_circle.png", Texture.class));	// Creates a sprite for the orb.
		orbSprite.setSize(ORB_DIAMETER, ORB_DIAMETER);							// Sets the size of the sprite as the default orb size.
	}
	
	@Override
	public void resize(Camera camera) {
		this.camera = camera;																	// Records the camera used to draw play.
		if(camera.viewportWidth > camera.viewportHeight) spawnDistance = camera.viewportWidth;	// Sets the spawn distance for the orbs
		else spawnDistance = camera.viewportHeight;												// to the biggest of width or height.
		
		spawnDistance = (float) Math.sqrt(Math.pow(spawnDistance / 2, 2)	// Sets how far from the magnet orbs should spawn by finding
				+ Math.pow(spawnDistance / 2, 2)) + ORB_DIAMETER; 			// the distance to the furthest just-off-screen point.
		
		player.setCamera(camera);	// Sets the camera used to draw the play field.		
	}

	@Override
	public boolean update(float delta) {
		if(border == null) createBorder();	// If the border hasn't been instantiated, it is.
		if(magnet == null) createMagnet();	// If the magnet hasn't been instantiated, it is.
		
		manager.update(delta);		// Updates all tweens that are currently running.
		world.step(delta, 6, 2); 	// Steps through the Box2D simulation.
		
		player.update();	// Updates game logic for the play controller.
		
		for(OrbData o : orbs) o.update(delta);	// Game logic for all orbs is updated.
		
		if(orbs.size <= 0) newGame();	// TODO: remove this hack and replace with a better method.
		
		return false;	// Does not block processing of lower layers.
	}

	@Override
	public void draw(SpriteBatch batch) {
		border.getSprite().draw(batch);	// Draws the border to the screen.
		magnet.getSprite().draw(batch);	// Draws the magnet to the screen.
		
		for(OrbData o : orbs) o.getSprite().draw(batch);	// Draws all orbs to the screen.
		
		player.draw(batch); // draws all orbs being managed by the play controller.
	}
	
	@Override
	public void dispose() {
		orbShape.dispose();	// Disposes of the shape template for orbs
		world.dispose();	// as well as the world simulation.
	}
	
	/**
	 * Creates a new orb at a random location about the center of the play field.
	 * @return Reference to the orb that was created.
	 */
	public Body createOrb() {
		float angleSpawn = MathUtils.random(360.0f); // Randomly selects where to spawn the orb relative to the magnet.
		
		return createOrb(																		// Creates a new orb at a position based on
				magnet.body.getPosition().x + MathUtils.sinDeg(angleSpawn) * spawnDistance, 	// the the randomly generated angle and at a
				magnet.body.getPosition().y + MathUtils.cosDeg(angleSpawn) * spawnDistance);	// distance from the magnet that is off screen.
	}
		
	/**
	 * Creates a new orb at the given location.
	 * @param x coordinate for the newly created orb.
	 * @param y coordinate for the newly created orb.
	 * @return Reference to the orb that was created.
	 */
	public Body createOrb(float x, float y) {
		orbBodyDef.position.set(x, y); 							// Sets the x, y co-ordinates for the new orb.
		Body orb = world.createBody(orbBodyDef);				// Creates a new orb in the physics world.
		orb.createFixture(orbFixDef); 							// Generates the fixture representing the physical orb.
		orbs.add(new OrbData(this, orb, orbSprite, 				// Creates and adds new orb data to the list of orbs
				palette.getRandomColor(), OrbData.State.FREE));	// using a random new color and in a FREE motion state.
		
		return orb; // Returns a reference to the orb which has just been created.
	}
	
	/**
	 * Removes the given orb and its user data from the game.
	 * @param orb The orb to be removed from the simulation.
	 */
	public void removeOrb(OrbData orb) {
		world.destroyBody(orb.body);	// The physics body for the orb is removed from the box2d simulation.
		orbs.removeValue(orb, true);	// Afterwards, the data for the orb is removed as well.
	}
	
	/**
	 * Selects the given orb by removing it from the list of orbs and passing its information to the play controller.
	 * @param orb The orb that has been selected by the player.
	 */
	public void selectOrb(OrbData orb) {
		orbs.removeValue(orb, true);	// Removes the selected orb from the layers record of FREE orbs
		player.selectOrb(orb);			// and instead selects it.
	}
	
	// TODO: implement a score system.
	public void scoreOrbs(int num) {
		spawnRate *= 0.97f;
		//ui.addPoints((int) Math.pow(selectedOrbs.size, 2) * POINTS_PER_ORB);	// The selected orbs are scored,
	}
	
	/**
	 * Creates and starts a new spawn timer with the given repeat rate and delay.
	 * @param delay Delay before the first orb is created by this timer in seconds.
	 * @param rate Number of seconds which should pass between each orb spawning.
	 */
	public void setSpawnTimer() {
		if(spawnTimer != null) spawnTimer.kill();					// If the spawnTimer is currently running it is killed.
		spawnTimer = Tween.call(new TweenCallback() {				// Starts a new Tween Callback
			@Override												//
			public void onEvent(int type, BaseTween<?> source) {	//
				createOrb();										// to create orbs during play
				setSpawnTimer();									//
			}														// at the speed defined by the spawn rate, on repeat.
		}).delay(spawnRate).start(manager);							//
	}
	
	/**
	 * Creates the border in the middle of the play field which 'contains' all settled orbs.
	 */
	public void createBorder() {
		BodyDef borderDef = new BodyDef(); 				// Creates a new body definition for the magnet,
		borderDef.type = BodyDef.BodyType.StaticBody;	// sets it to static,
		borderDef.position.set(0, 0); 					// and places it at the centre of the game world (0, 0).
		
		float borderSize = PlayScreen2D.PLAY_AREA_SIZE - ORB_DIAMETER * 2;	// Determines the size of the border.
		CircleShape borderShape = new CircleShape();						// Creates a new circle,
		borderShape.setRadius(borderSize / 2);								// and sets its size to the size of the border.
		
		FixtureDef borderFixDef = new FixtureDef();	// Creates a new fixture definiton for the border.
		borderFixDef.shape = borderShape;			// Sets the fixtures shape to the new circle,
		borderFixDef.isSensor = true;				// and makes it a sensor so that it has no direct effect on the simulation.
				
		Body b = world.createBody(borderDef);	// Creates a new border object using the magnets definition,
		b.createFixture(borderFixDef);			// and generates the borders fixture.
				
		Sprite borderSpr = new Sprite(assets.get("white_circle.png", Texture.class));	// Creates a sprite for the border.
		borderSpr.setSize(borderSize, borderSize);										// Sets its size to what was previously calculated,
		border = new OrbData(this, b, borderSpr, Color.MAROON, OrbData.State.BORDER);	//
		borderShape.dispose();															// Disposes of the borderShape when finished.
	}
	
	/**
	 * Creates a new magnet in the centre of the play field which all other orbs will be drawn to.
	 */
	public void createMagnet() {
		BodyDef magnetDef = new BodyDef(); 				// Creates a new body definition for the magnet,
		magnetDef.type = BodyDef.BodyType.StaticBody;	// sets it to static,
		magnetDef.position.set(0, 0); 					// and places it at the centre of the game world (0, 0).
		
		Body m = world.createBody(magnetDef);	// Creates a new magnet using the above Body Definition.
		m.createFixture(orbShape, 0f);		// Provides the magnet with a single circular fixture of size 0,
				
		Sprite pulse = new Sprite(assets.get("white_torus.png", Texture.class)); 	// Creates a pulse sprite for the magnet.
		pulse.setSize(ORB_DIAMETER, ORB_DIAMETER);									// Sets it to the size of an orb,
				
		magnet = new OrbData(this, m, orbSprite, Color.GRAY, OrbData.State.MAGNET); 	// Generates user data for the magnet.
		magnet.inPlay = true;															// Defines the magnet as in play.
		// TODO: re-add a large pulse to the magnet.
	}
	
	/**
	 * Creates a new game by placing starting orbs and starting the spawn timer.
	 */
	public void newGame() {
		placeStartingOrbs(6, 30);	// Creates the inner ring of 6 orbs to enclose the magnet.
		
		Tween.call(new TweenCallback() {							// Uses a Tween Callback
			@Override												//
			public void onEvent(int type, BaseTween<?> source) {	//
				placeStartingOrbs(12, 0);							// to create the outer ring of 12 orbs, enclosing the inner 6
			}														//
		}).delay(DELAY_BETWEEN_PLACEMENTS).start(manager);			// after a delay of .5s
		
		spawnRate = BASE_SPAWN_RATE;	// Resets the spawn rate to the starting rate.
		
		Tween.call(new TweenCallback() {											// Uses a Tween Callback
			@Override																//
			public void onEvent(int type, BaseTween<?> source) {					//
				if(ai != null) ai.start();											// to start the ai once the game begins
				setSpawnTimer();													// and starts the spawn timer running.
			}																		//
		}).delay(DELAY_BETWEEN_PLACEMENTS + PLACEMENT_TWEEN_TIME).start(manager);	//
	}
	
	/**
	 * Ends the game by stopping orbs spawning, breaking all joints, and causing orbs to bounce off of screen.
	 */
	public void endGame() {
		spawnTimer.kill();	// Stops the spawn timer to prevent new orbs being created.
		spawnTimer = null;	// Then removes the reference to that timer.
		
		if(ai != null) ai.end();	// If an ai is interacting with this layer is is stopped.
		
		Array<Joint> joints = new Array<Joint>();		// Initialises an array to access all joints in the simulation.
		world.getJoints(joints);						// Stores all joints in the array
		for(Joint j : joints) world.destroyJoint(j);	// then proceeds to break them all.
		
		float angle;																		// Angle the orbs should bounce towards.
		Vector2 angularForce = new Vector2();												// Vector to represent the angle and force.
		Vector2 removePoint = new Vector2(0, camera.position.y - camera.viewportHeight);	// Vector representing off-screen.
		
		for(OrbData o : orbs) {										// Every orb in the simulation (other than the border and magnet)	
			angle = MathUtils.random(90.0f) - 45.0f;				// has a random angle assigned to it between 45 and 135 degrees,
			angularForce.set(MathUtils.sinDeg(angle) * END_IMPULSE, // with 0 pointing right, incrementing clockwise. That angle is used
					MathUtils.cosDeg(angle) * END_IMPULSE);			// to calculate a Vector representing the force which should be
			o.setState(OrbData.State.GAME_OVER, removePoint);		// applied to the orb when it bounces off screen. The orb is set to
			o.body.getFixtureList().first().setSensor(true);		// the game over state and passed a Vector whose y component informs
			o.body.setLinearVelocity(angularForce);					// it when to be removed. Set to a sensor to prevent collision.
		}
	}
	
	/**
	 * Enables use of the AI. Will not be starting until a new game is created.
	 * @param ai The ai object running on this layer.
	 */
	public void enableAI(AIPlayer ai) {
		this.ai = ai;
	}
	
	/**
	 * Disables use of the AI. Should be called before player input is allowed.
	 */
	public void disableAI() {
		if(ai != null) ai.end();	// Stops the AI from running provided there is one.
		this.ai = null;				// before clearning references to it.
	}
	
	/**
	 * Places a ring of orbs around the magnet at a distance which will make them touch but not overlap. Newly placed orbs have their
	 * sprite 'pop' into existance using a tween.
	 * @param num Number of orbs to place on screen.
	 * @param offset How far, in degrees, from 0 new orbs should start being placed around the magnet.
	 */
	private void placeStartingOrbs(int num, float offset) {
		float distance = ORB_DIAMETER * num / 6;														// The distance new orbs are placed
		OrbData orbData;																				// Temporary variable used to store orb data.
		for(int i = 0; i < num; i++) {																	// from the magnet is based on the
			orbData = (OrbData) createOrb(																// size and number of orbs. The number
					magnet.body.getPosition().x + MathUtils.sinDeg(i * 360 / num + offset) * distance, 	// of orbs specified are placed around
					magnet.body.getPosition().y + MathUtils.cosDeg(i * 360 / num + offset) * distance)	// the magnet, offset appropriately.
					.getUserData();																		// OrbData for the orbs is accessed
			orbData.inPlay = true;																		// so that they can be set as in play.
			
			Tween.to(orbData.getSprite(), SpriteTween.SIZE, PLACEMENT_TWEEN_TIME)				// The new orbs sprite is tweened to
					.target(orbData.getSprite().getWidth(), orbData.getSprite().getHeight())	// its normal size over 2s using
					.ease(Elastic.OUT).start(manager);											// elastic easing to make it 'pop'.
			orbData.getSprite().setSize(0, 0);													// The orbs size is shrunk to nothing.
		}
	}
	
	/**
	 * @return Reference to the magnet on this screen.
	 */
	public OrbData getMagnet() {
		return magnet;
	}
}
