package psyknz.libgdx.orbgame.debug;

import psyknz.libgdx.orbgame.misc.CameraController;
import psyknz.libgdx.orbgame.misc.GamePalette;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class PaletteDebugger extends InputAdapter implements Disposable {
	
	public static final float MAX_HUE = 360;
	public static final float MAX_SATURATION = 100;
	public static final float MAX_VOLUME = 100;
	public static final int NUM_COLORS = 5;
	
	private Sprite spr;						// Reference to a 1x1 pixel sprite used to draw the colours.
	private SpriteBatch batch;				// Reference to the sprite batch used to draw the palette to the screen.
	private OrthographicCamera camera;		// Reference to the camera used to draw the palette.
	private CameraController camControl;	// Reference to the camera controller that sets the camera.
	
	private Rectangle[] colorBounds;
	private Vector3[] colorVectors;
	private int selectedBounds;
	
	private float currentVolume = 100;
	
	public PaletteDebugger(GamePalette palette, AssetManager assets) {
		spr = new Sprite(assets.get("white_circle.png", Texture.class), 32, 32, 1, 1);
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camControl = new CameraController(camera, 380, 700, CameraController.FIT_TO_SCREEN);
		
		colorVectors = new Vector3[NUM_COLORS];
		for(int i = 0; i < NUM_COLORS; i++) {
			colorVectors[i] = new Vector3(0, 1, 1);
		}
		
		colorBounds = new Rectangle[NUM_COLORS + 1];
		for(int i = 0; i < NUM_COLORS; i++) {
			colorBounds[i] = new Rectangle(0 - MAX_HUE / 2 + i * MAX_HUE / NUM_COLORS, 120 - MAX_SATURATION / 2,
					MAX_HUE / NUM_COLORS, MAX_HUE / NUM_COLORS);
		}
		colorBounds[NUM_COLORS] = new Rectangle(0 - MAX_HUE / 2, 0 - MAX_SATURATION / 2, MAX_HUE, MAX_SATURATION);
	}
	
	public void resize(int width, int height) {
		camControl.resize(width, height);
	}

	public void draw() {
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		spr.setSize(colorBounds[0].width, colorBounds[0].height);
		for(int i = 0; i < NUM_COLORS; i++) {
			spr.setColor(GamePalette.HSVToRGB(colorVectors[i].x, colorVectors[i].y, colorVectors[i].z));
			spr.setPosition(colorBounds[i].x, colorBounds[i].y);
			spr.draw(batch);
		}
		
		spr.setSize(1, 1);
		for(float y = 0; y < MAX_SATURATION; y++) {
			for(float x = 0; x < MAX_HUE; x++) {
				spr.setColor(GamePalette.HSVToRGB(x, y / MAX_SATURATION, currentVolume / MAX_VOLUME));
				spr.setPosition(colorBounds[NUM_COLORS].x + x, colorBounds[NUM_COLORS].y + y);
				spr.draw(batch);
			}
		}
		
		spr.setColor(Color.WHITE);
		for(float y = -2; y <= colorBounds[selectedBounds].height + 1; y++) {
			for(float x = -2; x <= colorBounds[selectedBounds].width + 1; x++) {
				if((x < 0 || x >= colorBounds[selectedBounds].width) || (y < 0 || y >= colorBounds[selectedBounds].height)) {
					spr.setPosition(colorBounds[selectedBounds].x + x, colorBounds[selectedBounds].y + y);
					spr.draw(batch);
				}
			}
		}
		
		spr.setColor(Color.BLACK);
		for(int i = 0; i < colorVectors.length; i++) {
			for(int y = -1; y < 2; y++) {
				for(int x = -1; x < 2; x++) {
					if(x != 0 || y != 0) {
						spr.setPosition(colorVectors[i].x + x + colorBounds[NUM_COLORS].x, 
								colorVectors[i].y * MAX_SATURATION + y + colorBounds[NUM_COLORS].y);
						spr.draw(batch);
					}
				}
			}
		}
		
		batch.end();
	}

	@Override
	public void dispose() {
		batch.dispose();
	}
	
	@Override
	public boolean scrolled(int amount) {
		if(selectedBounds < NUM_COLORS) {
			float fAmount = (float) amount / MAX_VOLUME;
			colorVectors[selectedBounds].z += fAmount;
			if(colorVectors[selectedBounds].z > 1) colorVectors[selectedBounds].z = 1;
			else if(colorVectors[selectedBounds].z < 0) colorVectors[selectedBounds].z = 0;
			return true;
		}
		
		currentVolume += amount;
		if(currentVolume > MAX_VOLUME) currentVolume = MAX_VOLUME;
		else if(currentVolume < 0) currentVolume = 0;
		return true;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Vector3 touch = new Vector3(screenX, screenY, 0);
		camera.unproject(touch);
		
		for(int i = 0; i < NUM_COLORS; i++) {
			if(colorBounds[i].contains(touch.x,  touch.y)) {
				selectedBounds = i;
				return true;
			}
		}
		
		if(selectedBounds < NUM_COLORS && colorBounds[NUM_COLORS].contains(touch.x, touch.y)) {
			colorVectors[selectedBounds].x = touch.x - colorBounds[NUM_COLORS].x;
			colorVectors[selectedBounds].y = (touch.y - colorBounds[NUM_COLORS].y) / MAX_SATURATION;
			colorVectors[selectedBounds].z = currentVolume / MAX_VOLUME;
			return true;
		}
		
		selectedBounds = NUM_COLORS;
		return false;
	}

}
