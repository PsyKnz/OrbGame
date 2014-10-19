package psyknz.libgdx.orbgame.tweenaccessors;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.math.Vector2;

public class Vector2Tween implements TweenAccessor<Vector2> {
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int VEC = 2;
	
	@Override
	public int getValues(Vector2 vec, int tweenType, float[] val) {
		switch(tweenType) {
			case X: val[0] = vec.x; return 1;
			case Y: val[0] = vec.y; return 1;
			case VEC: val[0] = vec.x; val[1] = vec.y; return 2;
			default: assert false; return -1;
		}
	}
	
	@Override
	public void setValues(Vector2 vec, int tweenType, float[] val) {
		switch(tweenType) {
			case X: vec.x = val[0]; break;
			case Y: vec.y = val[0]; break;
			case VEC: vec.set(val[0], val[1]); break;
			default: assert false;
		}
	}

}
