package psyknz.libgdx.orbgame;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class TextElement {
	
	public static final int LEFT = 0, TOP = 0;		// Alignment constants for text elements.
	public static final int CENTER = 1;				// Text can be horizontally aligned to the LEFT, CENTER, and RIGHT.
	public static final int RIGHT = 2, BOTTOM = 2;	// Text can be vertically aligned to the TOP, CENTER, and BOTTOM.
	
	private String text;		// The text this element draws.
	private BitmapFont font;	// The font used to draw the text.
	private Vector2 pos;		// The position on the screen this element should be drawn.
	private Rectangle bounds;	// Bounding box for the text.
	
	private int hAlign, vAlign; // Current horizontal and vertical alignment for the text (relative to its position).
	
	public TextElement(String text, BitmapFont font, float x, float y) {
		bounds = new Rectangle();	// Initialises the bounding box for the text.
		pos = new Vector2(x, y);	// Sets the position of this element.
		this.text = text;			// Sets the text this element displays.
		hAlign = vAlign = CENTER;	// By default the text is aligned as CENTER, CENTER.
		setFont(font);				// Sets the font used to draw this text.
	}
	
	public void setFont(BitmapFont font) {
		this.font = font;	// Sets the font used to draw the text.
		setText(text);		// Resets the text to ensure the bounding box is updated accordingly.
	}
	
	/** Returns the font used by this element.
	 * @return Reference to the font used to draw this elements text.
	 * @warning Changing font parameters can have unexpected results. It is recomended you call 
	 * {@link psyknz.libgdx.orbgame.TextElement#setFont(BitmapFont)}, passing in the altered font, after making any changes. */
	public BitmapFont getFont() {
		return font; 
	}
	
	public void setText(String text) {
		this.text = text;											// Sets the text this element will draw.
		BitmapFont.TextBounds textBounds = font.getBounds(text);	// Determines the size of the text.
		bounds.setSize(textBounds.width, textBounds.height);		// Sets the size of the bounding box to match the text.
		setAlignment(hAlign, vAlign);
	}
	
	public String getText() {
		return text; // Returns the text this element displays.
	}
	
	public void setAlignment(int h, int v) {
		bounds.setPosition(pos);							// By default sets the position of the bounding box to TOP, LEFT.
		if(h == RIGHT) bounds.x -= bounds.width;			// Adjusts x to horizontally align the text to the RIGHT.
		else if(h == CENTER) bounds.x -= bounds.width / 2;	// Adjusts x to horizontally align the text in the CENTER.
		if(v >= BOTTOM) bounds.y += bounds.height;			// Adjusts y to vertically align the text to the BOTTOM.
		else if(v == CENTER) bounds.y += bounds.height / 2;	// Adjusts y to vertically align the text in the CENTER.
		
		this.hAlign = h;	// Sets the horizontal alignment.
		this.vAlign = v;	// Sets the vertical alignment.
	}
	
	public int getHAlignment() {
		return hAlign; 	// Returns the current horizontal alignment of the text.
	}
	
	public int getVAlignment() {
		return vAlign;	// Returns the current vertical alignment of the text.
	}
	
	public void setPosition(float x, float y) {
		pos.set(x, y);					// Sets the x, y co-ordinates of the element,
		setAlignment(hAlign, vAlign);	// and shifts the text to that position based on its current alignment.
	}
	
	public Vector2 getPosition() {
		return pos;	// Returns the current position of the text.
	}
	
	public Rectangle getBounds() {
		return bounds; // Returns a reference to the texts bounding box.
	}
	
	public void draw(SpriteBatch batch) {
		font.draw(batch, text, bounds.x, bounds.y);	// Draws the text to the screen.
	}

}
