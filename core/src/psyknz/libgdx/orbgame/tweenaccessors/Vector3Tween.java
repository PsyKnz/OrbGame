package psyknz.libgdx.orbgame.tweenaccessors;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.math.Vector3;

public class Vector3Tween implements TweenAccessor<Vector3> {
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	public static final int VEC = 3;
	
	@Override
	public int getValues(Vector3 vec, int tweenType, float[] val) {
		switch(tweenType) {
			case X: val[0] = vec.x; return 1;
			case Y: val[0] = vec.y; return 1;
			case Z: val[0] = vec.z; return 1;
			case VEC: val[0] = vec.x; val[1] = vec.y; val[2] = vec.z; return 3;
			default: assert false; return -1;
		}
	}
	
	@Override
	public void setValues(Vector3 vec, int tweenType, float[] val) {
		switch(tweenType) {
			case X: vec.x = val[0]; break;
			case Y: vec.y = val[0]; break;
			case Z: vec.z = val[0]; break;
			case VEC: vec.set(val); break;
			default: assert false;
		}
	}

}
