package psyknz.libgdx.orbgame.layers;

import psyknz.libgdx.orbgame.Position2d;
import psyknz.libgdx.orbgame.TextElement;
import psyknz.libgdx.orbgame.screens.PlayScreen2D;
import psyknz.libgdx.orbgame.screens.GameScreen2D;
import psyknz.libgdx.orbgame.tweenaccessors.CameraTween;
import psyknz.libgdx.orbgame.tweenaccessors.ColorTween;
import psyknz.libgdx.orbgame.tweenaccessors.Position2dTween;
import psyknz.libgdx.orbgame.tweenaccessors.SpriteTween;

import psyknz.libgdx.architecture.FreeTypeFontLoader;
import psyknz.libgdx.architecture.GameCore;

import aurelienribon.tweenengine.Tween;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class LoadingLayer implements GameLayer {

	private GameScreen2D screen;
	private GameCore game;
	
	private TextElement loading;	// TextElement displaying loading information.
	
	public LoadingLayer(GameScreen2D screen) {
		this.screen = screen;
		this.game = screen.getGame();
		
		FreeTypeFontLoader.FreeTypeFontParameter p = new FreeTypeFontLoader.FreeTypeFontParameter();	// Creates a new FreeType Font
		p.size = 72;	// Loader parameter file and sets the size of the font to load as 72 so that it generally only scales down.
		game.assets.load("kenpixel_blocks.ttf", BitmapFont.class, p);	// Queues loading the font to use in the game,
		game.assets.finishLoading();									// and forces it to load so that loading messages can be displayed.
		
		BitmapFont font = game.assets.get("kenpixel_blocks.ttf", BitmapFont.class);	// Sets the font to use for TextElements,
		loading = new TextElement("Loading...0%", font, 0, 0);						// and creates the loading TextElement.
		
		game.assets.load("white_circle.png", Texture.class);	// Queues loading the sprite for the orbs,
		game.assets.load("white_torus.png", Texture.class);		// and for the pulses.
		game.assets.load("game_logo.png", Texture.class);		// and the game logo.
		game.assets.load("pointer.png", Texture.class);			// and the picture for when the AI touches down
		game.assets.load("singleplayer.png", Texture.class);	// and the picture for when the AI releases the screen.
		
		Tween.registerAccessor(Color.class, new ColorTween());		// Registers the accessor to use for tweening Color values.
		Tween.registerAccessor(Camera.class, new CameraTween());	// Registers the accessor to use for Tweening the Camera.
		Tween.registerAccessor(Sprite.class, new SpriteTween());	// Registers the accessor to use for Tweening Sprites.
		Tween.registerAccessor(Position2d.class, new Position2dTween());
	}
	
	@Override
	public void resize(Camera camera) {
		// Calculates where the loading text should fit.
		Rectangle textArea = new Rectangle(camera.viewportWidth * 0.25f, camera.viewportHeight * 0.25f, 
				camera.viewportWidth * 0.5f, camera.viewportHeight * 0.5f);
		loading.scaleToFit(textArea, true);	// Forces the text to fit within this area.
	}

	@Override
	public boolean update(float delta) {
		if(game.assets.update()) screen.setScreen(new PlayScreen2D(game)); 	// Keeps the AssetManager loading and switches the screen when finished.
		loading.setText("Loading..." + game.assets.getProgress() * 100 + "%");			// Updates the loading message with the current progress from the AssetManager.
		return false;
	}

	@Override
	public void draw(SpriteBatch batch) {
		loading.draw(batch);	// Draws the loading message.
	}
	
	@Override
	public void dispose() {}

}
