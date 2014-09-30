package psyknz.libgdx.orbgame;

import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;

public class OrbCollisionProcessor implements ContactListener {
	
	private PlayScreen screen; // Reference to the screen this processor is attached to.
	
	private OrbElement orbDataA, orbDataB;	// Temporary variables used to access orb user data.
	
	public OrbCollisionProcessor(PlayScreen screen) {
		this.screen = screen;
	}
	
	@Override
	public void preSolve(Contact contact, Manifold manifold) {}	// OrbCollisionProcessor does no preSolving.
	
	@Override
	public void beginContact(Contact contact) {
		if(!processBeginContact(contact.getFixtureA(), contact.getFixtureB())) {	// If the contact test fails for A vs B,
			processBeginContact(contact.getFixtureB(), contact.getFixtureA());		// then it is tested for B vs A.
		}
	}
	
	@Override
	public void endContact(Contact contact) {
		if(!processEndContact(contact.getFixtureA(), contact.getFixtureB())) {	// If the contact test fails for A vs B,
			processEndContact(contact.getFixtureB(), contact.getFixtureA());	// then it is tested for B vs A.
		}
	}
	
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {}	// OrbCollisionProcessor does no postSolving.
	
	/** Used to process collisions. Custom collisions only occur between the border and free orbs.
	 * @param a the orb to test as a sensor.
	 * @param b the orb to test as a normal orb body.
	 * @return	true if a collision outcome was detected. */
	public boolean processBeginContact(Fixture a, Fixture b) {
		orbDataA = (OrbElement) a.getBody().getUserData();
		if(orbDataA.getState() == OrbElement.State.BORDER) {
			orbDataB = (OrbElement) b.getBody().getUserData();
			if(orbDataB.getState() == OrbElement.State.FREE) {
				orbDataB.enterPlayArea();
				return true;
			}
		}
		return false;
	}
	
	/** Used to process collisions. Custom collisions only occur between actively selected orbs and free orbs, and between the border and
	 * free orbs. 
	 * @param a the orb to test as a sensor.
	 * @param b the orb to test as a normal orb body.
	 * @return	true if a collision outcome was detected. */
	public boolean processEndContact(Fixture a, Fixture b) {
		orbDataA = (OrbElement) a.getBody().getUserData();
		if(orbDataA.getState() == OrbElement.State.ACTIVE_SELECTED) {
			orbDataB = (OrbElement) b.getBody().getUserData();
			if(orbDataB.getState() == OrbElement.State.FREE && orbDataB.getSprite().getColor().equals(orbDataA.getSprite().getColor())) {
				screen.selectOrb(b.getBody());
				return true;
			}
		}
		else if(orbDataA.getState() == OrbElement.State.BORDER) {
			orbDataB = (OrbElement) b.getBody().getUserData();
			if(orbDataB.getState() == OrbElement.State.FREE) {
				orbDataB.exitPlayArea();
				return true;
			}
		}
		return false;
	}

}
