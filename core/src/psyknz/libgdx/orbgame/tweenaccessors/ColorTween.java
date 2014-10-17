package psyknz.libgdx.orbgame.tweenaccessors;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.graphics.Color;

public class ColorTween implements TweenAccessor<Color> {
	
	public static final int COLOR_RED = 0;
	public static final int COLOR_GREEN = 1;
	public static final int COLOR_BLUE = 2;
	public static final int COLOR_ALPHA = 3;
	public static final int COLOR = 4;
	
	@Override
	public int getValues(Color col, int tweenType, float[] val) {
		switch(tweenType) {
			case COLOR_RED: val[0] = col.r; return 1;
			case COLOR_GREEN: val[0] = col.g; return 1;
			case COLOR_BLUE: val[0] = col.b; return 1;
			case COLOR_ALPHA: val[0] = col.a; return 1;
			case COLOR: val[0] = col.r; val[1] = col.g;
					val[2] = col.b; val[3] = col.a; return 4;
			default: assert false; return -1;
		}
	}

	@Override
	public void setValues(Color col, int tweenType, float[] val) {
		switch(tweenType) {
			case COLOR_RED: col.r = val[0]; break;
			case COLOR_GREEN: col.g = val[0]; break;
			case COLOR_BLUE: col.b = val[0]; break;
			case COLOR_ALPHA: col.a = val[0]; break;
			case COLOR: col.set(val[0], val[1], val[2], val[3]); break;
			default: assert false; break;
		}
	}

}
