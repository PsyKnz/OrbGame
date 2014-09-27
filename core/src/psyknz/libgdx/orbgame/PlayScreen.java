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
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.ContactImpulse;

import psyknz.libgdx.architecture.*;

public class PlayScreen extends GameScreen implements ContactListener {
	
	public static final float ORB_DIAMETER = 32; // Size of the orbs in the game.
	
	private float spawnDistance, spawnTimer, orbAcceleration; // Variables to spawn, place, and move the orbs.
	private Color[] orbColors = {Color.BLUE, Color.GREEN, Color.RED, Color.MAGENTA}; // Array of colours to use when colouring orb sprites.
	private Vector2 magnetPos; // The position the magnet should be placed.
	private float spawnRate = 1.0f; // What the spawnTimer is set to when it resets in seconds.
	private Texture orbTex, pulseTex; // Texture used to draw orbs and any pulses coming off of them.
	private PulseElement magnetPulse; // Pulse eminating off of the magnet.
	private TouchTracker touchTracker;
	
	private World world; // Box2D simulation object.
	private BodyDef orbDef; // Definitions for the orbs.
	private FixtureDef orbFixDef; // Fixture definiton for orbs.
	private CircleShape circle; // Shape information for game entities.
	
	private Array<Body> orbs = new Array<Body>(); // Creates a black Array to store references to bodies in the Box2D simulation.
	
	private BitmapFont debugFont = new BitmapFont(); // Font used to draw debug info to the screen.
	private float fps = 0;
	private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer(); // Creates the debug renderer for Box2D entities.
	private boolean debugRenderOn = false; // Flag to determine if the Box2D debugRenderer should run.
	
	private OrbElement orbDataA, orbDataB; // Member variable used to temporarily access orb data.
	private Array<Body> selectedOrbs = new Array<Body>(); // Array to store orbs currently selected by the player.
	private Array<PulseElement> orbPulses = new Array<PulseElement>();
	
	public PlayScreen(GameCore game) {
		super(game);
		
		input.addProcessor(this); // Adds the PlayScreen as an input processor.
		
		world = new World(new Vector2(0, 0), true);	// Creates the Box2D World space.
		world.setContactListener(this); // Sets the PlayScreen as the contact listener for the box2d simulation.
		
		circle = new CircleShape(); // Creates the circle shape used to define entities.
		circle.setRadius(16f); // Sets the radius of all circles to 32 units.
		
		touchTracker = new TouchTracker(circle.getRadius() * 2);
		
		// Sets the distance new orbs should spawn from the magnet to the distance from the magnet to the furtherest corner of the screen plus the radius of an orb.
		spawnDistance = (float) Math.sqrt(Math.pow(Gdx.graphics.getWidth() / 2, 2) + Math.pow(Gdx.graphics.getHeight() / 2, 2)) + circle.getRadius();
		orbAcceleration = spawnDistance * 500; // Sets the base acceleration rate for orbs.
		magnetPos = new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2); // Sets the position for the magnet as the middle of the screen.
		orbTex = new Texture(Gdx.files.internal("white_circle.png")); // Loads the texture to use for drawing orbs.
		pulseTex = new Texture(Gdx.files.internal("white_torus.png")); // Loads the texture for drawing pulses.
		
		BodyDef magnetDef = new BodyDef(); // Creates a new definition for the magnet.
		magnetDef.type = BodyDef.BodyType.StaticBody; // Sets the magnet to static.
		magnetDef.position.set(magnetPos); // Places the magnet in its spot.
		
		Body magnet = world.createBody(magnetDef); // Creates the magnet body.
		magnet.createFixture(circle, 0f); // Sets the fixture for the magnet to a circle.
		Sprite magnetSprite = new Sprite(orbTex);	// Creates the sprite to use for drawing the magnet.
		magnetSprite.setSize(circle.getRadius() * 2, circle.getRadius() * 2);	// Sets the sprite to the size of the orbs.
		magnetSprite.setColor(Color.GRAY);	// Sets the color of the magnet to gray.	
		magnet.setUserData(new OrbElement(magnet, magnetSprite)); // Creates Orb Data for the magnet.
		
		Sprite pulse = new Sprite(pulseTex);							// Creates the sprite to use for drawing the magnet pulse.
		pulse.setSize(circle.getRadius() * 2, circle.getRadius() * 2);	// Sets the size of the sprite to the size of the orbs.
		pulse.setColor(Color.WHITE);									// Sets the color of the pulse to white.
		magnetPulse = new PulseElement(pulse, 3, 2, 0.8f, 0.3f);		// Creates the pulse element so that it doubles in size as it pulses, pulses twice a second, and goes from an alpha of 0.8 to 0.3 as it pulses.
		magnetPulse.setCenterPos(magnetPos);							// Centers the pulse over the magnet.
		
		orbDef = new BodyDef(); // Creates a new definition for orbs.
		orbDef.type = BodyDef.BodyType.DynamicBody; // Sets orbs to dynamic.
		
		orbFixDef = new FixtureDef(); // Creates a new fixture definiton for orbs.
		orbFixDef.shape = circle; // Sets the fixtures shape to the predefined circle.
		orbFixDef.friction = 0.0f; // Sets the fixtures friction.
		orbFixDef.density = 0.1f; // Sets the fixtures density.
		orbFixDef.restitution = 0.0f; // Sets the fixtures restitution.
		
		spawnTimer = spawnRate; // Starts the spawnTimer.
		world.getBodies(orbs); // Updates the list of Box2D entities.
	}
	
	@Override
	public void update(float delta) {		
		spawnTimer -= delta;			// Counts down the spawn timer.
		if(spawnTimer <= 0) {			// If it reaches 0,
			CreateOrb2D();				// then a new orb is created,
			spawnTimer += spawnRate;	// and the timer is resent to the current spawn rate.
		}
		
		for(int i = 0; i < selectedOrbs.size; i++) {
			selectedOrbs.get(i).setTransform(touchTracker.interpolateCoord(i * ORB_DIAMETER), selectedOrbs.get(i).getAngle());
		}
		
		for (Body orb : orbs) {
			if(orb.getType() == BodyDef.BodyType.DynamicBody) {
				orbDataA = (OrbElement) orb.getUserData();
				orbDataA.attractTo(magnetPos, orbAcceleration * delta);
				
				for(Body selectedOrb : selectedOrbs) {
					orbDataB = (OrbElement) selectedOrb.getUserData();
					if(orbDataA.getBounds().overlaps(orbDataB.getBounds()) && orbDataA.getSprite().getColor().equals(orbDataB.getSprite().getColor())) {
						orbDataA.attractTo(selectedOrb.getPosition(), orbAcceleration * delta * 2);
					}
				}
			}
		}
		
		world.step(1/60f, 6, 2); // Steps through the Box2D simulation.
		
		for(Body orb: orbs) {							// For every orb in the simulation,
			orbDataA = (OrbElement) orb.getUserData();	// it's user data is acessed.
			orbDataA.updateBounds();						// and the position of its bounding box is updated.
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
	public void CreateOrb2D() {
		float angleSpawn = MathUtils.random(360.0f); // Selects a random angle to spawn the new orb from (relative to the magnet).
		int colorIndex = MathUtils.random(3); // Randomly selects the color the orb will be set to.
		
		// Sets the x, y position the new orb should spawn based on the randomly determined angle to spawn at.
		float xSpawn = MathUtils.sinDeg(angleSpawn) * spawnDistance; 
		float ySpawn = MathUtils.cosDeg(angleSpawn) * spawnDistance;
		orbDef.position.set(magnetPos.x + xSpawn, magnetPos.y + ySpawn);
		
		Body orb = world.createBody(orbDef); // Creates a new orb in the physics world.
		Fixture orbFix = orb.createFixture(orbFixDef); // Generates the orbs fixture.
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
		orb.setType(BodyDef.BodyType.StaticBody); 									// Sets the selected orb to static so that it isn't effected by forces.
		orb.getFixtureList().first().setSensor(true); 								// Sets the selected orb as a sensor so that it can drag over orbs.
	}
	
	// Scores all currently selected orbs and removes them from the game.
	public void scoreSelectedOrbs() {
		for(Body orb : selectedOrbs) world.destroyBody(orb);	// Every currently selected orb is removed from the box2d simulation.
		selectedOrbs.clear();									// The list of selected orbs is cleared.
		touchTracker.setMaxLength(circle.getRadius() * 2);		// Resets the length of the touchTracker to the diameter of an orb.
		world.getBodies(orbs); 									// Refreshes the list of box2d elements in the simulation.
	}
	
	// Destroys the specified orb.
	public void RemoveOrb2D(Body orb) {
		world.destroyBody(orb); // Removes the orb from the box2d simulation.
		world.getBodies(orbs); // Refreshes the list of box2d elements.
	}
	
	@Override
	public void dispose() {
		debugRenderer.dispose(); // Disposes of the debugRenderer.
		circle.dispose(); // Disposes of the circle information.
		orbTex.dispose(); // Disposes of the texture used to draw the orbs.
		super.dispose();
	}
	
	// Processes the player touching down on the screen.
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector3 touchCoords = new Vector3(screenX, screenY, 0); // Creates a vector to store the touch co-ordinates.
		camera.unproject(touchCoords); // Transforms the vector from screen co-ordinates to world co-ordinates.
		
		for(Body orb : orbs) { 													// Every orb is checked,
			if(orb.getType() == BodyDef.BodyType.DynamicBody) { 				// to see if it has a Dynamic body.
				orbDataA = (OrbElement) orb.getUserData(); 						// If it does, it's OrbElement is accessed,
				if(orbDataA.getBounds().contains(touchCoords.x, touchCoords.y)) {// and its bounding box is tested against the touch co-ordinates.
					addOrbToSelection(orb);										// If the player touched inside the bounding box, the orb becomes selected.
					touchTracker.addTouch(touchCoords.x, touchCoords.y);		// Adds the current touch-coordinate to the TouchTracker.
					return true;
				}
			}
		}
		return false;
	}
	
	// Processes the player dragging their finger across the screen.
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(selectedOrbs.size > 0) {									// If there are currently come selected orbs.
			Vector3 touchCoords = new Vector3(screenX, screenY, 0); // Creates a vector to store the touch co-ordinates.
			camera.unproject(touchCoords); 							// Transforms the vector from screen co-ordinates to world co-ordinates.
			touchTracker.addTouch(touchCoords.x, touchCoords.y); 	// Updates the touchTracker with the current location of the players finger.
			return true;
		}
		return false;
	}
	
	// Processes the player lifting their finger off of the screen.
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(selectedOrbs.size > 0) {	// If there are currently some orbs selected,
			scoreSelectedOrbs();	// each selected orb is removed from the box2d simulation.
			return true;
		}
		return false;
	}
	
	@Override
	public void endContact(Contact contact) {}
	
	@Override
	public void beginContact(Contact contact) {
		/*if(contact.getFixtureA().isSensor() && !contact.getFixtureB().isSensor()) {
			force.x = (contact.getFixtureA().getBody().getPosition().x - contact.getFixtureB().getBody().getPosition().x) * orbAcceleration * 2;
			force.y = (contact.getFixtureA().getBody().getPosition().y - contact.getFixtureB().getBody().getPosition().y) * orbAcceleration * 2;
			contact.getFixtureB().getBody().applyForceToCenter(force, true);
		}*/
	}
	
	@Override
	public void preSolve(Contact contact, Manifold manifold) {}
	
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {}

}
