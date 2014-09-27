package psyknz.libgdx.orbgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Body;

import psyknz.libgdx.architecture.*;

public class PlayScreen extends GameScreen {
	
	public static final float ORB_DIAMETER = 32; // Size of the orbs in the game.
	
	private float spawnDistance, spawnTimer, orbAcceleration; // Variables to spawn, place, and move the orbs.
	private Color[] orbColors = {Color.BLUE, Color.GREEN, Color.RED, Color.MAGENTA}; // Array of colours to use when colouring orb sprites.
	private Vector2 magnetPos; // The position the magnet should be placed.
	private float spawnRate = 1.0f; // What the spawnTimer is set to when it resets in seconds.
	private Texture orbTex, pulseTex; // Texture used to draw orbs and any pulses coming off of them.
	private PulseElement magnetPulse; // Pulse eminating off of the magnet.
	private TouchTracker touchTracker;
	
	private World world; 						// Reference to the box2d simulation world.
	private BodyDef orbBodyDef; 				// Default body definition for new orbs.
	private FixtureDef orbFixDef, sensorFixDef; // Fixture definitons for orbs and their sensors.
	private CircleShape orbShape, sensorShape; 	// Shape information for orbs and their sensors.
	
	private Array<Body> orbs = new Array<Body>(); // Creates a black Array to store references to bodies in the Box2D simulation.
	
	private BitmapFont debugFont = new BitmapFont(); // Font used to draw debug info to the screen.
	private float fps = 0;
	private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer(); // Creates the debug renderer for Box2D entities.
	private boolean debugRenderOn = true; // Flag to determine if the Box2D debugRenderer should run.
	
	private Vector3 touchCoords = new Vector3(); // Vector3 used for processing and converting user touch co-ordinates.
	private OrbElement orbDataA; // Member variable used to temporarily access orb data.
	private Array<Body> selectedOrbs = new Array<Body>(); // Array to store orbs currently selected by the player.
	private Array<Vector2> selectedCoords = new Array<Vector2>();
	
	public PlayScreen(GameCore game) {
		super(game);
		
		input.addProcessor(this); 						// Adds the PlayScreen as an input processor for the game.
		touchTracker = new TouchTracker(ORB_DIAMETER);	// Creates a TouchTracker to movement of the players finger across the screen.
		
		world = new World(new Vector2(0, 0), true);	// Creates the Box2D World space.
		world.setContactListener(new OrbCollisionProcessor()); // Sets the PlayScreen as the contact listener for the box2d simulation.
		
		// Sets the distance new orbs should spawn from the magnet to the distance from the magnet to the furtherest corner of the screen plus the radius of an orb.
		spawnDistance = (float) Math.sqrt(Math.pow(Gdx.graphics.getWidth() / 2, 2) + Math.pow(Gdx.graphics.getHeight() / 2, 2)) + ORB_DIAMETER;
		orbAcceleration = spawnDistance * 500; // Sets the base acceleration rate for orbs.
		magnetPos = new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2); // Sets the position for the magnet as the middle of the screen.
		orbTex = new Texture(Gdx.files.internal("white_circle.png")); // Loads the texture to use for drawing orbs.
		pulseTex = new Texture(Gdx.files.internal("white_torus.png")); // Loads the texture for drawing pulses.
		
		BodyDef magnetDef = new BodyDef(); // Creates a new definition for the magnet.
		magnetDef.type = BodyDef.BodyType.StaticBody; // Sets the magnet to static.
		magnetDef.position.set(magnetPos); // Places the magnet in its spot.
		
		orbShape = new CircleShape(); 					// Creates the circle shape used to define orbs.
		orbShape.setRadius(ORB_DIAMETER / 2); 			// Sets the radius of all circles based on the pre-defined ORB_DIAMETER.
		sensorShape = new CircleShape();				// Creates the circle shape used to define orb sensors.
		sensorShape.setRadius(ORB_DIAMETER * 5 / 8);	// Sets the radius of sensor circles to 5/4 the size of the orb.
		
		Body magnet = world.createBody(magnetDef); // Creates the magnet body.
		magnet.createFixture(orbShape, 0f); // Sets the fixture for the magnet to a circle.
		Sprite magnetSprite = new Sprite(orbTex);	// Creates the sprite to use for drawing the magnet.
		magnetSprite.setSize(orbShape.getRadius() * 2, orbShape.getRadius() * 2);	// Sets the sprite to the size of the orbs.
		magnetSprite.setColor(Color.GRAY);	// Sets the color of the magnet to gray.	
		magnet.setUserData(new OrbElement(magnet, magnetSprite)); // Creates Orb Data for the magnet.
		
		Sprite pulse = new Sprite(pulseTex);						// Creates the sprite to use for drawing the magnet pulse.
		pulse.setSize(ORB_DIAMETER, ORB_DIAMETER);					// Sets the size of the sprite to the size of the orbs.
		pulse.setColor(Color.WHITE);								// Sets the color of the pulse to white.
		magnetPulse = new PulseElement(pulse, 3, 2, 0.8f, 0.3f);	// Creates the pulse element so that it doubles in size as it pulses, pulses twice a second, and goes from an alpha of 0.8 to 0.3 as it pulses.
		magnetPulse.setCenterPos(magnetPos);						// Centers the pulse over the magnet.
		
		orbBodyDef = new BodyDef(); 					// Creates the definition for orbs.
		orbBodyDef.type = BodyDef.BodyType.DynamicBody; // Sets orbs type to dynamic so that it is affected by forces.
		
		orbFixDef = new FixtureDef(); 	// Creates a new fixture definiton for orbs.
		orbFixDef.shape = orbShape; 	// Sets the fixtures shape to the predefined circle.
		orbFixDef.friction = 0.0f; 		// Sets the orbs friction.
		orbFixDef.density = 0.1f; 		// Sets the orbs density.
		orbFixDef.restitution = 0.0f; 	// Sets the orbs restitution.
		
		sensorFixDef = new FixtureDef(); 	// Creates a new fixture definition for orbs.
		sensorFixDef.shape = sensorShape;	// Sets the sensors shape to the predefined circle.
		sensorFixDef.isSensor = true;		// Sets the fixture to a sensor.
		
		spawnTimer = spawnRate; // Starts the spawnTimer.
		world.getBodies(orbs); 	// Updates the list of Box2D entities.
	}
	
	@Override
	public void update(float delta) {		
		spawnTimer -= delta;			// Counts down the spawn timer.
		if(spawnTimer <= 0) {			// If it reaches 0,
			createOrb();				// then a new orb is created,
			spawnTimer += spawnRate;	// and the timer is reset to the current spawn rate.
		}
		
		touchTracker.interpolateCoords(selectedCoords, selectedOrbs.size, ORB_DIAMETER);
		for(int i = 0; i < selectedOrbs.size; i++) {
			selectedOrbs.get(i).setTransform(selectedCoords.get(i), selectedOrbs.get(i).getAngle());
		}
		
		for (Body orb : orbs) {
			if(orb.getType() == BodyDef.BodyType.DynamicBody) {
				orbDataA = (OrbElement) orb.getUserData();
				orbDataA.attractTo(magnetPos, orbAcceleration * delta);
				if(orbDataA.nearbyPlayerOrbs.size > 0) {
					if(orbDataA.nearbyDynamicOrbs <= 0) {
						addOrbToSelection(orb);
					}
					else for(Body playerOrb : orbDataA.nearbyPlayerOrbs) {
						orbDataA.attractTo(playerOrb.getPosition(), orbAcceleration * delta * 2);
					}
				}
			}
		}
		
		world.step(1/60f, 6, 2); // Steps through the Box2D simulation.
		
		for(Body orb: orbs) {							// For every orb in the simulation,
			orbDataA = (OrbElement) orb.getUserData();	// it's user data is acessed.
			orbDataA.updateBounds();					// and the position of its bounding box is updated.
		}
		
		magnetPulse.update(delta); // Updates the pulse data.
		
		if(debugRenderOn) fps = 1 / delta; // Roughly works out how many frames per second are being rendered in debug mode.
	}
	
	@Override
	public void draw(SpriteBatch batch, Rectangle view) {
		for(Body orb: orbs) {
			orbDataA = (OrbElement) orb.getUserData();
			orbDataA.getSprite().draw(batch);
		}
		magnetPulse.draw(batch);
		
		if(debugRenderOn) debugFont.draw(batch, "FPS: " + fps, 0, Gdx.graphics.getHeight()); // Draws an FPS counter in the top left.
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);		
		if(debugRenderOn) debugRenderer.render(world, camera.combined); // Renders all Box2D entities with the debugRenderer if on.
	}
	
	// Creates a new orb at a random location on the screen.
	public void createOrb() {
		int colorIndex = MathUtils.random(3); 			// Randomly selects the color the orb will be set to.
		float angleSpawn = MathUtils.random(360.0f);	// Randomly selects wheree to spawn the orb relative to the magnet.
		
		orbBodyDef.position.set(magnetPos.x + MathUtils.sinDeg(angleSpawn) * spawnDistance, // Sets the x, y co-ordinates for the new orb.
				magnetPos.y + MathUtils.cosDeg(angleSpawn) * spawnDistance);
		
		Body orb = world.createBody(orbBodyDef); 	// Creates a new orb in the physics world.
		orb.createFixture(orbFixDef); 				// Generates the fixture representing the physical orb
		orb.createFixture(sensorFixDef);			// Generates the fixture which senses if the orb is next to other orbs.
		
		Sprite orbSprite = new Sprite(orbTex);
		orbSprite.setSize(orb.getFixtureList().first().getShape().getRadius() * 2, orb.getFixtureList().first().getShape().getRadius() * 2);
		orbSprite.setColor(orbColors[colorIndex]);
		orb.setUserData(new OrbElement(orb, orbSprite)); // Generates the orbs non-physics related data.
		
		world.getBodies(orbs); // Refreshes the list of box2d elements.
	}
	
	// Adds the given orb to the list of currently selected orbs.
	public void addOrbToSelection(Body orb) {
		selectedOrbs.add(orb); 														// Adds the given orb to the selection.
		touchTracker.setMaxLength(selectedOrbs.size * ORB_DIAMETER + ORB_DIAMETER); // Increases the length of the TouchTracker to accomodate another orb.
		orb.setType(BodyDef.BodyType.StaticBody);									// Sets the selected orb to static so that it isn't effected by forces.
		orb.destroyFixture(orb.getFixtureList().get(1));							// Removes the orbs defaut sensor for nearby orbs.
		orb.getFixtureList().first().setSensor(true);								// Sets the selected orb as a sensor so that it can drag over orbs.
	}
	
	// Scores all currently selected orbs and removes them from the game.
	public void scoreSelectedOrbs() {
		for(Body orb : selectedOrbs) world.destroyBody(orb);	// Every currently selected orb is removed from the box2d simulation.
		selectedOrbs.clear();									// The list of selected orbs is cleared.
		touchTracker.setMaxLength(ORB_DIAMETER);				// Resets the length of the touchTracker to the diameter of an orb.
		world.getBodies(orbs); 									// Refreshes the list of box2d elements in the simulation.
	}
	
	// Destroys the specified orb.
	public void RemoveOrb2D(Body orb) {
		world.destroyBody(orb); // Removes the orb from the box2d simulation.
		world.getBodies(orbs); 	// Refreshes the list of box2d elements.
	}
	
	@Override
	public void dispose() {
		debugRenderer.dispose(); 	// Disposes of the debugRenderer.
		orbShape.dispose(); 		// Disposes of the orbs circle information.
		sensorShape.dispose();		// Disposes of the sensors circle information.
		orbTex.dispose(); 			// Disposes of the texture used to draw the orbs.
		super.dispose();
	}
	
	/** Processes the player touching down on the screen. If the player touches on top of an orb it becomes selected so that it can
	 *  follow the users finger across the screen. 
	 * @see com.badlogic.gdx.InputAdapter#touchDown(int, int, int, int) */
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		touchCoords.set(screenX, screenY, 0); 	// Sets the current co-ordinates for the users touch input.
		camera.unproject(touchCoords); 			// Transforms the touch co-ordinates from screen space to world space.
		
		for(Body orb : orbs) { 														// Every orb is checked,
			if(orb.getType() == BodyDef.BodyType.DynamicBody) { 					// to see if it has a Dynamic body.
				orbDataA = (OrbElement) orb.getUserData(); 							// If it does, it's OrbElement is accessed,
				if(orbDataA.getBounds().contains(touchCoords.x, touchCoords.y)) {	// and its bounding box is tested against the touch co-ordinates.
					addOrbToSelection(orb);											// If the player touched inside the bounding box, the orb becomes selected.
					touchTracker.addTouch(touchCoords.x, touchCoords.y);			// Adds the current touch-coordinate to the TouchTracker.
					return true;
				}
			}
		}
		return false;
	}
	
	/** Processes the player dragging their finger across the screen. Provided the player currently has some orbs selected the movement
	 *  is recorded by the touchTracker object.
	 * @see com.badlogic.gdx.InputAdapter#touchDragged(int, int, int) */
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(selectedOrbs.size > 0) {									// Only processed if the player currently has orbs selected.
			touchCoords.set(screenX, screenY, 0); 					// Stores the co-ordinates where the player has touched the screen.
			camera.unproject(touchCoords); 							// Transforms the touch co-ordinates from screen space to world space.
			touchTracker.addTouch(touchCoords.x, touchCoords.y);	// Adds the current touch-coordinate to the TouchTracker.
			return true;
		}
		return false;
	}
	
	/** Processes the player lifting their finger off of the screen. If the player has orbs selected they are scored for the player.
	 * @see com.badlogic.gdx.InputAdapter#touchUp(int, int, int, int) */
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(selectedOrbs.size > 0) {	// If there are currently some orbs selected,
			scoreSelectedOrbs();	// each selected orb is removed from the box2d simulation.
			return true;
		}
		return false;
	}

}
