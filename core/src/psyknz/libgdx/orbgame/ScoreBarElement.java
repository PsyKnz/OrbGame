package psyknz.libgdx.orbgame;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class ScoreBarElement {
	
	public static final int ADD_POINT_SPEED = 1000; // Number of points to add to the score total per second.
	public static final int NUMBERS_IN_SCORE = 7;	// Number of numbers which should be displayed in the score.
	public static final int HIGHSCORE_ENTRIES = 10; // Maximum number of highscores the game keeps saved.
	
	private Sprite topBarSpr, bottomBarSpr; 								// Sprites used to draw the top and bottom bars.
	private TextElement scoreLabel, highscoreLabel, scoreVal, highscoreVal; // Labels placed on the bars.
	
	private Preferences scoreData; 		// LibGDX preferences containing the highscores.
	private int[] highscores;			// Array containing all of the current highscore values.
	private String[] highscoreNames;	// Array containing all of the names tied to the respective highscores.
	
	private int score = 0;			// Players current score.
	private int pointsToAdd = 0;	// Number of points that need to be added to the score.
	
	public ScoreBarElement(Sprite top, Sprite bottom, BitmapFont font) {
		loadHighscores();
		
		topBarSpr = top;		// Sets the sprite for the top bar,
		bottomBarSpr = bottom; 	// and for the bottom bar.
		
		scoreLabel = new TextElement("SCORE:", font, topBarSpr.getX(), topBarSpr.getY());
		scoreLabel.setAlignment(TextElement.LEFT, TextElement.BOTTOM);
		scoreVal = new TextElement("0000000", font, topBarSpr.getX() + topBarSpr.getWidth(), topBarSpr.getY());
		scoreVal.setAlignment(TextElement.RIGHT, TextElement.BOTTOM);
		highscoreLabel = new TextElement("HIGHSCORE:", font, bottomBarSpr.getX(), bottomBarSpr.getY());
		highscoreLabel.setAlignment(TextElement.LEFT, TextElement.BOTTOM);
		highscoreVal = new TextElement(valToText(highscores[0]), font, bottomBarSpr.getX() + bottomBarSpr.getWidth(), bottomBarSpr.getY());
		highscoreVal.setAlignment(TextElement.RIGHT, TextElement.RIGHT);
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
		for(int i = NUMBERS_IN_SCORE - 1; i > 0; i--) {	// For every character in the string,
			if(Math.pow(10, i) > val) text += "0";		// if the value is less than the a positive value which would go in this spot, a 0 is put in the string.
			else {										// Once the value is bigger,
				text += val;							// it fills what remains of the string,
				break;									// and the function stops.
			}
		}
		return text;	// Returns a reference to the string just created.
	}
	
	public void draw(SpriteBatch batch) {
		topBarSpr.draw(batch);
		bottomBarSpr.draw(batch);
		scoreLabel.draw(batch);
		scoreVal.draw(batch);
		highscoreLabel.draw(batch);
		highscoreVal.draw(batch);
	}
	
	/** Function to load highscores from the game preferences. */
	public void loadHighscores() {
		scoreData = Gdx.app.getPreferences("ScoreData");					// Accesses the ScoreData preferences information.
		highscores = new int[HIGHSCORE_ENTRIES];							// Initialises the highscore array,
		highscoreNames = new String[HIGHSCORE_ENTRIES];						// and the array of related names.
		for(int i = 0; i < HIGHSCORE_ENTRIES; i++) {						// For every highscore entry the game tracks,
			highscores[i] = scoreData.getInteger("highscore" + i, 0);		// The corresponding highscore is loaded, or set to 0 if empty.
			highscoreNames[i] = scoreData.getString("name" + i, "Unknown");	// corresponding names are loaded as well, or set to "unknown".
		}
	}
}
