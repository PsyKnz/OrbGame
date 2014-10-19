package psyknz.libgdx.orbgame.tweenaccessors;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.graphics.Camera;

public class CameraTween implements TweenAccessor<Camera> {
	
	public static final int POS_X = 0;
	public static final int POS_Y = 1;
	public static final int POS_Z = 2;
	public static final int POS = 3;
	public static final int VIEW_WIDTH = 4;
	public static final int VIEW_HEIGHT = 5;
	public static final int VIEW = 6;
	
	@Override
	public int getValues(Camera cam, int tweenType, float[] val) {
		switch(tweenType) {
			case POS_X: val[0] = val[0] = cam.position.x; return 1;
			case POS_Y: val[0] = val[0] = cam.position.y; return 1;
			case POS_Z: val[0] = val[0] = cam.position.z; return 1;
			case POS: val[0] = cam.position.x; val[1] = cam.position.y; 
					val[2] = cam.position.z; return 3;
			case VIEW_WIDTH: val[0] = cam.viewportWidth; return 1;
			case VIEW_HEIGHT: val[0] = cam.viewportHeight; return 1;
			case VIEW: val[0] = cam.viewportWidth; val[1] = cam.viewportHeight; return 2;
			default: assert false; return -1;
		}
	}
	
	@Override
	public void setValues(Camera cam, int tweenType, float[] val) {
		switch(tweenType) {
			case POS_X: cam.position.x = val[0]; cam.update(); break;
			case POS_Y: cam.position.y = val[0]; cam.update(); break;
			case POS_Z: cam.position.z = val[0]; cam.update(); break;
			case POS: cam.position.set(val); cam.update(); break;
			case VIEW_WIDTH: cam.viewportWidth = val[0]; cam.update(); break;
			case VIEW_HEIGHT: cam.viewportHeight = val[0]; cam.update(); break;
			case VIEW: cam.viewportWidth = val[0]; cam.viewportHeight = val[1];
					cam.update(); break;
			default: assert false;
		}
	}

}
