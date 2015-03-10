package psyknz.libgdx.orbgame.play;

import psyknz.libgdx.orbgame.layers.OrbLayer;
import psyknz.libgdx.orbgame.misc.VectorTracker;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;

public class PlayController extends InputAdapter {
	
	public static final float SELECTED_ORB_DIAMETER = OrbLayer.ORB_DIAMETER * 1.25f;	// Size orbs should be once selected.
	
	private OrbLayer layer;					// Reference to the orb layer this is the player controller for.
	public final VectorTracker touches;		// VectorTracker recording all user input co-ordinates.
	private Array<OrbData> selectedOrbs;	// Array containing all orbs selected by the player.
	
	private Camera camera = null; 			// Reference to the camera being used to draw the scene. Does not process input if null.
	private Vector3 touch = new Vector3();	// Temporary Vector3 used to process touch input.
	private int activeFinger;				// Record of the finger used to select the current range of orbs.
	
	private Array<Vector2> drawCoords = new Array<Vector2>();	// Temporary array of Vector2 objects for drawing selected orbs.
	
	/** 
	 * Creates a new PlayerController.
	 * @param world The box2d simulation the player is interacting with.
	 * @param ui The ui element being displayed by the screen.
	 * @param screen The screen this PlayerController is part of.
	 */
	public PlayController(OrbLayer layer) {
		this.layer = layer;									// Sets the layer this is the controller for.
		touches = new VectorTracker(SELECTED_ORB_DIAMETER);	// Creates a new VectorTracker to track touch input.
		selectedOrbs = new Array<OrbData>();				// Initialises the array recording what orbs the player has selected.
	}
	
	/** 
	 * Sets the camera to use when converting touch input to in-game units.
	 * @param camera The camera drawing elements in the box2d simulation the player can interact with. 
	 */
	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
	/** 
	 * Adds an orb to the list of currently selected orbs.
	 * @param orb Reference to the orb which will be added to the array of selected orbs.
	 */
	public void selectOrb(OrbData orb) {		
		Array<Joint> joints = new Array<Joint>();								// Temporary Array used to access joints.
		layer.world.getJoints(joints);											// Gets a list of all joints in the game.
		for(Joint joint : joints) {												// Iterates through each joint;
			if(joint.getBodyA() == orb.body || joint.getBodyB() == orb.body) {	// if the selected orb is attached to the joint,
				layer.world.destroyJoint(joint);								// then the joint is destroyed.
			}
		}
		
		if(selectedOrbs.size > 0) 									// If there are any currently selected orbs the current actively
			selectedOrbs.peek().setState(OrbData.State.SELECTED);	// selected orb becomes selected.
		orb.setState(OrbData.State.ACTIVE_SELECTED);				// to set its state to ACTIVE_SELECTED.
		selectedOrbs.add(orb); 										// Adds the given orb to array of selected orbs,
		
		touches.setDistance((selectedOrbs.size + 1) * SELECTED_ORB_DIAMETER);	// Extends the length of touch input recorded and
		touches.addVectorToEnd(orb.body.getPosition());							// adds the position of the selected orb to the end.
		
		orb.body.setType(BodyDef.BodyType.StaticBody);		// Sets the selected orb to static so that it isn't affected by forces.
		orb.body.getFixtureList().first().setSensor(true);	// Selected orb is set to a sensor to prevent physics based collisions.

		orb.body.getFixtureList().first().getShape().setRadius(SELECTED_ORB_DIAMETER / 2);	// Blows up the orbs circle fixture,
		orb.getSprite().setSize(SELECTED_ORB_DIAMETER, SELECTED_ORB_DIAMETER);				// and its sprite.
		
		// TODO: set pulses for newly selected orbs. Sprite pulseSprite = new Sprite(orbData.getSprite());
		// TODO: make the pulses color distinguishable. pulseSprite.setColor(GamePalette.invertColor(pulseSprite.getColor()));
	}
	
	/** 
	 * Scores all currently selected orbs and removes them from the game.
	 */
	public void scoreSelectedOrbs() {
		if(selectedOrbs.size <= 0) return;	// If there are no selected orbs to score nothing happens.
		
		layer.scoreOrbs(selectedOrbs.size);								// Scores all the orbs currently selected.
		selectedOrbs.peek().setState(OrbData.State.SELECTED);			// Active selected orb becomes selected to prevent collisions.
		for(OrbData o : selectedOrbs) layer.world.destroyBody(o.body);	// Every selected orb is removed from the box2d simulation,
		selectedOrbs.clear();											// and the array of selected orbs is cleared.
		touches.setDistance(SELECTED_ORB_DIAMETER);						// Finally the input tracking distance is reset.
	}
	
	/** 
	 * Processes the player touching down on the screen. If the player touches on top of an orb it becomes selected so that it can
	 * follow the users finger across the screen. 
	 * @see com.badlogic.gdx.InputAdapter#touchDown(int, int, int, int) 
	 */
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(camera == null) return false;	// If no camera is set (and touch co-ordinates can't be unprojected) input isn't processed.
		
		touch.set(screenX, screenY, 0); // Saves the current co-ordinates for the users touch input.
		camera.unproject(touch); 		// Transforms the touch co-ordinates from screen space to world space.
		
		for(OrbData o : layer.orbs) { 							// Every orb is assessed,
			if(o.getState() == OrbData.State.FREE) { 			// and checked to see if it is currently FREE.
				if(o.getBounds().contains(touch.x, touch.y)) {	// If it is and the player has touched down on it,
					activeFinger = pointer;						// the finger used to make the selection is recorded,
					touches.addVector(touch.x, touch.y);		// and the touch co-ordinate is recorded.
					layer.selectOrb(o);							// the touched orb is added to the list of selected orbs,
					return true;								// Prevents further input from being processed.
				}
			}
		}
		return false;	// Because nothing was selected further input is processed.
	}
	
	/** 
	 * Processes the player dragging their finger across the screen. Provided the player currently has some orbs selected the movement
	 * is recorded by the touchTracker object.
	 * @see com.badlogic.gdx.InputAdapter#touchDragged(int, int, int) 
	 */
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
	
	/** 
	 * Processes the player lifting their finger off of the screen. If the player has orbs selected they are scored for the player.
	 * @see com.badlogic.gdx.InputAdapter#touchUp(int, int, int, int) 
	 */
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(camera == null) return false;	// If no camera is set (and touch co-ordinates can't be unprojected) input isn't processed.
		
		if(selectedOrbs.size > 0 && pointer == activeFinger) {	// If there are currently some orbs selected,
			scoreSelectedOrbs();								// each selected orb is removed from the box2d simulation.
			return true;
		}
		return false;
	}
	
	/** 
	 * Runs the game logic for the player.
	 */
	public void update() {
		touches.interpolateCoords(										// Interpolates an evenly spaced set of co-ordinates matching
				drawCoords, selectedOrbs.size, SELECTED_ORB_DIAMETER);	// the users input and stores them in an array.
		for(int i = 0; i < selectedOrbs.size; i++) {					// For each orb that is currently selected
			selectedOrbs.get(i).setPosition(drawCoords.get(i));			// Its position is updated based on the interpolated coords.
		}
	}
	
	/** 
	 * Draws all player controlled orbs.
	 * @param batch The SpriteBatch used to draw the players orbs. 
	 */
	public void draw(SpriteBatch batch) {
		for(OrbData o: selectedOrbs) o.getSprite().draw(batch);	// and is drawn to the screen.
	}
}
