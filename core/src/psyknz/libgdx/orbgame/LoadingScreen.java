package psyknz.libgdx.orbgame;

import psyknz.libgdx.orbgame.tweenaccessors.*;

import psyknz.libgdx.architecture.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import aurelienribon.tweenengine.Tween;

public class LoadingScreen extends GameScreen {
	
	private TextElement loading;	// TextElement displaying loading information.
	
	/** Creates a new loading screen which loads all assets used by the OrbGame.
	 * @param game The GameCore object managing this screen. */
	public LoadingScreen(GameCore game) {
		super(game);
		
		FreeTypeFontLoader.FreeTypeFontParameter p = new FreeTypeFontLoader.FreeTypeFontParameter();	// Creates a new FreeType Font
		p.size = 72;	// Loader parameter file and sets the size of the font to load as 72 so that it generally only scales down.
		game.assets.load("kenpixel_blocks.ttf", BitmapFont.class, p);	// Queues loading the font to use in the game,
		game.assets.finishLoading();									// and forces it to load so that loading messages can be displayed.
		
		BitmapFont font = game.assets.get("kenpixel_blocks.ttf", BitmapFont.class);	// Sets the font to use for TextElements,
		loading = new TextElement("Loading...0%", font, 0, 0);						// and creates the loading TextElement.
		
		game.assets.load("white_circle.png", Texture.class);	// Queues loading the sprite for the orbs,
		game.assets.load("white_torus.png", Texture.class);		// and for the pulses.
		
		Tween.registerAccessor(Color.class, new ColorTween());		// Registers the accessor to use for tweening Color values.
		Tween.registerAccessor(Camera.class, new CameraTween());	// Registers the accessor to use for Tweening the Camera.
		Tween.registerAccessor(Sprite.class, new SpriteTween());	// Registers the accessor to use for Tweening Sprites.
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		
		camera.position.set(0, 0, 0);	// Centers the camera,
		camera.update();				// and updates the change.
		
		// Calculates where the loading text should fit.
		Rectangle textArea = new Rectangle(width * 0.25f, height * 0.25f, width * 0.5f, height * 0.5f);
		loading.scaleToFit(textArea, true);	// Forces the text to fit within this area.
	}
	
	@Override
	public void update(float delta) {
		if(game.assets.update()) nextScreen = new PlayScreen(game); 			// Keeps the AssetManager loading and switches the screen when finished.
		loading.setText("Loading..." + game.assets.getProgress() * 100 + "%");	// Updates the loading message with the current progress from the AssetManager.
	}
	
	@Override
	public void draw(SpriteBatch batch, Rectangle area) {
		loading.draw(batch);	// Draws the loading message.
	}
}
