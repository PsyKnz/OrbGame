package psyknz.libgdx.orbgame.uifeatures;

import psyknz.libgdx.architecture.UIFeature;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public class UISprite extends Sprite implements UIFeature {
	
	public UISprite(Sprite spr) {
		super(spr);
	}
	
	@Override
	public Rectangle getBounds() {
		return getBoundingRectangle();
	}

}
