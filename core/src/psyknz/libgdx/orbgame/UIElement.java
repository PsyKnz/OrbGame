package psyknz.libgdx.orbgame;

import psyknz.libgdx.architecture.*;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class UIElement extends InputAdapter {
	
	public static final int ADD_POINT_SPEED = 1000; // Number of points to add to the score total per second.
	public static final int NUMBERS_IN_SCORE = 8;	// Number of numbers which should be displayed in the score.
	public static final int HIGHSCORE_ENTRIES = 10; // Maximum number of highscores the game keeps saved.
	public static final float PANEL_PADDING = 10;	//
	
	private Sprite panelA, panelB, midLine;									// Sprites used to draw the top and bottom bars.
	private TextElement scoreLabel, highscoreLabel, scoreVal, highscoreVal; // Labels placed on the bars.
	
	private int[] highscores;			// Array containing all of the current highscore values.
	private String[] highscoreNames;	// Array containing all of the names tied to the respective highscores.
	
	private int score = 0;			// Players current score.
	private int pointsToAdd = 0;	// Number of points that need to be added to the score.
	
	public UIElement(GameScreen screen) {
		highscores = new int[HIGHSCORE_ENTRIES];		// Initialises the highscore array,
		highscoreNames = new String[HIGHSCORE_ENTRIES];	// and the array of corresponding names.
		loadHighscores();								// Loads saved highscores.
		
		Texture panelTex = screen.getGame().assets.get("white_circle.png", Texture.class);		// Loads the texture for drawing the panels.
		panelA = new Sprite(panelTex, panelTex.getWidth() / 2, panelTex.getHeight() / 2, 1, 1);	// Builds the sprite for panel A from a single pixel in the middle of the texture.
		panelA.setColor(Color.DARK_GRAY);														// Sets the color of the panel.
		panelB = new Sprite(panelA);															// Uses panel A as the template for panel B.
		
		BitmapFont font = screen.getGame().assets.get("kenpixel_blocks.ttf", BitmapFont.class);							// Loads the font to use for drawing the UI's text elements.
		scoreLabel = new TextElement("SCORE", font, 0, 0, TextElement.RIGHT, TextElement.CENTER);						// Generates the score label text,
		scoreVal = new TextElement(valToText(score), font, 0, 0, TextElement.RIGHT, TextElement.CENTER);				// the score text,
		highscoreLabel = new TextElement("HIGHSCORE", font, 0, 0, TextElement.RIGHT, TextElement.CENTER);				// the highscore label text,
		highscoreVal = new TextElement(valToText(highscores[0]), font, 0, 0, TextElement.RIGHT, TextElement.CENTER);	// and the highscore value text.
		
		midLine = new Sprite(panelA);
		midLine.setColor(Color.GREEN);
	}
	
	/**
	 * @param camera Camera object which will be drawing the UIElement. */
	public void setViewport(OrthographicCamera camera) {
		float vWidth = camera.viewportWidth;	// Finds the width of the viewport.
		float vHeight = camera.viewportHeight;	// Finds the height of the viewport.
		
		if(vWidth < vHeight) {								// If the viewport width is less than its height (portrait),
			float panelHeight = (vHeight - vWidth) / 2;		//
			panelA.setSize(vWidth, panelHeight);			//
			panelA.setPosition(0, vHeight - panelHeight);	//
			panelB.setSize(vWidth, panelHeight);			//
			panelB.setPosition(0, 0);						//
			
			midLine.setBounds(vWidth / 2, 0, 1, vHeight);
			
			Rectangle textZone = new Rectangle(0, 0, vWidth / 2 - 1.5f * PANEL_PADDING, panelHeight - 2 * PANEL_PADDING);	//
			
			highscoreLabel.scaleToFit(textZone, true);			//
			highscoreVal.setScale(highscoreLabel.getScale());	//
			scoreLabel.setScale(highscoreLabel.getScale());		//
			scoreVal.setScale(highscoreLabel.getScale());		//
			
			scoreLabel.setPosition(panelA.getWidth() / 2 - PANEL_PADDING / 2, panelA.getY() + panelA.getHeight() / 2);
			scoreVal.setPosition(panelA.getWidth() - PANEL_PADDING, panelA.getY() + panelA.getHeight() / 2);
			highscoreLabel.setPosition(panelB.getWidth() / 2 - PANEL_PADDING / 2, panelB.getY() + panelB.getHeight() / 2);
			highscoreVal.setPosition(panelB.getWidth() - PANEL_PADDING, panelB.getY() + panelB.getHeight() / 2);
		}
		else {											// Otherwise if the viewport height is less than its width (landscape),
			float panelWidth = (vWidth - vHeight) / 2;	//
			panelA.setSize(panelWidth, vHeight);		//
			panelA.setPosition(0, 0);					//
			panelB.setSize(panelWidth, vHeight);		//
			panelB.setPosition(0, vWidth - panelWidth);	//
		}
	}
	
	public void update(float delta) {
		if(pointsToAdd > 0) {										// If there are currently points which need to be added to the score,
			score += MathUtils.ceil(ADD_POINT_SPEED * delta);		// they are added at the rate determined by ADD_POINT_SPEED,
			pointsToAdd -= MathUtils.ceil(ADD_POINT_SPEED * delta);	// and subtracted from the points waiting to be added.
			if(pointsToAdd < 0) {									// Once too many points have been added,
				score += pointsToAdd;								// the score adds the negative number to balance,
				pointsToAdd = 0;									// and points waiting to be added is set to 0.
			}
		}		
		scoreVal.setText(valToText(score));	// Updates the score being displayed by the scoreVal label.
	}
	
	/** Function to increase the number of points the player has. 
	 * @param points The number of points to be added to the score. */
	public void addToScore(int points) {
		pointsToAdd += points;	// Increases the number of points waiting to be added to the score.
	}
	
	/** Function to convert integer values into string with a pre-defined number of characters. For example 1234 with 7 characters would
	 * produce the string "0001234". 
	 * @param val The value you want converted into text.
	 * @return The string produced by the function. */
	public static String valToText(int val) {
		String text = "";								// Creates a new string to hold what's generated.
		for(int i = NUMBERS_IN_SCORE; i > 0; i--) {	// For every character in the string,
			if(Math.pow(10, i) > val) text += "0";		// if the value is less than the a positive value which would go in this spot, a 0 is put in the string.
			else {										// Once the value is bigger,
				text += val;							// it fills what remains of the string,
				break;									// and the function stops.
			}
		}
		return text;	// Returns a reference to the string just created.
	}
	
	public void draw(SpriteBatch batch) {
		panelA.draw(batch);
		panelB.draw(batch);
		scoreLabel.draw(batch);
		scoreVal.draw(batch);
		highscoreLabel.draw(batch);
		highscoreVal.draw(batch);
		
		midLine.draw(batch);
	}
	
	/** Function to load highscores from the game preferences. */
	public void loadHighscores() {
		Preferences scoreData = Gdx.app.getPreferences("ScoreData");		// Accesses the ScoreData preferences information.
		for(int i = 0; i < HIGHSCORE_ENTRIES; i++) {						// For every highscore entry the game tracks,
			highscores[i] = scoreData.getInteger("highscore" + i, 0);		// The corresponding highscore is loaded, or set to 0 if empty.
			highscoreNames[i] = scoreData.getString("name" + i, "Unknown");	// corresponding names are loaded as well, or set to "unknown".
		}
	}
	
	/** Function to save highscores to the game preferences */
	public void saveHighscores() {
		Preferences scoreData = Gdx.app.getPreferences("ScoreData");	// Accesses the ScoreData preferences information.
		for(int i = 0; i < HIGHSCORE_ENTRIES; i++) {					// For every highscore entry the game tracks,
			scoreData.putInteger("highscore" + i, highscores[i]);		// the highscore is saved into the preferences,
			scoreData.putString("name" + i, highscoreNames[i]);			// as is the corresponding name.
		}
	}
}
