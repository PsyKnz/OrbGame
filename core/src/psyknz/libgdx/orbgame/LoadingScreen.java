package psyknz.libgdx.orbgame;

import psyknz.libgdx.orbgame.tweenaccessors.*;

import psyknz.libgdx.architecture.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import aurelienribon.tweenengine.Tween;

public class LoadingScreen extends GameScreen {
	
	private TextElement loading;	// TextElement displaying loading information.
	
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
		Tween.registerAccessor(Camera.class, new CameraTween());
	}
	
	@Override
	public void show() {
		super.show();
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		camera.position.set(0, 0, 0);
		camera.update();
		
		Rectangle textArea = new Rectangle(width * 0.25f, height * 0.25f, width * 0.5f, height * 0.5f);
		loading.scaleToFit(textArea, true);
	}
	
	@Override
	public void update(float delta) {
		if(game.assets.update()) nextScreen = new PlayScreen(game);
		loading.setText("Loading..." + game.assets.getProgress() * 100 + "%");
	}
	
	@Override
	public void draw(SpriteBatch batch, Rectangle area) {
		loading.draw(batch);
	}
}
