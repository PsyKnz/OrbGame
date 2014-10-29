package psyknz.libgdx.orbgame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class TextElement {
	
	public static final int LEFT = 0, TOP = 0;		// Alignment constants for text elements.
	public static final int CENTER = 1;				// Text can be horizontally aligned to the LEFT, CENTER, and RIGHT.
	public static final int RIGHT = 2, BOTTOM = 2;	// Text can be vertically aligned to the TOP, CENTER, and BOTTOM.
	
	private String text;				// The text this element draws.
	private BitmapFont font;			// The font used to draw the text.
	private Vector2 pos, scale, draw;	// Vectors for the texts position, scale, and the co-ordinates for its draw function.
	private Rectangle bounds;			// Bounding box for the text.
	
	public Color color = new Color(Color.WHITE); // The color the text element is set to.
	
	private int hAlign, vAlign; // Current horizontal and vertical alignment for the text (relative to its position).
	
	/** Creates a new TextElement with both its horizontal and vertical alignment as CENTER. 
	 * @param text Text the element should display.
	 * @param font The font used to draw the text.
	 * @param x The x position of the text.
	 * @param y The y position of the text. */
	public TextElement(String text, BitmapFont font, float x, float y) {
		this(text, font, x, y, CENTER, CENTER);
	}
	
	public TextElement(String text, BitmapFont font, float x, float y, int hAlign, int vAlign) {
		bounds = new Rectangle();	// Initialises the bounding box for the text.
		pos = new Vector2(x, y);	// Sets the position of this element.
		scale = new Vector2();		// Sets the scale of the text element to its default size.
		draw = new Vector2();
		this.text = text;			// Sets the text this element displays.
		this.hAlign = hAlign;		// Sets the horizontal alignment of the text,
		this.vAlign = vAlign;		// and its vertical alignment.
		setFont(font);				// Sets the font used to draw this text.
	}
	
	public void setFont(BitmapFont font) {
		setFont(font, 1, 1);	// Sets the new font and resets the scaling factor.
	}
	
	public void setFont(BitmapFont font, float scaleX, float scaleY) {
		this.font = font;			// Sets the font used to draw the text element.
		setScale(scaleX, scaleY);	// Sets the scaling factor for the font.
	}
	
	public void setScale(Vector2 scale) {
		setScale(scale.x, scale.y);	// Sets this texts scale using the x and y values from the given scaling vector.
	}
	
	public void setScale(float scaleX, float scaleY) {
		scale.set(scaleX, scaleY);	// Sets the scaling factor for the text element,
		setText(text);				// and resets the text to reconfigure its bounding box.
	}
	
	/** Function to scale the text element so that it fits inside of a given area. Can be scaled on the x and y axis independently,
	 * or uniformly; in which case the x and y are scaled by the same factor while staying inside the designated area. May leave
	 * unfilled space.
	 * @param area Rectangle representing the area of space you want the text to fill.
	 * @param uniform True if the text should scale by the same amount on both the x and y axis. */
	public void scaleToFit(Rectangle area, boolean uniform) {
		scaleToFit(area.width, area.height, uniform);
	}
	
	public void scaleToFit(float width, float height, boolean uniform) {
		setScale(1, 1);										// Resets the font to its natural scale.
		float scaleX = width / bounds.width;				// Determines how much the x axis needs to be scaled by,
		float scaleY = height / bounds.height;				// as well as the y axis.
		if(uniform) {										// If scaling uniformly,
			if(scaleX < scaleY) setScale(scaleX, scaleX);	// and the x axis scaling factor is smallest it is used.
			else setScale(scaleY, scaleY);					// Otherwise the y axis scaling factor is used.
		}
		else setScale(scaleX, scaleY);	// If not scaling uniformly the x and y axis are adjusted independently.
	}
	
	/** @return Returns a vector representing this texts scaling factor. */
	public Vector2 getScale() {
		return scale;
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
		font.setScale(scale.x, scale.y);							// Sets the fonts scaling factor to get its bounding box.
		BitmapFont.TextBounds textBounds = font.getBounds(text);	// Determines the size of the text.
		bounds.setSize(textBounds.width, textBounds.height);		// Sets the size of the bounding box to match the text,
		setAlignment(hAlign, vAlign);								// and then re-aligns the text.
	}
	
	public String getText() {
		return text; // Returns the text this element displays.
	}
	
	public void setAlignment(int h, int v) {
		draw.set(pos);				// By default sets the point to draw from as the texts position,
		bounds.setPosition(pos);	// as well as the lower left corner of the bounding box. No changes if aligned LEFT.
		
		if(h == RIGHT) draw.x = bounds.x -= bounds.width;			// If aligned RIGHT then draw and bounds must be shifted left,
		else if(h == CENTER) draw.x = bounds.x -= bounds.width / 2;	// same thing if CENTERED horizontally, but shifted by half as much.
		if(v >= BOTTOM) draw.y += bounds.height;					// If aligned to the bottom only the draw point needs to be shifted up.
		else if(v == CENTER) {										// If vertical alignment is CENTER,
			draw.y += bounds.height / 2;							// then the draw points needs to be raied by half a much,
			bounds.y -= bounds.height / 2;							// and the bounding box moved down by the same amount.
		}
		else bounds.y -= bounds.height;								// Lastly, if aligned to the TOP the draw point stays the same but the bounding box shifts down.
		
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
		font.setColor(color);					// Sets the color the text will be drawn,
		font.setScale(scale.x, scale.y);		// and its scale.
		font.draw(batch, text, draw.x, draw.y);	// Draws the text to the screen.
	}

}
