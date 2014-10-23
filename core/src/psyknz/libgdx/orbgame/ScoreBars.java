package psyknz.libgdx.orbgame;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class ScoreBars {
	
	public static final int PLACES_IN_SCORE = 7;		// Number of numerical places which should be displayed in the score.
	public static final float PADDING_RATIO = 0.15f;	// Proportion of the score bar which should be used for padding between elements (based on the shortest edge).
	
	private UIElement ui;
	
	private Sprite bgTop, bgBottom;
	private TextElement highscoreLabel, scoreLabel;
	public final TextElement highscoreVal, scoreVal;
	private GameButton pauseButton;
	
	public ScoreBars(UIElement uiElement, Sprite background, BitmapFont font) {
		ui = uiElement;
		bgTop = new Sprite(background);
		bgTop.setColor(Color.DARK_GRAY);
		bgBottom = new Sprite(bgTop);
		
		pauseButton = new GameButton(background) {
			@Override
			public void buttonAction() {
				ui.pauseGame();
			}
		};		
		pauseButton.sprite.setColor(Color.GRAY);
		ui.input.addProcessor(pauseButton);
		
		highscoreLabel = new TextElement("HIGHSCORE", font, 0, 0, TextElement.LEFT, TextElement.CENTER);
		scoreLabel = new TextElement("SCORE", font, 0, 0, TextElement.RIGHT, TextElement.CENTER);
		highscoreVal = new TextElement(UIElement.valToText(0, PLACES_IN_SCORE), font, 0, 0, TextElement.RIGHT, TextElement.CENTER);
		scoreVal = new TextElement(UIElement.valToText(0, PLACES_IN_SCORE), font, 0, 0, TextElement.RIGHT, TextElement.CENTER);
	}
	
	public void setCamera(Camera camera) {		
		if(camera.viewportWidth <= camera.viewportHeight) {
			bgTop.setSize(camera.viewportWidth, (camera.viewportHeight - camera.viewportWidth) / 2);
			bgTop.setPosition(camera.position.x - camera.viewportWidth / 2, camera.position.y + camera.viewportHeight / 2 - bgTop.getHeight());
			bgBottom.setSize(bgTop.getWidth(), bgTop.getHeight());
			bgBottom.setPosition(bgTop.getX(), camera.position.y - camera.viewportHeight / 2);
			
			float padding = bgTop.getHeight() * PADDING_RATIO;
			
			pauseButton.sprite.setSize(bgTop.getHeight() - padding * 2, bgTop.getHeight() - padding * 2);
			pauseButton.sprite.setPosition(bgTop.getX() + padding, bgTop.getY() + padding);
			
			highscoreLabel.setScale(1, 1);
			highscoreVal.setScale(1, 1);
			float scalingFactor = (bgTop.getWidth() - padding * 3) / (highscoreLabel.getBounds().width + highscoreVal.getBounds().width);
			highscoreLabel.setScale(scalingFactor, scalingFactor);
			highscoreVal.setScale(scalingFactor, scalingFactor);
			scoreLabel.setScale(scalingFactor, scalingFactor);
			scoreVal.setScale(scalingFactor, scalingFactor);
			
			highscoreLabel.setPosition(bgBottom.getX() + padding, bgBottom.getY() + bgBottom.getHeight() / 2);
			highscoreVal.setPosition(bgBottom.getX() + bgBottom.getWidth() - padding, highscoreLabel.getPosition().y);
			scoreVal.setPosition(highscoreVal.getPosition().x, bgTop.getY() + bgTop.getHeight() / 2);
			scoreLabel.setPosition(scoreVal.getBounds().x - padding, scoreVal.getPosition().y);
		}
		else {
			// Positions the ScoreBar against the left edge of the screen.
			bgTop.setSize((camera.viewportWidth - camera.viewportHeight) / 2, camera.viewportHeight);
			bgTop.setPosition(camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2);
			bgBottom.setSize(bgTop.getWidth(), bgTop.getHeight());
			bgBottom.setPosition(camera.viewportWidth - bgBottom.getWidth(), bgTop.getY());
			
			float padding = bgTop.getWidth() * PADDING_RATIO;
			
			pauseButton.sprite.setSize(bgTop.getWidth() - padding * 2, bgTop.getWidth() - padding * 2);
			pauseButton.sprite.setPosition(bgTop.getX() + padding, bgTop.getY() - padding - pauseButton.sprite.getHeight());
			
			/* Rectangle textArea = new Rectangle(0, 0, (background.getWidth() - padding * 3) / 2, 
					(background.getHeight() - pauseButton.sprite.getHeight() - padding * 4) / 2);
			
			Need to work out how to re-orient text to vertical before writing this section of code. */
		}
		
		pauseButton.camera = camera;	// Sets the camera used to draw the Pause Button.
	}
	
	public void draw(SpriteBatch batch) {
		bgTop.draw(batch);
		bgBottom.draw(batch);
		pauseButton.draw(batch);
		highscoreLabel.draw(batch);
		scoreLabel.draw(batch);
		highscoreVal.draw(batch);
		scoreVal.draw(batch);
	}

}
