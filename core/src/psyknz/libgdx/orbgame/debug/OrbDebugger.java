package psyknz.libgdx.orbgame.debug;

import psyknz.libgdx.orbgame.layers.OrbLayer;
import psyknz.libgdx.orbgame.play.OrbData;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

public class OrbDebugger extends InputAdapter {
	
	private OrbLayer layer;
	private Camera camera;
	private Box2DDebugRenderer box2dRenderer;
	private Vector3 touch = new Vector3();
	
	public OrbDebugger(OrbLayer layer) {
		this.layer = layer;
		box2dRenderer = new Box2DDebugRenderer();
		//box2dRenderer.setDrawBodies(false);
	}
	
	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
	public void draw() {
		if(camera != null) box2dRenderer.render(layer.world, camera.combined);
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(camera == null) return false;
		
		touch.set(screenX, screenY, 0);
		camera.unproject(touch);
		
		for(OrbData o : layer.orbs) {
			if(o.getBounds().contains(touch.x, touch.y)) {
				System.out.println("You selected the orb located at " 
						+ o.body.getPosition().x + ", " + o.body.getPosition().y
						+ ". That orbs sprite is located at "
						+ (o.getSprite().getX() + o.getSprite().getWidth() / 2) + ", " 
						+ (o.getSprite().getY() + o.getSprite().getHeight() / 2) + ". "
						+ "The sprite is " + o.getSprite().getWidth() + ", " + o.getSprite().getHeight() + " in size.");
				return true;
			}
		}
		
		return false;
	}

}
