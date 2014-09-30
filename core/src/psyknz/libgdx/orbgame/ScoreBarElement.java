package psyknz.libgdx.orbgame;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class ScoreBarElement {
	
	public static final int ADD_POINT_SPEED = 1000; // Adds 100 points to the total per second.
	public static final int NUMBERS_IN_SCORE = 7;	// Number of numbers which should be displayed in the score.
	
	private Sprite topBarSpr, bottomBarSpr;
	
	private TextElement scoreLabel, highscoreLabel, scoreVal, highscoreVal;
	
	private int score = 0;
	private int pointsToAdd = 0;
	
	public ScoreBarElement(Sprite top, Sprite bottom, BitmapFont font) {
		topBarSpr = top;		//
		bottomBarSpr = bottom; 	//		
		scoreLabel = new TextElement("SCORE:", font, topBarSpr.getX(), topBarSpr.getY());
		scoreLabel.setAlignment(TextElement.LEFT, TextElement.BOTTOM);
		scoreVal = new TextElement("0000000", font, topBarSpr.getX() + topBarSpr.getWidth(), topBarSpr.getY());
		scoreVal.setAlignment(TextElement.RIGHT, TextElement.BOTTOM);
		highscoreLabel = new TextElement("HIGHSCORE:", font, bottomBarSpr.getX(), bottomBarSpr.getY());
		highscoreLabel.setAlignment(TextElement.LEFT, TextElement.BOTTOM);
		highscoreVal = new TextElement(valToText(getHighscore()), font, bottomBarSpr.getX() + bottomBarSpr.getWidth(), bottomBarSpr.getY());
		highscoreVal.setAlignment(TextElement.RIGHT, TextElement.RIGHT);
	}
	
	public void update(float delta) {
		if(pointsToAdd > 0) {
			score += MathUtils.ceil(ADD_POINT_SPEED * delta);
			pointsToAdd -= MathUtils.ceil(ADD_POINT_SPEED * delta);
			if(pointsToAdd < 0) {
				score += pointsToAdd;
				pointsToAdd = 0;
			}
		}
		
		scoreVal.setText(valToText(score));
	}
	
	public void addToScore(int points) {
		pointsToAdd += points;
	}
	
	public static String valToText(int val) {
		String text = "";
		for(int i = NUMBERS_IN_SCORE - 1; i > 0; i--) {
			if(Math.pow(10, i) > val) text += "0";
			else {
				text += val;
				break;
			}
		}
		return text;
	}
	
	public void draw(SpriteBatch batch) {
		topBarSpr.draw(batch);
		bottomBarSpr.draw(batch);
		scoreLabel.draw(batch);
		scoreVal.draw(batch);
		highscoreLabel.draw(batch);
		highscoreVal.draw(batch);
	}
	
	private int getHighscore() {
		return 0;
	}

}
