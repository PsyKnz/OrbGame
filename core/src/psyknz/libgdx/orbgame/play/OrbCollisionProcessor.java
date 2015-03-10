package psyknz.libgdx.orbgame.play;

import psyknz.libgdx.orbgame.layers.OrbLayer;

import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;

import aurelienribon.tweenengine.*;

public class OrbCollisionProcessor implements ContactListener {
	
	private OrbLayer layer; 		// Reference to the screen this processor is attached to.
	
	private OrbData orbDataA, orbDataB;	// Temporary variables used to access orb user data.
	
	/**
	 * Creates a new collision processor and registers it with the box2d simulation it is responsible for.
	 * @param layer The orb layer this collision processor is attached to.
	 * @param world The box2d simulation this is a collision processor for.
	 * @param manager The tween manager this collision processor should use for delayed events.
	 */
	public OrbCollisionProcessor(OrbLayer layer) {
		this.layer = layer;		// Sets the layer, world, and manager this collision processor interacts with.
		
		layer.world.setContactListener(this);	// Sets this contact listener as the listener for the given world.
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
	
	/** 
	 * Used to process collisions. Custom collisions only occur between the border and free orbs.
	 * @param a the orb to test as a sensor.
	 * @param b the orb to test as a normal orb body.
	 * @return true if a collision outcome was detected.
	 */
	public boolean processBeginContact(Fixture a, Fixture b) {
		orbDataA = (OrbData) a.getBody().getUserData();		// Accesses the orb data for Fixture A
		if(orbDataA.getState() == OrbData.State.BORDER) {	// and determines if it is the games border.
			orbDataB = (OrbData) b.getBody().getUserData();	// If it is, orb data for Fixture B is accessed
			if(orbDataB.getState() == OrbData.State.FREE) {	// and whether or not it is a free moving orb is determined.
				return orbDataB.inPlay = true;				// If it is, the orb is designated as in play and a collision reported.
			}
		}
		
		else if(orbDataA.getState() == OrbData.State.FREE) {		// If Fixture A is instead a free moving orb
			orbDataB = (OrbData) b.getBody().getUserData();			// then orb data for Fixture B is accessed.
			if(orbDataB.getState() == OrbData.State.FREE ||			// Tests if Fixture B is free moving aswell,
					orbDataB.getState() == OrbData.State.MAGNET) {	// or if it is the magnet.
				if(!orbDataA.inPlay && orbDataB.inPlay || 
						orbDataA.inPlay && !orbDataB.inPlay) {
					
					Tween.call(new TweenCallback() {
						@Override
						public void onEvent(int type, BaseTween<?> source) {
							layer.endGame();
						}
					}).start(layer.manager);
					
				}
				
				Tween.call(new JointCallback(a.getBody(), b.getBody()))	// Queues up building a joint between a and b
						.start(layer.manager);							// and passes the event to the layers tween manager.
				return true;											// and a collision is reported.
			}
		}
		return false;	// Otherwise no collision is reported.
	}
	
	/** 
	 * Used to process collisions. Custom collisions only occur between actively selected orbs and free orbs, and between the border and
	 * free orbs. 
	 * @param a the orb to test as a sensor.
	 * @param b the orb to test as a normal orb body.
	 * @return	true if a collision outcome was detected. 
	 */
	public boolean processEndContact(Fixture a, Fixture b) {
		orbDataA = (OrbData) a.getBody().getUserData();				// Accesses the orb data for Fixture A
		if(orbDataA.getState() == OrbData.State.ACTIVE_SELECTED) {	// and determines if it is the actively selected orb.
			orbDataB = (OrbData) b.getBody().getUserData();			// If it is orb data for Fixture B is accessed.
			if(orbDataB.getState() == OrbData.State.FREE && 		// If Fixture B is free moving and
					orbDataB.getSprite().getColor().equals(			// is the same colour as Fixture A
							orbDataA.getSprite().getColor())) {		//
				Tween.call(new SelectOrbCallback(orbDataB))			// Orb B becomes selected
						.start(layer.manager);						// and the call is delayed until the next update.							
				return true;										// and a collision is reported.
			}
		}
		return false;	// Otherwise no collision is reported.
	}
	
	/**
	 * 
	 * @author W110ER
	 *
	 */
	private class JointCallback implements TweenCallback {
		
		private DistanceJointDef jointDef;	// Reference to the joint definition which will be implemented on callback.
		
		/**
		 * Creates a new JointCallback which will join bodies a and b together once completed.
		 * @param a The first body you want included in the joint.
		 * @param b The second body you want included in the joint.
		 */
		public JointCallback(Body a, Body b) {
			jointDef = new DistanceJointDef();								// Defines a new fixed distance joint
			jointDef.initialize(a, b, a.getPosition(), b.getPosition());	// and initialises it using the two bodies passed.
			jointDef.length = OrbLayer.ORB_DIAMETER;						// Ensures the length of the joint is the size of the orb.
		}
		
		@Override
		public void onEvent(int type, BaseTween<?> source) {
			layer.world.createJoint(jointDef);	// Once complete the joint is created.
		}
	}
	
	/**
	 * 
	 * @author W110ER
	 *
	 */
	private class SelectOrbCallback implements TweenCallback {
		
		private OrbData orb;	// Reference to the orb which has been selected.
		
		/**
		 * Creates a new select orb callback to delay selecting the orb until this update is completed.
		 * @param orb The orb that is being selected.
		 */
		public SelectOrbCallback(OrbData orb) {
			this.orb = orb;
		}
		
		@Override
		public void onEvent(int type, BaseTween<?> source) {
			layer.selectOrb(orb);	// Once called, the saved orb becomes selected.
		}
	}

}

/*
switch(type) {
case TweenCallback.ANY: System.out.println("TweenCallback " + this.toString() + " ANY called."); break;
case TweenCallback.ANY_BACKWARD: System.out.println("TweenCallback " + this.toString() + " ANY_BACKWARD called."); break;
case TweenCallback.ANY_FORWARD: System.out.println("TweenCallback " + this.toString() + " ANY_FORWARD called."); break;
case TweenCallback.BACK_BEGIN: System.out.println("TweenCallback " + this.toString() + " BACK_BEGIN called."); break;
case TweenCallback.BACK_COMPLETE: System.out.println("TweenCallback " + this.toString() + " BACK_COMPLETE called."); break;
case TweenCallback.BACK_END: System.out.println("TweenCallback " + this.toString() + " BACK_END called."); break;
case TweenCallback.BACK_START: System.out.println("TweenCallback " + this.toString() + " BACK_START called."); break;
case TweenCallback.BEGIN: System.out.println("TweenCallback " + this.toString() + " BEGIN called."); break;
case TweenCallback.COMPLETE: System.out.println("TweenCallback " + this.toString() + " COMPLETE called."); break;
case TweenCallback.END: System.out.println("TweenCallback " + this.toString() + " END called."); break;
case TweenCallback.START: System.out.println("TweenCallback " + this.toString() + " START called."); break;
}
*/
