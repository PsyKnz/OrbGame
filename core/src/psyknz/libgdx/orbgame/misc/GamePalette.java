package psyknz.libgdx.orbgame.misc;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class GamePalette {
	
	public static final float HUE_DISTANCE = 40;	// How different generated colors should be from each other.
	public static final float SATURATION = 0.9f;	// Saturation used when generating a palette solely from hue.
	public static final float VOLUME = 0.9f;		// Volume used when generating a palette solely from hue.
	public static final int LIST_REPETITIONS = 3;	// Maximum number of times a color is placed in the array of random colors.
	
	private Color[] colors;			// Array of the colors available to use in the current playthrough.
	private Array<Color> colorList;	// List of colors assorted randomly used when generating orbs.
	
	/** 
	 * Creates a new GamePalette object with the number of colours specified in the argument, based around a random hue.
	 * @param numColors Number of colors the first palette generated should contain. 
	 */
	public GamePalette(int numColors) {
		generatePalette(MathUtils.random(360.0f), numColors);	// Generates an initial palette based on a random hue.
	}
	
	/**
	 * Creates a new GamePalette using the list of vectors passed as arguments.
	 * @param colorVectors List of vectors to  convert into colors.
	 */
	public GamePalette(Vector3... colorVectors) {
		generatePalette(colorVectors);	// Generates an initial palette based on the array of vectors provided.
	}
	
	/** 
	 * Generates a new palette derived from the hue provided containing the number of colours specified.
	 * @param hue Base hue to use when generating a new palette. All colors will be derived from rotations of this hue.
	 * @param numColors Number of colors to generate in the palette. 
	 */
	public void generatePalette(float hue, int numColors) {
		colors = new Color[numColors];	// Initialises a new array to hold the generated colors.
		
		hue = rotateVal(hue - (numColors * HUE_DISTANCE) / 2);								// Rotates the hue to the first derived color.
		for(int i = 0; i < numColors; i++) {												// Each color is generated,
			colors[i] = HSVToRGB(rotateVal(hue + i * HUE_DISTANCE), SATURATION, VOLUME);	// by slowly rotating the hue past the base.
		}
		
		if(colorList != null && colorList.size > 0)	// If a list of randomised colors has been created and it is not empty
			colorList.clear(); 						// the list is cleared to prevent old colors being selected randomly.
	}
	
	/**
	 * Generates a new color palette using the given three dimensional vectors to determine what the colors should be. A vectors x 
	 * component represents the hue (0.0 - 360.0), its y component represents the saturation (0.0 - 1.0), and the z component represents
	 * the volume (0.0 - 1.0).
	 * @param colorVectors Array of vectors to use when generating colors.
	 */
	public void generatePalette(Vector3... colorVectors) {
		colors = new Color[colorVectors.length];	// Initialises a new array to hold the generated colors.
		for(int i = 0; i < colors.length; i++) {	// For every color which is to be generated
			colors[i] = HSVToRGB(colorVectors[i]);	// the corresponding vector is used to create a new color.
		}
	}
	
	/**
	 * @param index Position of the color in the backing array.
	 * @return Reference to the color in the palette. Should not be directly edited.
	 */
	public Color getColor(int index) {
		return colors[index];
	}
	
	/**
	 * @return Number of colors present in the palette.
	 */
	public int getNumColors() {
		return colors.length;
	}
	
	/** 
	 * Returns a random color from the list of predefined colors using a shuffled list without replacement. 
	 * @return Reference to a random color. 
	 */
	public Color getRandomColor() {
		if(colorList == null) colorList = new Array<Color>();	// If an array hasn't yet been created to hold randomised color, it is.
		
		if(colorList.size <= 1) {								// If the list of colors to choose from is empty a new list is generated.			
			for(int num = 0; num < LIST_REPETITIONS; num++) {	// The list is filled with three of each color,
				for(int i = 0; i < colors.length; i++) {		// using colors available in the current theme.
					colorList.add(colors[i]);					// Colors are added sequentially.
				}
			}
			colorList.shuffle(); // Shuffles the newly generated list to allow for randomisation.
		}
		return colorList.pop(); // Returns the color at the end of the list.
	}
	
	/** 
	 * Rotates the given value to keep it between 0 and 360 degrees while maintaining its relative position.
	 * @param val The value you want constrained to 360 degree space.
	 * @return Returns the rotated value. 
	 */
	public static float rotateVal(float val) {
		while(val > 360) val -= 360;	// The number is reduced to below 360 degrees, in 360 degree increments
		while(val < 0) val += 360;		// or the number is increased to above 0 degrees, in 360 degree increments.
		return val;						// The adjusted value is returned.
	}
	
	/**
	 * Generates an RGBA color using a vector representing the H(X), S(Y), and V(Z), components of the vector. H should be between
	 * 0.0 and 360.0, S between 0.0 and 1.0, and V between 0.0 and 1.0.
	 * @param colorVector The vector to translate into a color.
	 * @return The newly generated color.
	 */
	public static Color HSVToRGB(Vector3 colorVector) {
		return HSVToRGB(colorVector.x, colorVector.y, colorVector.z);
	}
	
	/** 
	 * Generates an RGBA color using the given HSV values. Color is always fully opaque.
	 * @param h Color hue. Should be between 0 and 360.
	 * @param s Color saturation. Must be between 0and 1.
	 * @param v Color volume. Must be between 0 and 1.
	 * @return Returns the RGBA color generated from HSV values. 
	 */
	public static Color HSVToRGB(float h, float s, float v) {
		assert 0 <= s || s <= 1;				// Asserts that all legal saturations are between 0 and 1.
		assert 0 <= v || v <= 1;				// Asserts that all legal volumes are between 0 and 1.
		Color color = new Color(0, 0, 0, 1);	// Creates a blank black fully opaque color.
		
		float chroma = s * v;						// Finds chroma (the base value).
		if(h < 60) {								// Finds where in the spectrum of colors you are,
			color.r = chroma;						// Sets the primary color to chroma,
			color.g = chroma * (h / 60);			// and the secondary color to its linear derivative in the current color range.
		}											// The third color stays at 0.
		else if(h < 120) {							// There are 6 color bands in order Red, Yellow, Green, Cyan, Blue, Magenta.
			color.r = chroma * ((120 - h) / 60);	// Each if statement is different color band.
			color.g = chroma; 
		}
		else if(h < 180) {
			color.g = chroma;
			color.b = chroma * ((h - 120) / 60);
		}
		else if(h < 240) {
			color.g = chroma * ((240 - h) / 60);
			color.b = chroma;
		}
		else if(h < 300) {
			color.r = chroma * ((h - 240) / 60);
			color.b = chroma;
		}
		else {
			color.r = chroma;
			color.b = chroma * ((360 - h) / 60);
		}
		color.r += v - chroma;	// Saturates each color using the difference between volume and chroma.
		color.g += v - chroma;	//
		color.b += v - chroma;	//
		
		return color;	// Returns the newly generated color.
	}
	
	/** 
	 * Returns the fully opaque inverted form of a given color (opposite side of the color wheel).
	 * @param color The color you want to invert.
	 * @return The inverted form of the color. 
	 */
	public static Color invertColor(Color color) {
		return new Color(1 - color.r, 1 - color.g, 1 - color.b, 1);
	}
 
}
