package psyknz.libgdx.orbgame;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;

public class OrbCollisionProcessor implements ContactListener {
	
	private OrbElement orbDataA, orbDataB;	// Temporary variables used to access orb user data.
	
	// OrbCollisionProcessor does no preSolving.
	@Override
	public void preSolve(Contact contact, Manifold manifold) {}
	
	@Override
	public void beginContact(Contact contact) {
		if(!beginContactTest(contact.getFixtureA(), contact.getFixtureB())) { 	// If the contact test fails for A vs B,
			beginContactTest(contact.getFixtureB(), contact.getFixtureA());		// then it is tested for B vs A.
		}
	}
	
	@Override
	public void endContact(Contact contact) {
		if(!endContactTest(contact.getFixtureA(), contact.getFixtureB())) {	// If the contact test fails for A vs B,
			endContactTest(contact.getFixtureB(), contact.getFixtureA());	// then it is tested for B vs A.
		}
	}
	
	// OrbCollisionProcessor does no postSolving.
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	
	/** Used to process collisions. Custom collisions only occur between sensors and non-sensors. If a dynamic sensor (always attached
	 *  to a freely moving orb) comes in contact with a non-sensor orb it increases its count of nearby orbs. If the sensor is static
	 *  and comes in contact with a dynamic non-sensor, it is the player interacting with an orb.
	 * @param orbA the orb to test as a sensor.
	 * @param orbB the orb to test as a normal orb body.
	 * @return	true if a collision outcome was detected. */
	private boolean beginContactTest(Fixture orbA, Fixture orbB) {
		if(orbA.isSensor() && !orbB.isSensor()) {
			if(orbA.getBody().getType() == BodyDef.BodyType.DynamicBody) {
				orbDataA = (OrbElement) orbA.getBody().getUserData();
				orbDataA.nearbyDynamicOrbs++;
				return true;
			}
			else if(orbA.getBody().getType() == BodyDef.BodyType.StaticBody && orbB.getBody().getType() == BodyDef.BodyType.DynamicBody) {
				orbDataA = (OrbElement) orbA.getBody().getUserData();
				orbDataB = (OrbElement) orbB.getBody().getUserData();
				if(orbDataA.getSprite().getColor().equals(orbDataB.getSprite().getColor())) {
					orbDataB.nearbyPlayerOrbs.add(orbA.getBody());
					return true;
				}
			}
		}
		return false;
	}
	
	/** Used to process collisions. Custom collisions only occur between sensors and non-sensors. If a dynamic sensor (always attached
	 *  to a freely moving orb) leaves contact with a non-sensor orb it decreases its count of nearby orbs. If the sensor is static
	 *  and leaves contact with a dynamic non-sensor, the player is no longer interacting with the orb. 
	 * @param orbA the orb to test as a sensor.
	 * @param orbB the orb to test as a normal orb body.
	 * @return	true if a collision outcome was detected. */
	private boolean endContactTest(Fixture orbA, Fixture orbB) {
		if(orbA.isSensor() && !orbB.isSensor()) {
			if(orbA.getBody().getType() == BodyDef.BodyType.DynamicBody) {
				orbDataA = (OrbElement) orbA.getBody().getUserData();
				orbDataA.nearbyDynamicOrbs--;
				return true;
			}
			else if(orbA.getBody().getType() == BodyDef.BodyType.StaticBody && orbB.getBody().getType() == BodyDef.BodyType.DynamicBody) {
				orbDataA = (OrbElement) orbA.getBody().getUserData();
				orbDataB = (OrbElement) orbB.getBody().getUserData();
				if(orbDataA.getSprite().getColor().equals(orbDataB.getSprite().getColor())) {
					orbDataB.nearbyPlayerOrbs.removeValue(orbA.getBody(), true);
					return true;
				}
			}
		}
		return false;
	}

}
