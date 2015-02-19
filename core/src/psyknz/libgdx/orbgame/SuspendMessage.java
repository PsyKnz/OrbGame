package psyknz.libgdx.orbgame;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class SuspendMessage extends InputAdapter implements UiElement {
	
	public static final float PADDING_RATIO = 0.1f;	//
	
	private TextElement msg;	// Reference to the text element displaying the message.
	private Sprite bg;			// Reference to the sprite used to draw the background for the element.
	private GameUi ui;		// Reference to the UI element this message is being managed by.
	private Vector2 offset;
	
	public SuspendMessage(Sprite background, TextElement message, GameUi ui) {
		this(background, message, ui, 0, 0);
	}
	
	public SuspendMessage(Sprite background, TextElement message, GameUi ui, float x, float y) {
		this.bg = background;
		this.msg = message;
		this.ui = ui;
		this.offset = new Vector2(x, y);
	}
	
	public void setCamera(Camera camera) {
		bg.setSize(camera.viewportWidth, camera.viewportHeight);
		bg.setCenter(camera.position.x, camera.position.y);
		
		float padding;
		if(bg.getWidth() <= bg.getHeight()) padding = bg.getWidth() * PADDING_RATIO;
		else padding = bg.getHeight() * PADDING_RATIO;
		
		msg.scaleToFit(bg.getWidth() - padding * 2, bg.getHeight() - padding * 2, true);
		msg.setPosition(camera.position.x + offset.x, camera.position.y + offset.y);
	}
	
	@Override
	public void draw(SpriteBatch batch) {
		bg.draw(batch);		// Draws the message background.
		msg.draw(batch);	// Draws the message.
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
		ui.removeMessage(this);
		return true;
	}
	
	@Override
	public Rectangle getBounds() {
		return msg.getBounds();
	}
	
	public void setOffset(float x, float y) {
		offset.set(x, y);
		msg.setPosition(msg.getPosition().x + x, msg.getPosition().y + y);
	}
}
