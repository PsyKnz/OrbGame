package psyknz.libgdx.orbgame.uifeatures;

import psyknz.libgdx.architecture.UIFeature;

import psyknz.libgdx.orbgame.TextElement;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public abstract class UIButton extends InputAdapter implements UIFeature {
	
	private Sprite selectedSpr, unselectedSpr, disabledSpr;	// Sprites used to draw the button.
	private TextElement text;								// The text element drawn over the top of the button.
	private float padding;//, paddingPix;						// Amount of padding between text and button, proportionally and in pixels.
	
	private Camera camera;							// Reference to the camera used to translate input on this button.
	private boolean selected = false;				// Flag for whether the button is currently selected.
	public boolean disabled = false;				// Flag for whether the button is currently interactable.
	private Vector3 touchCoords = new Vector3();	// Vector used to store transformed touch co-ordinates.
	
	/**
	 * Creates a new button using the given button style.
	 * @param style UI button style used to build the button.
	 */
	public UIButton(String id, UIButtonStyle style) {
		selectedSpr = new Sprite(style.selectedSpr);
		unselectedSpr = new Sprite(style.unselectedSpr);
		disabledSpr = new Sprite(style.disabledSpr);
		
		text = new TextElement(id, style.font);
		this.padding = style.padding;
	}

	@Override
	public void setBounds(float x, float y, float width, float height) {
		selectedSpr.setBounds(x, y, width, height);
		unselectedSpr.setBounds(x, y, width, height);
		disabledSpr.setBounds(x, y, width, height);
		
		float paddingPix;
		if(width > height) paddingPix = height * padding;
		else paddingPix = width * padding;
		text.scaleToFit(width - paddingPix, height - paddingPix, true);
		text.setPosition(x + width / 2, y + height / 2);
	}

	@Override
	public Rectangle getBounds() {
		return selectedSpr.getBoundingRectangle();
	}

	@Override
	public void setPosition(float x, float y) {
		selectedSpr.setPosition(x, y);
		unselectedSpr.setPosition(x, y);
		disabledSpr.setPosition(x, y);
		text.setPosition(x + selectedSpr.getWidth() / 2, y + selectedSpr.getHeight() / 2);
	}
	
	/**
	 * Sets the camera which should be used to translate input into the world space.
	 * @param camera Camera being used to draw this button.
	 */
	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
	/**
	 * Draws the button to the screen.
	 * @param batch Batch drawing the button should be included in.
	 */
	public void draw(SpriteBatch batch) {
		if(disabled) disabledSpr.draw(batch);		// The disabled sprite is drawn if disabled.
		else if(selected) selectedSpr.draw(batch);	// Otherwise is enabled and selected the selected sprite is drawn,
		else unselectedSpr.draw(batch);				// if not selected the unselected sprite is drawn.
		
		if(text != null) text.draw(batch);	// Draws the text on the button if the button has text.
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return touchDragged(screenX, screenY, pointer);	// Touching down on a button has the same effect as dragging over it.
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(camera == null || disabled) return false;	// If no camera is set or the button is disabled the input isn't processed.
		
		touchCoords.set(screenX, screenY, 0);					// Records the touch co-ordinates
		camera.unproject(touchCoords);							// and translates them into in-game units.
		if(selectedSpr.getBoundingRectangle().contains(			// If the player drags over the top of the button
				touchCoords.x, touchCoords.y)) selected = true;	// it becomes selected.
		return false;											// Other input is allowed simultaneously.
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(selected) {			// If the button is currently selected,
			selected = false;	// it is deselected,
			buttonAction();		// and performs its action.
			return true;		// Returns true to prevent more input being processed.
		}
		return false;	// Otherwise no input is processed and returns false.
	}
	
	/** 
	 * Action called whenever the button is pressed.
	 */
	public abstract void buttonAction();
	
	/**
	 * 
	 * @author W110ER
	 *
	 */
	public static class UIButtonStyle {
		
		public Sprite selectedSpr, unselectedSpr, disabledSpr;	// Template sprites to use when generating buttons.
		public BitmapFont font;									// Font to use when generating on button text.
		public float padding;									// Padding between text on buttons and the buttons edges.
		
		/**
		 * Creates a new button which uses a default font unpadded and the same sprite for all button states.
		 * @param spr The sprite to use to draw all button states.
		 */
		public UIButtonStyle(Sprite spr) {
			this(spr, new BitmapFont(), 0);
		}
		
		/**
		 * Creates a new button which uses a default font unpadded and has sprites for each button state.
		 * @param selectedSpr Sprite to use while the button is selected.
		 * @param unselectedSpr Sprite to use while the button is not selected.
		 * @param disabledSpr Sprite to use when the button is disabled
		 */
		public UIButtonStyle(Sprite selectedSpr, Sprite unselectedSpr, Sprite disabledSpr) {
			this(selectedSpr, unselectedSpr, disabledSpr, new BitmapFont(), 0);
		}
		
		/**
		 * Creates a new button style which uses the same sprite for all button states.
		 * @param spr The sprite to use to draw the button.
		 * @param font The font to use when adding text to buttons.
		 * @param padding Proportional distance between the text and the edge of the button. Should be between 0 and 1.
		 */
		public UIButtonStyle(Sprite spr, BitmapFont font, float padding) {
			this(spr, spr, spr, font, padding);
		}
		
		/**
		 * Creates a new button style using the given sprites and font.
		 * @param selectedSpr Sprite to use while the button is selected.
		 * @param unselectedSpr Sprite to use while the button is not selected.
		 * @param disabledSpr Sprite to use when the button is disabled
		 * @param font The font to use when adding text to buttons.
		 * @param padding Proportional distance between the text and the edge of the button. Should be between 0 and 1.
		 */
		public UIButtonStyle(Sprite selectedSpr, Sprite unselectedSpr, Sprite disabledSpr, BitmapFont font, float padding) {
			this.selectedSpr = selectedSpr;
			this.unselectedSpr = unselectedSpr;
			this.disabledSpr = disabledSpr;
			this.font = font;
			this.padding = padding;
		}
	}
}
