package psyknz.libgdx.orbgame.layers;

import psyknz.libgdx.orbgame.play.OrbData;
import psyknz.libgdx.orbgame.screens.PlayScreen2D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Circle;

public class AIPlayer implements GameLayer {
	
	public static final int MIN_TARGETS = 3; // Minimum number of target orbs of a given colour for it to be worth the AI targetting them.
	
	public static final float MAX_SPEED = PlayScreen2D.PLAY_AREA_SIZE * 0.75f;	// Maximum distance/s the AI can travel.
	public static final float ACCELERATION = MAX_SPEED / 2;						// Velocity the AI should reach from 0 within 1s.
	public static final int NUM_ALT_TARGETS = 16;								// Number of blank targets for the AI to approach.
	
	private Sprite touchDown, touchUp;	// Reference to the sprites used to draw the AI while touching down, or releasing the screen.
	private boolean touching = false;	// Flag representing the AI is currently touching the screen.
	
	private OrbLayer orbLayer;			// Reference to the layer this AI is playing on.	
	private Array<OrbData> targets;		// List of orb targets the AI is aiming for at any given time.
	private Array<OrbData> touchedOrbs;	// The curret target the AI is pursuing.
	private Rectangle playField;		// Rectangle representing the active area of the playField.
	private Camera camera;				// Reference to the camera drawing the playfield.
	
	private Vector2 position, vel, dif;	// 2D vectors representing the position of the AI, its velocity, and distance to its target.
	private Vector3 screenPosition; 	// Vector3 representing the position of the finger relative to the physical screen.
	private Array<Vector2> altTargets;	//
	
	private boolean enabled = false;	// Flag to determine whether or not the AI is allowed to run.
	
	/**
	 * Creates a new AI player to interact with the specified orb layer.
	 * @param assets Asset manager containing the sprites to use to draw the player.
	 * @param layer
	 */
	public AIPlayer(AssetManager assets, OrbLayer layer) {
		position = new Vector2(0, 0);			// The AI has a starting position directly above the magnet,
		vel = new Vector2(0, 0);				// starts motionless,
		dif = new Vector2(0, 0);				// and the distance to its target is nothing, since it has no targets.
		screenPosition = new Vector3(0, 0, 0);	// By default this is also at 0, 0, 0 in screen coords.
		
		altTargets = new Array<Vector2>();	//
		
		touchDown = new Sprite(assets.get("pointer.png", Texture.class));	// New sprite representing the AI pressing down.
		touchDown.setSize(OrbLayer.ORB_DIAMETER, OrbLayer.ORB_DIAMETER);	// Sprite is set to the same size as an orb.
		touchDown.setPosition(position.x - touchDown.getWidth() * 1/4, 		// The position of the AI's sprites are updated and shifted
				position.y - touchDown.getHeight() * 3/4);					// to sit the index finger above the AI's current position.
		touchUp = new Sprite(touchDown);									// New sprite representing the AI releasing the screen.
		touchUp.setAlpha(0.5f);												// While released, the sprite is semi-transparent.
		
		this.orbLayer = layer;	// Saves a reference to the layer the AI is interacting with.
		
		targets = new Array<OrbData>();		// Creates a new blank array to prevent null calls.
		touchedOrbs = new Array<OrbData>();	// Instantiates the array to store orbs the AI has touched.
	}

	@Override
	public void resize(Camera camera) {
		this.camera = camera;	// Stores a reference to the camera drawing the scene.
		
		if(camera.viewportWidth > camera.viewportHeight) 			// If the width of the viewport is greater than its height
			playField = new Rectangle(								// then a new square rectangle is created representing the play area
					camera.position.x - camera.viewportHeight / 2, 	// centered around the x
					camera.position.y - camera.viewportHeight / 2, 	// and y position of the camera,
					camera.viewportHeight, camera.viewportHeight);	// the size of the height, the smallest edge.
		else playField = new Rectangle(								// otherwise the new square rectangle created
					camera.position.x - camera.viewportWidth / 2, 	// is centered the same
					camera.position.y - camera.viewportWidth / 2, 	//
					camera.viewportWidth, camera.viewportWidth);	// but made the size of the width, the smalled edge.
		
		altTargets.clear();																				// Clears all alt targets.
		for(float i = 0; i < NUM_ALT_TARGETS; i++) {													// For every alt target the AI has
			altTargets.add(new Vector2(camera.position.x + MathUtils.sinDeg(i * 360 / NUM_ALT_TARGETS) 	// a new 2d vector is generated
					* (playField.width - OrbLayer.ORB_DIAMETER) / 2, 									// about the centre of the screen
					camera.position.y + MathUtils.cosDeg(i * 360 / NUM_ALT_TARGETS) 					// for the AI to move towards
					* (playField.width - OrbLayer.ORB_DIAMETER) / 2));									// while trying to select orbs.
		}
	}

	@Override
	public boolean update(float delta) {
		if(!enabled) return false;	// If the AI is not currently active it does not process game logic.
		
		if(targets.size <= 0 && !touching) {	// If the AI currently has no targets and isn't touching the screen
			selectTargets();					// new targets are selected
			return false; 						// but nothing else is done in this cycle.
		}
		
		if(position.dst2(altTargets.peek()) <= MAX_SPEED * delta)	// If the AI gets close enough to an alt target that it could
			altTargets.insert(0, altTargets.pop());					// overshoot it, the next alt target is selected.
		
		if(targets.size <= 0) moveTowards(altTargets.peek(), delta);	// If there are targets to approach, the AI move towards them
		else moveTowards(targets.peek().body.getPosition(), delta);		// otherwise it follows the path of blank targets.
		
		if(touching) {
			orbLayer.player.touchDragged((int) screenPosition.x, (int) screenPosition.y, 0);	// If the AI is touching the screen its// movement is reported to the input
			for(OrbData o : targets) if(o.getBounds().contains(position)) {						// processor. Every target is checked
					touchedOrbs.add(o);															// to see whether it was passed over, if
					targets.removeValue(o, true);												// so it becomes touched and removed.
			}
		}
		
		else if(targets.peek().body.getPosition().dst(position) < OrbLayer.ORB_DIAMETER / 2							// If the AI has reached the target orb
				&& orbLayer.player.touchDown((int) screenPosition.x, (int) screenPosition.y, 0, Buttons.LEFT)) {	// the AI touches down on the orb to select// it (simulating a left mouse button click)
			touching = true;																						// and the AI is set to touching the screen.
			touchedOrbs.add(targets.pop());																			//
		}
		
		for(OrbData o : touchedOrbs)																	// Every orb the AI has touched
			if(o.getState() == OrbData.State.SELECTED || o.getState() == OrbData.State.ACTIVE_SELECTED)	// if selected, is removed from
				touchedOrbs.removeValue(o, true);														// the list of touched orbs.
		
		if(touching && touchedOrbs.size <= 0 && targets.size <= 0)	// If there are no longer any targets remaining to select
			touchUp();												// the AI releases the screen.
		
		return false;	// Lower layers are permitted to update.
	}

	@Override
	public void draw(SpriteBatch batch) {
		if(touching) touchDown.draw(batch);	// If the AI is 'touching' the screen, the touchDown sprite (a finger) is used
		else touchUp.draw(batch);			// otherwise the touchUp sprite is used.
	}

	@Override
	public void dispose() {}
	
	/**
	 * Starts the AI running.
	 */
	public void start() {
		enabled = true;
	}
	
	/**
	 * Stops the AI and makes it release the screen.
	 */
	public void end() {
		touchUp();			// Releases the screen.
		enabled = false;	// and disables the AI from acting.
	}
	
	/**
	 * Simulates the AI letting go of the screen.
	 */
	private void touchUp() {
		if(touching) orbLayer.player.touchUp((int) screenPosition.x,	// If the AI is currently touching the screen it 'lifts its
				(int) screenPosition.y, 0, Buttons.LEFT);				// finger up' from its current position in screen coordinates.
		targets.clear();												// Any remaining targets the AI has are forgotten
		touchedOrbs.clear();											// as are any records of orbs it has touched.
		touching = false;												// It is no longer touching the screen.
	}
	
	/**
	 * Method to select a new list of target orbs the AI should move to pick up.
	 */
	private void selectTargets() {
		IdentityMap<Color, Array<OrbData>> t = new IdentityMap<Color, Array<OrbData>>();	// Instantiates a map to relate lists of orbs
		for(OrbData o : orbLayer.orbs) {													// to their color then every orb is sorted
			if(!t.containsKey(o.getColor())) 									// according to its color. Colors are used as
				t.put(o.getColor(), new Array<OrbData>());						// keys, if no key exists a new list is built.
			
			if(playField.overlaps(o.getSprite().getBoundingRectangle()))	// If the current orbs sprite is inside of the play area
				t.get(o.getColor()).add(o);									// it is added to the list of targets for its color.
		}
		
		for(Color c : t.keys())							// For every color of orb available on the screen
			if(t.get(c).size > targets.size 			// If there are more orbs of that color than the current targets
					&& t.get(c).size >= MIN_TARGETS)	// and there are the minimum number of targets available for it to be worth moving
				targets = t.get(c);						// the greater color becomes the new target
	}
	
	/**
	 * Accelerates the AI towards the given target at a speed adjusted by time since the last cycle was run.
	 * @param target Point in space the AI should move towards.
	 * @param delta Time in ms since the last logic cycle was completed.
	 */	
	private void moveTowards(Vector2 target, float delta) {
		dif.set(target).sub(position)								// Finds the difference between the position of the target and the
				.limit(1);											// position of the AI, then limits it to 1 to make it proportional.
		vel.mulAdd(dif, ACCELERATION * delta)						// The difference is scaled by acceleration/s and added to the AI's 
				.limit(MAX_SPEED * delta);							// current velocity, before its velocity is limited to max speed/s.
		position.add(vel);											// The new velocity is applied to the AI to move its position.		
		
		touchUp.setPosition(position.x - touchUp.getWidth() * 1/4, 	// The position of the AI's sprites are updated and shifted to sit the
				position.y - touchUp.getHeight() * 3/4);			// index finger over top the AI position. The index finger is roughly
		touchDown.setPosition(touchUp.getX(), touchUp.getY());		// a 25% in from the left, and 75% up from the bottom.
		
		screenPosition.set(position.x, position.y, 0);					// The position of the AI is converted into a 3 dimensional
		camera.project(screenPosition);									// vector and then projected into screen co-ordinates.
		screenPosition.y = Gdx.graphics.getHeight() - screenPosition.y;	// The y co-ordinate is flipped to avoid libgdx eccentricities.
	}
}
