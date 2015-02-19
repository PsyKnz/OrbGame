package psyknz.libgdx.orbgame.tweenaccessors;

import aurelienribon.tweenengine.TweenAccessor;

import psyknz.libgdx.orbgame.Position2d;

public class Position2dTween implements TweenAccessor<Position2d> {
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int XY = 2;

	@Override
	public int getValues(Position2d pos, int tweenType, float[] val) {
		switch(tweenType) {
			case X: val[0] = pos.getX(); return 1;
			case Y: val[0] = pos.getY(); return 1;
			case XY: val[0] = pos.getX(); val[1] = pos.getY(); return 2;
			default: assert false; return -1;
		}
	}

	@Override
	public void setValues(Position2d pos, int tweenType, float[] val) {
		switch(tweenType) {
			case X: pos.setX(val[0]); break;
			case Y: pos.setY(val[0]); break;
			case XY: pos.setX(val[0]); pos.setY(val[1]); break;
		}
	}

}
