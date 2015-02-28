package psyknz.libgdx.orbgame.play;

import psyknz.libgdx.orbgame.screens.PlayScreen2D;
import psyknz.libgdx.orbgame.layers.OrbLayer;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class OrbData {
	
		/**
	 * Enum containing all the different states an orb can be in.
	 * MAGNET: 			This orb attracts all other orbs towards itself.
	 * BORDER: 			This orb is used to determine whether or not an orb is inside the play area.
	 * FREE:			This orb is currently freely being affected by box2d forces.
	 * SELECTED:		This orb is currently selected by the player and part of their chain.
	 * ACTIVE_SELECTED:	This orb is the most recently selected player orb and controls if other join the chain.
	 * GAME_OVER:		This orb was on screen when the game ended.
	 */
	public enum State {MAGNET, BORDER, FREE, SELECTED, ACTIVE_SELECTED, GAME_OVER};
	
	public static final float FREE_SPEED = PlayScreen2D.PLAY_AREA_SIZE / 4;	// Speed orbs should travel at while in free motion.
	public static final float GRAVITY = PlayScreen2D.PLAY_AREA_SIZE; 		// Force to apply to orbs as they fall off the screen.
	
	public final OrbLayer layer;	// Reference to the layer managing this orb data.
	public final Body body;			// Reference to the body this is orb data for.
	private Sprite sprite; 			// Reference to the sprite to use when drawing this orb.
	private Circle bounds; 			// Circle representing the orbs bounding box.
	
	private State state;			// Current state of the orb.
	public boolean inPlay = false;	// Whether or not the orb has entered the 'play area'.
	
	private Vector2 force, target; // Temporary variables used to process forces applied to the orb.
	
	/**
	 * 
	 * @param layer
	 * @param body
	 * @param sprite
	 * @param state
	 */
	public OrbData(OrbLayer layer, Body body, Sprite sprite, State state) {
		this.layer = layer;				// Saves references to all details passed in the data's constructor.
		this.body = body; 				//
		this.sprite = sprite; 			//
		setState(state);				//
		this.body.setUserData(this);	//
			
		bounds = new Circle(0, 0,										// Creates a bounding circle for this orb the same size as
				body.getFixtureList().first().getShape().getRadius());	// the box2d circle used for its orb.
		
		force = new Vector2();	// Instantiates a blank Vector2 which will be used to process motion.
		
		update(0); // Does an initial blank update to synchronise the bounding box and sprite with the box2d body.
	}
	
	/**
	 * Applies motion and updates the position of the orb, its bounding box, and the sprite.
	 * @param delta Time in ms since the last update call.
	 */
	public void update(float delta) {
		if(state == State.FREE) {						// If this orb is currently in FREE motion.
			force.set(target.x - body.getPosition().x, 	// The force to be applied to it is updated as the difference between
					target.y - body.getPosition().y)	// the position of the orb, and it target.
					.clamp(FREE_SPEED, FREE_SPEED);		//
			body.setLinearVelocity(force);				// The force is applied as a linear velocity.
		}
		
		else if(state == State.GAME_OVER) {					// If this orb is currently in the game over state
			body.applyForceToCenter(0, 0 - GRAVITY, true);	// a force representing gravity is applied to its center.
			if(body.getPosition().y < target.y) 			// The body is checked to see if it has passed its off-screen target.
				layer.removeOrb(this);						// If it has, it is removed from the simulation.
		}
		
		bounds.setPosition(body.getPosition());	// Matches the location of the orbs bounding box to its physics body.
		sprite.setCenter(bounds.x, bounds.y);	// Matches the location of the orbs sprite to its physics body.
	}
	
	/**
	 * Sets the state of the orb. For states requiring a target it is set to 0, 0.
	 * @param state The state this orb should be set to.
	 */
	public void setState(State state) {
		setState(state, new Vector2(0, 0));
	}
	
	/**
	 * Sets the state of the orb and the target it should move towards.
	 * @param state The state this orb should be set to.
	 * @param target Two dimensional vector this orb will use as a target while in FREE motion.
	 */
	public void setState(State state, Vector2 target) {
		this.state = state;
		this.target = target;
		if(state == State.GAME_OVER) body.setLinearVelocity(0, 0);
	}
	
	/**
	 * @return Current state of the orb.
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * @return Circle representing the orbs non-physics bouning box.
	 */
	public Circle getBounds() {
		return bounds;
	}
	
	/**
	 * @return Sprite used to draw the orb to screen.
	 */
	public Sprite getSprite() {
		return sprite;
	}
}
