package psyknz.libgdx.orbgame;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class GameMessage extends InputAdapter {
	
	public static final float PADDING_RATIO = 0.1f;	//
	
	private TextElement message;	// Reference to the text element displaying the message.
	private Sprite background;		// Reference to the sprite used to draw the background for the element.
	private UIElement ui;			// Reference to the UI element this message is being managed by.
	
	private boolean inputEnabled = false;
	private TextElement inputMessage = null;

	public GameMessage(Sprite background, TextElement message, UIElement ui) {
		this.background = background;
		this.message = message;
		this.ui = ui;
	}
	
	public void setBounds(Rectangle bounds) {
		setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
	public void setBounds(float x, float y, float width, float height) {
		background.setBounds(x, y, width, height);
		
		float paddingSize;
		if(width < height) paddingSize = width * PADDING_RATIO;
		else paddingSize = height * PADDING_RATIO;
		
		message.scaleToFit(width - paddingSize * 2, height - paddingSize * 2, true);
		message.setPosition(x + width / 2, y + height / 2);
		
		if(inputMessage != null) {
			inputMessage.scaleToFit(width - paddingSize * 2, height - paddingSize * 2, true);
			inputMessage.setPosition(x + width / 2, message.getBounds().y - message.getBounds().height - paddingSize);
		}
	}
	
	public void draw(SpriteBatch batch) {
		background.draw(batch);	//
		message.draw(batch);	//
		
		if(inputMessage != null) inputMessage.draw(batch);
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return true;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return true;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(inputEnabled) ui.removeMessage(this);
		return true;
	}
	
	public void enableInput(TextElement inputMessage) {
		this.inputMessage = inputMessage;
		inputMessage.setAlignment(TextElement.CENTER, TextElement.TOP);
		inputEnabled = true;
	}
}
