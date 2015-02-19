package psyknz.libgdx.orbgame.tweenaccessors;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class SpriteTween implements TweenAccessor<Sprite> {
	
	public static final int POS_X = 0;
	public static final int POS_Y = 1;
	public static final int POS = 2;
	public static final int SIZE_WIDTH = 3;
	public static final int SIZE_HEIGHT = 4;
	public static final int SIZE = 5;
	public static final int SCALE_UNIFORM = 6;
	public static final int SCALE_NONUNIFORM = 7;
	public static final int COLOR_RED = 8;
	public static final int COLOR_GREEN = 9;
	public static final int COLOR_BLUE = 10;
	public static final int COLOR_ALPHA = 11;
	public static final int COLOR = 12;

	@Override
	public int getValues(Sprite spr, int tweenType, float[] val) {
		switch(tweenType) {
			case POS_X: val[0] = spr.getX(); return 1;
			case POS_Y: val[0] = spr.getY(); return 1;
			case POS: val[0] = spr.getX(); val[1] = spr.getY(); return 2;
			case SIZE_WIDTH: val[0] = spr.getWidth(); return 1;
			case SIZE_HEIGHT: val[0] = spr.getHeight(); return 1;
			case SIZE: val[0] = spr.getWidth(); val[1] = spr.getHeight(); return 2;
			case SCALE_UNIFORM: val[0] = spr.getScaleX(); return 1;
			case SCALE_NONUNIFORM: val[0] = spr.getScaleX(); val[1] = spr.getScaleY(); return 2;
			case COLOR_RED: val[0] = spr.getColor().r; return 1;
			case COLOR_GREEN: val[0] = spr.getColor().g; return 1;
			case COLOR_BLUE: val[0] = spr.getColor().b; return 1;
			case COLOR_ALPHA: val[0] = spr.getColor().a; return 1;
			case COLOR: val[0] = spr.getColor().r; val[1] = spr.getColor().g;
					val[2] = spr.getColor().b; val[3] = spr.getColor().a; return 4;
			default: assert false; return -1;
		}
	}

	@Override
	public void setValues(Sprite spr, int tweenType, float[] val) {
		switch(tweenType) {
			case POS_X: spr.setX(val[0]); break;
			case POS_Y: spr.setY(val[0]); break;
			case POS: spr.setX(val[0]); spr.setY(val[1]); break;
			case SIZE_WIDTH: spr.setSize(val[0], spr.getHeight()); break;
			case SIZE_HEIGHT: spr.setSize(spr.getWidth(), val[0]); break;
			case SIZE: spr.setSize(val[0], val[1]); break;
			case SCALE_UNIFORM: spr.setScale(val[0]); break;
			case SCALE_NONUNIFORM: spr.setScale(val[0], val[1]); break;
			case COLOR_RED: spr.getColor().r = val[0]; break;
			case COLOR_GREEN: spr.getColor().g = val[0]; break;
			case COLOR_BLUE: spr.getColor().b = val[0]; break;
			case COLOR_ALPHA: spr.setAlpha(val[0]); break;
			case COLOR: spr.setColor(val[0], val[1], val[2], val[3]); break;
			default: assert false; break;
		}
	}

}
