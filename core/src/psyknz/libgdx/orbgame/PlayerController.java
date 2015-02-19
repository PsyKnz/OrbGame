package psyknz.libgdx.orbgame;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;

public class PlayerController extends InputAdapter {
	
	public static final int POINTS_PER_ORB = 10;										// Points received per orb scored.
	public static final float SELECTED_ORB_DIAMETER = PlayScreen.ORB_DIAMETER * 1.25f;	// Size orbs should be once selected.
	
	private VectorTracker touches;		// VectorTracker recording all user input co-ordinates.
	private GameUi ui;				// Reference to the UIElement for this game.
	private PlayScreen screen;			// Reference to the PlayScreen this Controller is attached to.
	private Array<Body> selectedOrbs;	// Array containing all orbs selected by the player.
	private World world;				// Reference to the box2d simulation the player is interacting with.
	
	private Camera camera = null; 			// Reference to the camera being used to draw the scene. Does not process input if null.
	private Vector3 touch = new Vector3();	// Temporary Vector3 used to process touch input.
	private OrbElement orbData;				// Temporary OrbElement used to process information about in game orbs.
	private int activeFinger;				// Record of the finger used to select the current range of orbs.
	private Body orbToAdd = null;			// Reference to an orb which is waiting to be added to the selection (works like a flag).
	
	private Array<Joint> joints = new Array<Joint>();	// Temporary Array used to access joints.
	
	private Array<Vector2> drawCoords = new Array<Vector2>();	// Temporary array of Vector2 objects for drawing selected orbs.
	
	/** Creates a new PlayerController.
	 * @param world The box2d simulation the player is interacting with.
	 * @param ui The ui element being displayed by the screen.
	 * @param screen The screen this PlayerController is part of. */
	public PlayerController(World world, GameUi ui, PlayScreen screen) {
		this.world = world;									// Sets a reference to the box2d simulation.
		touches = new VectorTracker(SELECTED_ORB_DIAMETER);	// Creates a new VectorTracker to track touch input.
		this.ui = ui;										// Sets the UI element the controller reports scores to.
		this.screen = screen;								// Sets the screen this element is on.
		selectedOrbs = new Array<Body>();					// Initialises the array recording what orbs the player has selected.
	}
	
	/** Sets the camera to use when converting touch input to in-game units.
	 * @param camera The camera drawing elements in the box2d simulation the player can interact with. */
	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
	/** Adds an orb to the list of currently selected orbs.
	 * @param orb Reference to the orb which will be added to the array of selected orbs. */
	public void addOrbToSelection(Body orb) {		
		if(world.isLocked()) {	// If the box2d simulation is currently being processed,
			orbToAdd = orb;		// then the orb waiting to be added is queued.
			return;				// and the function exits.
		}
		
		world.getJoints(joints);										// Gets a list of all joints in the game.
		for(Joint joint : joints) {										// Iterates through each joint;
			if(joint.getBodyA() == orb || joint.getBodyB() == orb) {	// if the selected orb is attached to the joint,
				world.destroyJoint(joint);								// then the joint is destroyed.
			}
		}
		
		if(selectedOrbs.size > 0) {										// If there are already orbs selected by the player,
			orbData = (OrbElement) selectedOrbs.peek().getUserData();	// the data for the last orb is acccessed,
			orbData.state = OrbElement.State.SELECTED;					// and its state is set to SELECTED (From ACTIVE_SELECTED).
			orbData.getPulse().setPulseScale(1.3f);						// Reduces the pulse scale.
		}
		
		selectedOrbs.add(orb); 								// Adds the given orb to array of selected orbs,
		orbData = (OrbElement) orb.getUserData();			// and accesses its user data,
		orbData.state = OrbElement.State.ACTIVE_SELECTED;	// to set its state to ACTIVE_SELECTED.
		
		// Increases the distance of recorded touch inputs to accomodate for a longer chain of selected orbs.
		touches.setDistance(selectedOrbs.size * SELECTED_ORB_DIAMETER + SELECTED_ORB_DIAMETER);
		touches.addVectorToEnd(orb.getPosition());		// And adds the orbs current position to the end of the chain.
		orb.setType(BodyDef.BodyType.StaticBody);		// Sets the selected orb to static so that it isn't affected by forces.
		orb.getFixtureList().first().setSensor(true);	// Sets the selected orb as a sensor so that it doesn't automatically collide.
		
		orb.getFixtureList().first().getShape().setRadius(SELECTED_ORB_DIAMETER / 2);	// Blows up the orbs circle fixture,
		orbData.getSprite().setSize(SELECTED_ORB_DIAMETER, SELECTED_ORB_DIAMETER);		// and its sprite.
		
		Sprite pulseSprite = new Sprite(orbData.getSprite());				// Creates a new sprite to use as the orbs pulse.
		//pulseSprite.setColor(GamePalette.invertColor(pulseSprite.getColor()));
		orbData.setPulse(new PulseElement(pulseSprite, 2, 3, 0.8f, 0.0f));	// Sets the pulse for the newly selected orb.
	}
	
	/** Scores all currently selected orbs and removes them from the game. */
	public void scoreSelectedOrbs() {
		if(selectedOrbs.size <= 0) return;	// If there are no selected orbs to score nothing happens.
		
		if(world.isLocked()) {
			screen.eventProcessor.addEvent(new GameEvent() {
				@Override
				public void eventAction() {scoreSelectedOrbs();}
			});
			return;
		}
		
		ui.addPoints((int) Math.pow(selectedOrbs.size, 2) * POINTS_PER_ORB);	// The selected orbs are scored,
		screen.spawnRate *= 0.97f;												// and the game difficulty is increased.
		orbData = (OrbElement) selectedOrbs.peek().getUserData();				// User data for the last orb selected is accessed,
		orbData.state = OrbElement.State.SELECTED;							// and it is set to SELECTED to prevent further endCollision calls.
		for(Body orb : selectedOrbs) world.destroyBody(orb);					// Every selected orb is removed from the box2d simulation,
		selectedOrbs.clear();													// and the array of selected orbs is cleared.
		touches.setDistance(SELECTED_ORB_DIAMETER);								// Finally the input tracking distance is reset.
		world.getBodies(screen.getOrbs());										// Updates the list of bodies managed by the screen.
	}
	
	/** Processes the player touching down on the screen. If the player touches on top of an orb it becomes selected so that it can
	 *  follow the users finger across the screen. 
	 * @see com.badlogic.gdx.InputAdapter#touchDown(int, int, int, int) */
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(camera == null) return false;	// If no camera is set (and touch co-ordinates can't be unprojected) input isn't processed.
		
		touch.set(screenX, screenY, 0); // Saves the current co-ordinates for the users touch input.
		camera.unproject(touch); 		// Transforms the touch co-ordinates from screen space to world space.
		
		for(Body orb : screen.getOrbs()) { 								// Every orb is assessed,
			orbData = (OrbElement) orb.getUserData();					// by having its user data accessed,
			if(orbData.state == OrbElement.State.FREE) { 				// and checked to see if it is currently FREE.
				if(orbData.getBounds().contains(touch.x, touch.y)) {	// If it is and the player has touched down on it,
					activeFinger = pointer;								// the finger used to make the selection is recorded,
					addOrbToSelection(orb);								// the touched orb is added to the list of selected orbs,
					touches.addVector(touch.x, touch.y);				// and the touch co-ordinate is recorded.
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
		if(camera == null) return false;	// If no camera is set (and touch co-ordinates can't be unprojected) input isn't processed.
		
		if(selectedOrbs.size > 0 && pointer == activeFinger) {	// Only processed if the player currently has orbs selected.
			touch.set(screenX, screenY, 0); 					// Stores the co-ordinates where the player has touched the screen.
			camera.unproject(touch); 							// Transforms the touch co-ordinates from screen space to world space.
			touches.addVector(touch.x, touch.y);				// Adds the current touch-coordinate to the TouchTracker.
			return true;
		}
		return false;
	}
	
	/** Processes the player lifting their finger off of the screen. If the player has orbs selected they are scored for the player.
	 * @see com.badlogic.gdx.InputAdapter#touchUp(int, int, int, int) */
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(camera == null) return false;	// If no camera is set (and touch co-ordinates can't be unprojected) input isn't processed.
		
		if(selectedOrbs.size > 0 && pointer == activeFinger) {	// If there are currently some orbs selected,
			scoreSelectedOrbs();								// each selected orb is removed from the box2d simulation.
			return true;
		}
		return false;
	}
	
	/** Runs the game logic for the player. */
	public void update() {
		if(orbToAdd != null) {				// If an orb is currently queued to be added.
			addOrbToSelection(orbToAdd);	// It is added to the selection,
			orbToAdd = null;				// and the queue is cleared.
		}
		
		touches.interpolateCoords(drawCoords, selectedOrbs.size, SELECTED_ORB_DIAMETER);			// Interpolates an evenly spaced set of co-ordinates matching the users input.
		for(int i = 0; i < selectedOrbs.size; i++) {												// Each selected orb,
			selectedOrbs.get(i).setTransform(drawCoords.get(i), selectedOrbs.get(i).getAngle());	// is placed along those co-ordinates.
		}
	}
	
	/** Draws all player controlled resources.
	 * @param batch The SpriteBatch used to draw the players orbs. */
	public void draw(SpriteBatch batch) {
		for(Body orb: selectedOrbs) {					// Every orb the user currently has selected,
			orbData = (OrbElement) orb.getUserData();	// has its user data accessed,
			orbData.getSprite().draw(batch);			// and is drawn to the screen.
		}
	}
}
