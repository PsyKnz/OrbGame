package psyknz.libgdx.orbgame;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class OrbElement {
	
	/* Enum containing all the different states an orb can be in.
	 * MAGNET: 			This orb attracts all other orbs towards itself.
	 * BORDER: 			This orb is used to determine whether or not an orb is inside the play area.
	 * FREE:			This orb is currently freely being affected by box2d forces.
	 * SELECTED:		This orb is currently selected by the player and part of their chain.
	 * ACTIVE_SELECTED:	This orb is the most recently selected player orb and controls if other join the chain. */
	public enum State {MAGNET, BORDER, FREE, SELECTED, ACTIVE_SELECTED};
	
	private Body body; 		// Reference to the body this is orb data for.
	private Sprite sprite; 	// Reference to the sprite to use when drawing this orb.
	private State state; 	// Variable to track the current state of the element.
	private Circle bounds; 	// Circle representing the orbs bounding box.
	
	private PulseElement pulse = null;	// Reference to the pulse coming from this orb. No pulse by default.
	
	private boolean inPlayArea = false;	// Flag to track whether or not the orb is currently in the play area.
	
	private Vector2 force; // Temporary variable used to process forces applied to the orb.
	
	public OrbElement(Body body, Sprite sprite, State state) {
		this.body = body; 		// Records the box2d body this orb is the user data for.
		this.sprite = sprite; 	// Sets the sprite to use when drawing this orb.
		this.state = state;		// Sets this orbs current state.
		
		// Creates a bounding circle for this orb the same size as the box2d circle it is user data for.
		bounds = new Circle(0, 0, body.getFixtureList().first().getShape().getRadius());
		
		update(0); // Does an initial blank update to synchronise the bounding box and sprite with the box2d body.
	}
	
	public void update(float delta) {
		bounds.setPosition(body.getPosition());	// Matches the location of the orbs bounding box to its physics body.
		sprite.setCenter(bounds.x, bounds.y);	// Matches the location of the orbs sprite to its physics body.
		
		if(pulse != null) {							// If this orb currently has a pulse attached to itm
			pulse.setCenterPos(body.getPosition());	// its position is synchronised with the physics body,
			pulse.update(delta);					// and it is updated with delta time.
		}
	}
	
	// Returns the Circle representing the orbs bounding box.
	public Circle getBounds() {
		return bounds;
	}
	
	// Returns the sprite to use to draw this orb.
	public Sprite getSprite() {
		return sprite;
	}
	
	// Applies a force of given intensity to attract the body this OrbElement is data for to the target point.
	public void attractTo(Vector2 target, float intensity) {
		force = new Vector2(target.x - body.getPosition().x, target.y - body.getPosition().y);
		force.clamp(intensity, intensity);
		body.applyForceToCenter(force, true);
	}
	
	public void setPulse(PulseElement pulse) {
		this.pulse = pulse;
		pulse.setCenterPos(bounds.x, bounds.y);
	}
	
	public PulseElement getPulse() {
		return pulse;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public State getState() {
		return state;
	}

	public void enterPlayArea() {
		inPlayArea = true;
	}
	
	public void exitPlayArea() {
		inPlayArea = false;
	}
}
