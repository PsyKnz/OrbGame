package psyknz.libgdx.orbgame;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public interface UiElement extends InputProcessor {
	
	public void setCamera(Camera camera);
	
	public void draw(SpriteBatch batch);
	
	public Rectangle getBounds();
}
