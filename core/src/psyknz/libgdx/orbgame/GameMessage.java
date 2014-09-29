package psyknz.libgdx.orbgame;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;

public class GameMessage {
	
	private BitmapFont font;
	private String text;
	private Rectangle textBounds;
	private Sprite background;	
	private Vector2 pos;
	
	public GameMessage(String text, BitmapFont font, float x, float y) {
		this.font = font;
		pos = new Vector2(x, y);
		setText(text);
	}
	
	public void setText(String text) {
		this.text = text;
		BitmapFont.TextBounds bounds = font.getBounds(text);
		textBounds = new Rectangle(pos.x - bounds.width / 2, pos.y - bounds.height / 2, bounds.width, bounds.height);
	}
	public void setFont(BitmapFont font) {
		this.font = font;
		setText(text);
	}
	
	public void setBackground(Sprite sprite, float width, float height) {
		background = sprite;
		background.setSize(width, height);
		background.setCenter(pos.x, pos.y);
	}
	
	public void draw(SpriteBatch batch) {
		background.draw(batch);
		font.draw(batch, text, textBounds.x, textBounds.y + textBounds.height);
	}
	
	public void setPos(float x, float y) {
		translatePos(x - pos.x, y - pos.y);
	}
	
	public void translatePos(float x, float y) {
		pos.add(x, y);
		if(background != null) background.translate(x, y);
		textBounds.x += x;
		textBounds.y += y;
	}
	
	public Vector2 getPos() {
		return pos;
	}
}
