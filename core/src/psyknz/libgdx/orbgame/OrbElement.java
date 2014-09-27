package psyknz.libgdx.orbgame;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.Body;

public class OrbElement {
	
	private Body body; // Reference to the body this is orb data for.
	private Sprite sprite; // Reference to the sprite to use when drawing this orb.
	private Circle bounds; // Circle representing the orbs bounding box.
	
	private Vector2 force; // Temporary variable used to process forces applied to the orb.
	private boolean playerInteracted = false; // Flag to define whether the player is currently interacting with this orb.
	private Array<Body> interactingOrbs;
	
	public OrbElement(Body body, Sprite sprite) {
		this.body = body; // Sets the box2d body this orb is the data for.
		this.sprite = sprite; // Sets this orbs color.
		bounds = new Circle(0, 0, body.getFixtureList().first().getShape().getRadius()); // Creates the bounding box circle for this orb and sets it radius equal to that for the simulation body.
		updateBounds(); // Does an initial update of the bounding box.
		
		interactingOrbs = new Array<Body>();
	}
	
	// Updates the orbs bounding box position to match the box2d simulation.
	public void updateBounds() {
		bounds.setPosition(body.getPosition());
		sprite.setCenter(bounds.x, bounds.y);
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
}
