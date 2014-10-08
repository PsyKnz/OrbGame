package psyknz.libgdx.orbgame;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

public abstract class GameButton extends InputAdapter {
	
	public Sprite sprite; 			// Sprite used to draw the button.
	public Camera camera = null;	// Reference to the camera drawing this element. Used to unproject touch co-ordinates.
	
	private boolean selected = false;				// Flag for whether the button is currently selected.
	private Vector3 touchCoords = new Vector3();	// Vector used to store transformed touch co-ordinates.
	
	public GameButton(Sprite sprite) {
		this.sprite = sprite;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return touchDragged(screenX, screenY, pointer);
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		touchCoords.set(screenX, screenY, 0);
		if(camera != null) camera.unproject(touchCoords);
		if(sprite.getBoundingRectangle().contains(touchCoords.x, touchCoords.y)) return selected = true;
		return false;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(selected) {
			selected = false;
			buttonAction();
			return true;
		}
		return false;
	}
	
	public void draw(SpriteBatch batch) {
		sprite.draw(batch);
	}
	
	public abstract void buttonAction();

}
