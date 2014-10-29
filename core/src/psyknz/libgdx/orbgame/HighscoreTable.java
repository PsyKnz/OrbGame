package psyknz.libgdx.orbgame;

import psyknz.libgdx.orbgame.tweenaccessors.ColorTween;
import aurelienribon.tweenengine.Tween;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class HighscoreTable extends InputAdapter implements GameMessage {
	
	public static final float SCREEN_PADDING = 0.1f;	// Proportion of blank space between the table and edges of the screen.
	public static final int MAX_CHARACTERS = 10;		// Maximum number of characters allowed in a highscore name.
	
	private UIElement ui;			// Reference to the UIElement for the game.
	private HighscoreSystem scores;	// Reference to the Highscore information for the game.
	
	private Sprite bg;											// Sprite used to draw the background for the table.
	private TextElement title;											// TextElement displaying the tables title.
	private TextElement[] highscoreNums, highscoreNames, highscores;	// TextElements for all the score information.
	
	private String entryToEdit = null;	// Reference to the string for the highscore name being edited.
	private int entryPosition;			// Position of the highscore name being editied on the table.
	private Tween entryTween;			// Reference to the tween making the new highscore flash.
	
	/** Creates a new highscore table to display score information.
	 * @param background The sprite to use when drawing the tables background.
	 * @param ui The UI element which is managing this table.
	 * @param scores The score information you want presented in the highscore table. */
	public HighscoreTable(Sprite background, UIElement ui, HighscoreSystem scores) {
		this.bg = background;																	// Sets the tables background sprite.
		title = new TextElement("Highscores", ui.uiFont, 0, 0, TextElement.CENTER, TextElement.TOP);	// Creates its title.
		
		highscoreNums = new TextElement[scores.getHighscoreEntries()];	// Initialises the array for highscore position info.
		highscoreNames = new TextElement[scores.getHighscoreEntries()];	// Initialises the array for highscore names.
		highscores = new TextElement[scores.getHighscoreEntries()];		// Initialises the array for highscores.
		
		/* Every array for highscore information is filled with data from the referenced HighscoreSystem. */
		for(int i = 0; i < scores.getHighscoreEntries(); i++) {
			highscoreNums[i] = new TextElement(i + 1 + ".", ui.uiFont, 0, 0, TextElement.LEFT, TextElement.TOP);
			highscoreNames[i] = new TextElement(scores.getHighscoreName(i), ui.uiFont, 0, 0, TextElement.LEFT, TextElement.TOP);
			highscores[i] = new TextElement(UIElement.valToText(scores.getHighscore(i), ScoreBars.PLACES_IN_SCORE), 
					ui.uiFont, 0, 0, TextElement.RIGHT, TextElement.TOP);
		}
		
		this.scores = scores;	// Sets the score information this table is displaying.
		this.ui = ui;			// Sets the UI managing this table.
	}
	
	@Override
	public void setCamera(Camera camera) {
		/* Determines how much padding there should be between the table and the screen by looking at the shortest edge. */
		float padding;
		if(camera.viewportWidth <= camera.viewportHeight) padding = camera.viewportWidth * SCREEN_PADDING;
		else padding = camera.viewportHeight * SCREEN_PADDING;
		
		bg.setSize(camera.viewportWidth - padding * 2, camera.viewportHeight - padding * 2); 	// Sets the size of the background sprite.
		bg.setCenter(camera.position.x, camera.position.y);										// Places the background in the center of the screen.
		
		// Determines padding between elements horizontally by dividing the width by the amount of space required to fit the maximum number of character in a line.
		float hPadding = bg.getWidth() / (MAX_CHARACTERS + ScoreBars.PLACES_IN_SCORE + 7);
		// Determines padding between elements vertically by dividing the height by the number of entries factoring in the title and padding the same height as entries.
		float vPadding = bg.getHeight() / ((highscoreNums.length + 2) * 2);
		
		title.scaleToFit(bg.getWidth() - hPadding * 2, vPadding * 2, true);							// Scales the title to fit at the top of the table,
		title.setPosition(bg.getX() + bg.getWidth() / 2, bg.getY() + bg.getHeight() - vPadding);	// and centers it.
		
		float yPos;
		for(int i = 0; i < highscoreNums.length; i++) {																// Every highscore entry has its size and position assessed.
			yPos = bg.getY() + bg.getHeight() - title.getBounds().getHeight() - vPadding * 2 - vPadding * 2 * i;	// Finds the y position entries at i should be placed.
			
			highscoreNums[i].scaleToFit(hPadding * 3, vPadding, true);	// Gives the highscore numbers enough space to fit 3 characters.
			highscoreNums[i].setPosition(bg.getX() + hPadding, yPos);	// And places them hPadding away from the left edge of the table.
			
			highscoreNames[i].scaleToFit(hPadding * MAX_CHARACTERS, vPadding, true);	// Gives the highscore names enough space to fit MAX_CHARACTERS.
			highscoreNames[i].setPosition(bg.getX() + hPadding * 5, yPos);				// And places them hPadding away from the rightmost edge of the numbers.
			
			highscores[i].scaleToFit(hPadding * (ScoreBars.PLACES_IN_SCORE), vPadding, true);	// Gives the highscores enough space to fit the maximum number of places recorded in a score.
			highscores[i].setPosition(bg.getX() + bg.getWidth() - hPadding, yPos);				// And place them hPadding away from the right edge of the table.
		}
	}
	
	@Override
	public void draw(SpriteBatch batch) {
		bg.draw(batch);
		title.draw(batch);
		for(int i = 0; i < highscoreNums.length; i++) {
			highscoreNums[i].draw(batch);
			highscoreNames[i].draw(batch);
			highscores[i].draw(batch);
		}
	}
	
	public void editHighscoreName(int index) {
		Gdx.input.setOnscreenKeyboardVisible(true);
		Color entryColor = new Color(Color.GREEN);
		highscoreNums[index].color = entryColor;
		highscoreNames[index].color = entryColor;
		entryTween = Tween.to(highscoreNames[index].color, ColorTween.COLOR_ALPHA, 0.5f)
				.target(0.2f).repeatYoyo(-1, 0).start(ui.manager);
		highscores[index].color = entryColor;
		entryToEdit = highscoreNames[index].getText();
		entryPosition = index;
	}
	
	@Override
	public boolean keyUp(int keyCode) {
		if(entryToEdit != null) {
			if(keyCode == Input.Keys.BACKSPACE && entryToEdit.length() > 0) {
				entryToEdit = entryToEdit.substring(0, entryToEdit.length() - 2);
				highscoreNames[entryPosition].setText(entryToEdit);
			}
			else if(keyCode == Input.Keys.ENTER) {
				entryTween.kill();
				highscoreNames[entryPosition].color.a = 1;
				scores.defaultName = entryToEdit;
				scores.setHighscoreName(entryToEdit, entryPosition);
				scores.saveHighscores();
				entryToEdit = null;
				Gdx.input.setOnscreenKeyboardVisible(false);
				ui.enableInput("Tap to play again");
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean keyTyped(char character) {
		if(entryToEdit != null) {
			if(entryToEdit.length() < MAX_CHARACTERS) {
				entryToEdit += character;
				highscoreNames[entryPosition].setText(entryToEdit);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(entryToEdit == null) ui.removeMessage(this);
		return true;
	}
	
	@Override
	public Rectangle getBounds() {
		return bg.getBoundingRectangle();
	}
}
