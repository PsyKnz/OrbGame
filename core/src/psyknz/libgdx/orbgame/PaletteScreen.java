package psyknz.libgdx.orbgame;

import psyknz.libgdx.architecture.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class PaletteScreen extends GameScreen {
	
	public static final int COLOR_WIDTH = 360;
	public static final int COLOR_HEIGHT = 100;
	public static final int PANEL_SPACE = 50;
	
	private Sprite[][] sprA, sprB;
	
	private float currentVolume = 1;
	private int oldY = Gdx.input.getY();
	
	public PaletteScreen(GameCore game) {
		super(game);
		
		Texture tex = game.assets.get("white_circle.png", Texture.class);
		Sprite spr = new Sprite(tex, tex.getWidth() / 2, tex.getHeight() / 2, 1, 1);
		
		sprA = new Sprite[COLOR_WIDTH][COLOR_HEIGHT];
		sprB = new Sprite[COLOR_WIDTH][COLOR_HEIGHT];
		
		for(int width = 0; width < COLOR_WIDTH; width++) {
			for(int height = 0; height < COLOR_HEIGHT; height++) {
				sprA[width][height] = new Sprite(spr);
				sprB[width][height] = new Sprite(spr);
			}
		}
		
		updatePalettes(currentVolume);
		input.addProcessor(this);
	}
	
	public void resize(int width, int height) {
		super.resize(width, height);
		
		Vector2 posA = new Vector2(camera.position.x - COLOR_WIDTH / 2, camera.position.y - COLOR_HEIGHT - PANEL_SPACE);
		Vector2 posB = new Vector2(camera.position.x - COLOR_WIDTH / 2, camera.position.y + PANEL_SPACE);
		
		for(int wid = 0; wid < COLOR_WIDTH; wid++) {
			for(int hei = 0; hei < COLOR_HEIGHT; hei++) {
				sprA[wid][hei].setPosition(posA.x + wid, posA.y + hei);
				sprB[wid][hei].setPosition(posB.x + wid, posB.y + hei);
			}
		}
	}
	
	private void updatePalettes(float volume) {
		float saturation;
		for(int width = 0; width < COLOR_WIDTH; width++) {
			for(int height = 0; height < COLOR_HEIGHT; height++) {
				saturation = height * 0.01f;
				//sprA[width][height].setColor(GamePalette.HSVToRGB(width, saturation, volume));
				//sprB[width][height].setColor(GamePalette.HSVToRGB(width, saturation, volume));
			}
		}
	}
	
	@Override
	public void draw(SpriteBatch batch, Rectangle rect) {
		for(int width = 0; width < COLOR_WIDTH; width++) {
			for(int height = 0; height < COLOR_HEIGHT; height++) {
				sprA[width][height].draw(batch);
				sprB[width][height].draw(batch);
			}
		}
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(oldY < screenY && currentVolume < 1f) currentVolume += 0.1f;
		else if(oldY > screenY && currentVolume > 0f) currentVolume -= 0.1f;
		oldY = screenY;
		updatePalettes(currentVolume);
		return true;
	}

}
