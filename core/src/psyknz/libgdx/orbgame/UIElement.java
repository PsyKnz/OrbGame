package psyknz.libgdx.orbgame;

import psyknz.libgdx.architecture.*;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
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
	
	private InputMultiplexer input;				//
	private OrthographicCamera camera = null;	//
	
	private Sprite panelA, panelB;											// Sprites used to draw the top and bottom bars.
	private TextElement scoreLabel, highscoreLabel, scoreVal, highscoreVal; // Labels placed on the bars.
	private GameButton pauseButton;											// Button used to pause the game.
	
	private int[] highscores;			// Array containing all of the current highscore values.
	private String[] highscoreNames;	// Array containing all of the names tied to the respective highscores.
	
	private int score = 0;			// Players current score.
	private int pointsToAdd = 0;	// Number of points that need to be added to the score.
	
	private GameMessage suspendMessage = null;	//
	
	public UIElement(GameScreen screen, InputMultiplexer input) {
		this.input = input;
		
		highscores = new int[HIGHSCORE_ENTRIES];		// Initialises the highscore array,
		highscoreNames = new String[HIGHSCORE_ENTRIES];	// and the array of corresponding names.
		loadHighscores();								// Loads saved highscores.
		
		Texture panelTex = screen.getGame().assets.get("white_circle.png", Texture.class);		// Loads the texture for drawing the panels.
		panelA = new Sprite(panelTex, panelTex.getWidth() / 2, panelTex.getHeight() / 2, 1, 1);	// Builds the sprite for panel A from a single pixel in the middle of the texture.
		panelA.setColor(Color.DARK_GRAY);														// Sets the color of the panel.
		panelB = new Sprite(panelA);															// Uses panel A as the template for panel B.
		
		pauseButton = new GameButton(new Sprite(panelA)) {
			@Override
			public void buttonAction() {
				displayMessage("Game Paused");
			}
		};
		pauseButton.sprite.setColor(Color.GRAY);
		input.addProcessor(pauseButton);
		
		BitmapFont font = screen.getGame().assets.get("kenpixel_blocks.ttf", BitmapFont.class);							// Loads the font to use for drawing the UI's text elements.
		scoreLabel = new TextElement("SCORE", font, 0, 0, TextElement.RIGHT, TextElement.CENTER);						// Generates the score label text,
		scoreVal = new TextElement(valToText(score), font, 0, 0, TextElement.RIGHT, TextElement.CENTER);				// the score text,
		highscoreLabel = new TextElement("HIGHSCORE", font, 0, 0, TextElement.RIGHT, TextElement.CENTER);				// the highscore label text,
		highscoreVal = new TextElement(valToText(highscores[0]), font, 0, 0, TextElement.RIGHT, TextElement.CENTER);	// and the highscore value text.
	}
	
	/**
	 * @param camera Camera object which will be drawing the UIElement. */
	public void setViewport(OrthographicCamera camera) {
		this.camera = camera;	//
		
		if(camera.viewportWidth < camera.viewportHeight) {							// If the camera is in portrait,
			float panelHeight = (camera.viewportHeight - camera.viewportWidth) / 2;	//
			panelA.setSize(camera.viewportWidth, panelHeight);						//
			panelA.setPosition(0, camera.viewportHeight - panelHeight);				//
			panelB.setSize(camera.viewportWidth, panelHeight);						//
			panelB.setPosition(0, 0);												//
			
			pauseButton.camera = camera;
			pauseButton.sprite.setBounds(PANEL_PADDING, panelA.getY() + PANEL_PADDING, panelHeight - PANEL_PADDING * 2, panelHeight - PANEL_PADDING * 2);	//
			
			Rectangle textZone = new Rectangle(0, 0, camera.viewportWidth / 2 - 1.5f * PANEL_PADDING, panelHeight - 2 * PANEL_PADDING);	//
			
			highscoreLabel.scaleToFit(textZone, true);			//
			highscoreVal.setScale(highscoreLabel.getScale());	//
			scoreLabel.setScale(highscoreLabel.getScale());		//
			scoreVal.setScale(highscoreLabel.getScale());		//
			
			scoreLabel.setPosition(panelA.getWidth() / 2 - PANEL_PADDING / 2, panelA.getY() + panelA.getHeight() / 2);
			scoreVal.setPosition(panelA.getWidth() - PANEL_PADDING, panelA.getY() + panelA.getHeight() / 2);
			highscoreLabel.setPosition(panelB.getWidth() / 2 - PANEL_PADDING / 2, panelB.getY() + panelB.getHeight() / 2);
			highscoreVal.setPosition(panelB.getWidth() - PANEL_PADDING, panelB.getY() + panelB.getHeight() / 2);
			
			if(suspendMessage != null) suspendMessage.setBounds(camera.position.x - camera.viewportWidth / 2, camera.position.y - (camera.viewportWidth * 0.2f) / 2, camera.viewportWidth, camera.viewportWidth * 0.2f);
		}
		else {																		// Otherwise if the camera is in landscape,
			float panelWidth = (camera.viewportWidth - camera.viewportHeight) / 2;	//
			panelA.setSize(panelWidth, camera.viewportHeight);						//
			panelA.setPosition(0, 0);												//
			panelB.setSize(panelWidth, camera.viewportHeight);						//
			panelB.setPosition(camera.viewportWidth - panelWidth, 0);				//
			
			if(suspendMessage != null) suspendMessage.setBounds(camera.position.x - camera.viewportWidth / 2, camera.position.y - (camera.viewportHeight * 0.2f) / 2, camera.viewportWidth, camera.viewportHeight * 0.2f);
		}
	}
	
	public boolean update(float delta) {
		if(pointsToAdd > 0) {										// If there are currently points which need to be added to the score,
			score += MathUtils.ceil(ADD_POINT_SPEED * delta);		// they are added at the rate determined by ADD_POINT_SPEED,
			pointsToAdd -= MathUtils.ceil(ADD_POINT_SPEED * delta);	// and subtracted from the points waiting to be added.
			if(pointsToAdd < 0) {									// Once too many points have been added,
				score += pointsToAdd;								// the score adds the negative number to balance,
				pointsToAdd = 0;									// and points waiting to be added is set to 0.
			}
		}		
		scoreVal.setText(valToText(score));	// Updates the score being displayed by the scoreVal label.
		
		if(suspendMessage != null) return true;
		
		return false;
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
		for(int i = NUMBERS_IN_SCORE - 1; i >= 0; i--) {// For every character in the string,
			if(Math.pow(10, i) > val) text += "0";		// if the value is less than the a positive value which would go in this spot, a 0 is put in the string.
			else {										// Once the value is bigger,
				text += val;							// it fills what remains of the string,
				break;									// and the function stops.
			}
		}
		return text;	// Returns a reference to the string just created.
	}
	
	public void draw(SpriteBatch batch) {
		panelA.draw(batch);	// First draws the UI panels.
		panelB.draw(batch);	//
		
		scoreLabel.draw(batch);		// Then draws the score labels,
		scoreVal.draw(batch);		// and values.
		highscoreLabel.draw(batch);	//
		highscoreVal.draw(batch);	//
		
		pauseButton.draw(batch);	// Draws the pause butto to screen last.
		
		if(suspendMessage != null) suspendMessage.draw(batch);
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
	
	/** Adds the current score to the highscore table.
	 * @return The rank the score added acheived in the highscore table, or the size of the table if it didn't rank. */
	public int addScoreToHighscores() {
		for(int i = 0; i < HIGHSCORE_ENTRIES; i++) {				// Starts with the hihest highscore and works backwards.
			if (score > highscores[i]) {							// If the current score is bigger than a score in the list,
				for(int j = HIGHSCORE_ENTRIES - 1; j > i; j--) {	// then starting with the lowest score,
					highscores[j] = highscores[j - 1];				// all scores after it are replaced by the score infront of them to make room.
				}
				highscores[i] = score;	// Stores the new highscore.
				return i;				// Returns the index of rank of the score.
			}
		}
		return HIGHSCORE_ENTRIES;	// If the score doesn't rate on the highscores then the max number of entries is returned.
	}
	
	public void displayMessage(String text) {
		Sprite msgBackgroundSpr = new Sprite(panelA);
		msgBackgroundSpr.setColor(Color.BLACK);
		msgBackgroundSpr.setAlpha(0.75f);
		
		TextElement message = new TextElement(text, scoreLabel.getFont(), 0, 0);
		suspendMessage = new GameMessage(msgBackgroundSpr, message, this);
		input.addProcessor(0, suspendMessage);
		
		if(camera != null) {
			if(camera.viewportWidth > camera.viewportHeight) suspendMessage.setBounds(camera.position.x - camera.viewportWidth / 2, camera.position.y - (camera.viewportHeight * 0.2f) / 2, camera.viewportWidth, camera.viewportHeight * 0.2f);
			else suspendMessage.setBounds(camera.position.x - camera.viewportWidth / 2, camera.position.y - (camera.viewportWidth * 0.2f) / 2, camera.viewportWidth, camera.viewportWidth * 0.2f);
		}
	}
	
	public void removeMessage(GameMessage message) {
		input.removeProcessor(message);
		suspendMessage = null;
	}
}
