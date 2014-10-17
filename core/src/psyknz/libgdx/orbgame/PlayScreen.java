package psyknz.libgdx.orbgame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Body;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import psyknz.libgdx.architecture.*;

public class PlayScreen extends GameScreen {
	
	public static final float ORB_DIAMETER = 52;	// Size of the orbs in the game.
	public static final int POINTS_PER_ORB = 10;	// Number of points earned per orb.
	
	private float spawnDistance;	// Distance from the magnet new orbs should be spawned at.
	public float spawnRate = 1.0f;	// How frequently new orbs should spawn in seconds.
	private float spawnTimer = 0;	// Tracks time in seconds until a new orb should spawn.
	
	private float orbAcceleration; // Variables to spawn, place, and move the orbs.
	
	// Array containing all the potential colors an orb can be.
	private Color[] orbColors = {Color.BLUE, Color.PURPLE, Color.RED, Color.MAGENTA, Color.CYAN};
	private Array<Color> colorList = new Array<Color>(); // Array which contains the above colours in random order.
	
	private int gameOverStage = 0;		// Flag used to determine if the game is over.
	
	private World world; 			// Reference to the box2d simulation world.
	private BodyDef orbBodyDef; 	// Default body definition for new orbs.
	private FixtureDef orbFixDef;	// Fixture definitons for orbs and their sensors.
	private CircleShape orbShape; 	// Shape information for orbs and their sensors.
	private Body border, magnet;	// References to the border and magnet object which are always present.
	
	private Array<Body> orbs = new Array<Body>(); // Creates a blank Array to store references to bodies in the Box2D simulation.
	private PlayerController player;

	private OrbElement orbDataA; // Member variable used to temporarily access orb data.
	private Body orbToAdd = null; // Flag to indicate an orb which should be added to the selection when possible.
	
	private UIElement ui;					//
	private OrthographicCamera uiCamera;	//
	
	public PlayScreen(GameCore game) {
		super(game);
		viewSize = 480; // Sets the length of the shortest screen edge in game units.
		
		world = new World(new Vector2(0, 0), true);					// Creates the Box2D World space.
		world.setContactListener(new OrbCollisionProcessor(this)); 	// Creates a new collision processor to listen to box2d contcts.
		
		// Sets the distance new orbs should spawn from the magnet to the distance from the magnet to the furtherest corner of the screen plus the radius of an orb.
		spawnDistance = (float) Math.sqrt(Math.pow(viewSize / 2, 2) + Math.pow(viewSize / 2, 2)) + ORB_DIAMETER;
		orbAcceleration = spawnDistance * viewSize; // Sets the base acceleration rate for orbs.
		
		orbShape = new CircleShape(); 			// Creates the circle shape used to define orbs.
		orbShape.setRadius(ORB_DIAMETER / 2); 	// Sets the radius of all circles based on the pre-defined ORB_DIAMETER.
		
		orbBodyDef = new BodyDef(); 					// Creates the definition for orbs.
		orbBodyDef.type = BodyDef.BodyType.DynamicBody; // Sets orbs type to dynamic so that it is affected by forces.
		
		orbFixDef = new FixtureDef(); 	// Creates a new fixture definiton for orbs.
		orbFixDef.shape = orbShape; 	// Sets the fixtures shape to the predefined circle.
		orbFixDef.friction = 0.0f; 		// Sets the orbs friction.
		orbFixDef.density = 0.1f; 		// Sets the orbs density.
		orbFixDef.restitution = 0.0f; 	// Sets the orbs restitution.
		
		ui = new UIElement(this, input);
		uiCamera = new OrthographicCamera();
		
		player = new PlayerController(world, ui, this);
		input.addProcessor(player);
	}
	
	@Override
	public void show() {
		super.show();		
		generateNewGame();	// When the screen is first switched to it generates a new game.
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		camera.position.x = magnet.getPosition().x;	// Sets the x position of the camera to above the magnet.
		camera.position.y = magnet.getPosition().y;	// Sets the y position of the camera to above the magnet.
		camera.update();							// Updates the camera to put the changes into effect.
		
		uiCamera.viewportWidth = width;
		uiCamera.viewportHeight = height;
		uiCamera.position.set(width / 2, height / 2, 0);
		uiCamera.update();
		ui.setViewport(uiCamera);
		
		player.setCamera(camera);
	}
	
	@Override
	public void resume() {
		super.resume();
		ui.displayMessage("Tap Screen to Resume"); // Waits for user input before resuming a game.
	}
	
	@Override
	public void update(float delta) {		
		if(gameOverStage > 0) {
			player.scoreSelectedOrbs();
			ui.addAllOutstandingPoints();
			ui.addScoreToHighscores();
			ui.saveHighscores();
			gameOverStage = 2;
		}
		
		if(ui.update(delta)) return;
		
		if(gameOverStage > 1) {
			for(Body orb : orbs) world.destroyBody(orb);
			ui.resetScore();
			spawnRate = 1;
			gameOverStage = 0;
			generateNewGame();
		}
		
		spawnTimer -= delta;			// Counts down the spawn timer.
		while(spawnTimer <= 0) {		// As long as the spawnTimer is less tha 0,
			createOrb();				// a new orb is created,
			spawnTimer += spawnRate;	// and the timer is incremented by the spawn rate to the current spawn rate.
		}
		
		for (Body orb : orbs) {
			if(orb.getType() == BodyDef.BodyType.DynamicBody) {
				orbDataA = (OrbElement) orb.getUserData();
				orbDataA.attractTo(magnet.getPosition(), orbAcceleration * delta);
			}
		}
		
		world.step(1/60f, 6, 2); // Steps through the Box2D simulation.
		
		player.update();
		
		if(orbToAdd != null) {					// If an orb is flagged to be added to the selection,
			player.addOrbToSelection(orbToAdd); // its added,
			orbToAdd = null;					// and the flag is set back to null.
		}
		
		for(Body orb: orbs) {							// For every orb in the simulation,
			orbDataA = (OrbElement) orb.getUserData();	// it's user data is acessed.
			orbDataA.update(delta);						// and the position of its bounding box is updated.
		}
	}
	
	@Override
	public void draw(SpriteBatch batch, Rectangle view) {
		orbDataA = (OrbElement) border.getUserData();	// Draws the border to the screen.
		orbDataA.getSprite().draw(batch);		
		orbDataA = (OrbElement) magnet.getUserData();	// Draws the magnet to the screen.
		orbDataA.getSprite().draw(batch);
		
		for(Body orb: orbs) if(orb.getType() == BodyDef.BodyType.DynamicBody) {	// Every dynamic orb on the screen,
			orbDataA = (OrbElement) orb.getUserData();							// has its user data accessed,
			orbDataA.getSprite().draw(batch);									// and is drawn to the screen.
		}
		
		player.draw(batch);
		
		for(Body orb: orbs) {													// Every orb on the screen,
			orbDataA = (OrbElement) orb.getUserData();							// has its user data accessed,
			if(orbDataA.getPulse() != null) orbDataA.getPulse().draw(batch);	// and if it has a pulse its pulse is drawn.
		}
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		
		batch.setProjectionMatrix(uiCamera.combined);
		batch.begin();
		ui.draw(batch);
		batch.end();
	}
	
	// Creates a new orb at a random location on the screen.
	public Body createOrb() {
		float angleSpawn = MathUtils.random(360.0f); // Randomly selects where to spawn the orb relative to the magnet.
		
		return createOrb(magnet.getPosition().x + MathUtils.sinDeg(angleSpawn) * spawnDistance, // Creates a new orb at a position based
				magnet.getPosition().y + MathUtils.cosDeg(angleSpawn) * spawnDistance);			// on the randomly generated angle.
	}
	
	// Creates a new orb at the given position.
	public Body createOrb(float x, float y) {
		orbBodyDef.position.set(x, y); // Sets the x, y co-ordinates for the new orb.
		
		Body orb = world.createBody(orbBodyDef);	// Creates a new orb in the physics world.
		orb.createFixture(orbFixDef); 				// Generates the fixture representing the physical orb.
		orb.setLinearVelocity(0 - x / 2, 0 - y / 2);
		
		Sprite orbSprite = new Sprite(game.assets.get("white_circle.png", Texture.class));			// Creates a sprite for the orb.
		orbSprite.setSize(ORB_DIAMETER, ORB_DIAMETER);	// Sets the size of the sprite as the default orb size.
		orbSprite.setColor(getRandomColor());			// Sets the color of the sprite to a randomly selected color.
		orb.setUserData(new OrbElement(orb, orbSprite,	// Generates the orbs non-physics related data.
				OrbElement.State.FREE));	
		
		world.getBodies(orbs); // Refreshes the list of box2d elements.
		
		return orb; // Returns a reference to the orb which has just been created.
	}
	
	public void selectOrb(Body orb) {
		orbToAdd = orb;
	}
	
	@Override
	public void dispose() {
		orbShape.dispose(); 					// Disposes of the orbs circle information.
		world.dispose();						// Disposes of the box2d simulation object.
		super.dispose();
	}
	
	private void placeRingOfOrbs(int num, float offset) {
		float distance = ORB_DIAMETER * num / 6;
		for(int i = 0; i < num; i++) {
			createOrb(magnet.getPosition().x + MathUtils.sinDeg(i * 360 / num + offset) * distance, 
					magnet.getPosition().y + MathUtils.cosDeg(i * 360 / num + offset) * distance);
		}
	}
	
	// Returns a random color from the list of predefined colors using a shuffled list without replacement.
	public Color getRandomColor() {
		if(colorList.size <= 1) {							// If the list of colors to choose from is empty a new list is generated.
			for(int num = 0; num < 3; num++) {				// The list is filled with three of each color,
				for(int i = 0; i < orbColors.length; i++) {	// using colors available in the current theme.
					colorList.add(orbColors[i]);			// Colors are added sequentially.
				}
			}
			colorList.shuffle(); // Shuffles the newly generated list to allow for randomisation.
		}
		return colorList.pop(); // Returns the color at the end of the list.
	}
	
	public void gameOver(Body orb) {
		ui.displayMessage("Game Over");
		gameOverStage = 1;
	}
	
	public void generateNewGame() {
		BodyDef magnetDef = new BodyDef(); 				// Creates a new body definition for the magnet,
		magnetDef.type = BodyDef.BodyType.StaticBody;	// as static,
		magnetDef.position.set(0, 0); 					// and placed at the centre of the game world (0, 0).
		
		magnet = world.createBody(magnetDef);	// Creates a new magnet using the above Body Definition.
		magnet.createFixture(orbShape, 0f);		// Provides the magnet with a single circular fixture the size of an orb.
		
		Sprite magnetSpr = new Sprite(game.assets.get("white_circle.png", Texture.class));	// Creates the magnets sprite.
		magnetSpr.setSize(ORB_DIAMETER, ORB_DIAMETER);										// Sets it to the size of an orb,
		magnetSpr.setColor(Color.GRAY);														// and makes it gray.
		
		Sprite pulse = new Sprite(game.assets.get("white_torus.png", Texture.class)); 	// Creates a pulse sprite for the magnet.
		pulse.setSize(ORB_DIAMETER, ORB_DIAMETER);										// Sets it to the size of an orb,
		magnetSpr.setColor(Color.GRAY);													// and makes it gray.
		
		OrbElement magnetData = new OrbElement(magnet, magnetSpr, OrbElement.State.MAGNET); // Generates user data for the magnet.
		magnetData.setPulse(new PulseElement(pulse, 3.5f, 2, 0.8f, 0));						// Adds the new pulse to the magnet.
		magnet.setUserData(magnetData);														// Sets the data to the body.
		
		float borderSize = viewSize - ORB_DIAMETER;		// Determines the size of the border.
		CircleShape borderShape = new CircleShape();	// Creates a new circle,
		borderShape.setRadius(borderSize / 2);			// and sets its size to the size of the border.
		
		FixtureDef borderFixDef = new FixtureDef();	// Creates a new fixture definiton for the border.
		borderFixDef.shape = borderShape;			// Sets the fixtures shape to the new circle,
		borderFixDef.isSensor = true;				// and makes it a sensor so that it has no direct effect on the simulation.
		
		border = world.createBody(magnetDef);	// Creates a new border object using the magnets definition,
		border.createFixture(borderFixDef);		// and generates the borders fixture.
		
		Sprite borderSpr = new Sprite(game.assets.get("white_circle.png", Texture.class));	// Creates a sprite for the border.
		borderSpr.setSize(borderSize, borderSize);											// Sets its size to what was previously calculated,
		borderSpr.setColor(Color.MAROON);													// and makes it MAROON.
		border.setUserData(new OrbElement(border, borderSpr, OrbElement.State.BORDER));		// User data is generated for the border.
		borderShape.dispose();																// The circle created to generate the border is disposed of.	
		
		placeRingOfOrbs(6, 30);									// Places a ring of six orbs,
		placeRingOfOrbs(12, 0);									// and a ring of 12 orbs,
		for(int i = 0; i < 30; i++) world.step(1/60f, 6, 2);	// then simulates the world for half a second to put everything in place.
		
		for(Body orb: orbs) {							// Every orb in the simulation,
			orbDataA = (OrbElement) orb.getUserData();	// has its user data accessed,
			orbDataA.update(0);							// and is updated to sync its sprite with its physics body.
		}
		
		ui.displayMessage("Play Game"); // Waits for user input before starting a game.
	}
	
	public Array<Body> getOrbs() {
		return orbs;
	}

}
