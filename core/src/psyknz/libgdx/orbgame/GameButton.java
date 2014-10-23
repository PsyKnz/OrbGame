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
	
	/** Creates a new button based on the given sprite.
	 * @param sprite Sprite used to draw the button. */
	public GameButton(Sprite sprite) {
		this.sprite = sprite;	// Sets the sprite used to draw this button.
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return touchDragged(screenX, screenY, pointer);	// Touching down on a button has the same effect as dragging over it.
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		touchCoords.set(screenX, screenY, 0);														// Saves the co-ordinates of the players touch,
		if(camera != null) camera.unproject(touchCoords);											// and translates it into world co-ordinates provided a camera is registered.
		if(sprite.getBoundingRectangle().contains(touchCoords.x, touchCoords.y)) selected = true;	// If the users finger is above the button it becomes selected.
		return false;																				// Never prevents other input from being processed.
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(selected) {			// If the button is currently selected,
			selected = false;	// it is deselected,
			buttonAction();		// and performs its action.
			return true;		// Once done returns true to indicate input was processed.
		}
		return false;	// Otherwise no input is processed and returns false.
	}
	
	/** Draws the button to the screen.
	 * @param batch SpriteBatch used to draw the button. */
	public void draw(SpriteBatch batch) {
		sprite.draw(batch);
	}
	
	/** Needs to be overrided whenever a new button is created. Determines what the button does when pressed. */
	public abstract void buttonAction();

}
