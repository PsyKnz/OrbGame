package psyknz.libgdx.orbgame.layers;

import psyknz.libgdx.architecture.UITable;

import psyknz.libgdx.orbgame.uifeatures.UIButton;
import psyknz.libgdx.orbgame.uifeatures.UISprite;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.assets.AssetManager;

public class MainMenuLayer implements GameLayer {
	
	private InputMultiplexer input;	// Reference to the input processor this layer is enabled on.
	
	private UITable table;	// UI Table used to position the logo and buttons.
	
	private UISprite logo;				// Reference to the logo drawn at the top of the menu.
	private Array<UIButton> buttons;	// Reference to the buttons drawn on the menu.
	
	/**
	 * Creates a new main menu layer which displays the game logo as well as three buttons: Play Game, How To Play, and Options.
	 * @param assets AssetManager managing the assets needed to build the menu.
	 */
	public MainMenuLayer(AssetManager assets) {
		table = new UITable(0.05f, 0.12f, true);	// Creates a new UITable		
		buttons = new Array<UIButton>();			// Instantiates a new array to hold the game buttons.
		
		Texture tex = assets.get("white_circle.png", Texture.class);			// Loads the texture to use for drawing buttons
		Sprite spr = new Sprite(tex, 32, 32, 1, 1);								// and builds a sprite using a single pixel from it.
		spr.setColor(Color.GRAY);												// Makes the sprite light gray
		spr.setAlpha(0.6f);														// and 40% transparent.
		BitmapFont font = assets.get("kenpixel_blocks.ttf", BitmapFont.class);	// Loads the font used to draw text on the buttons.
		UIButton.UIButtonStyle style = new UIButton.UIButtonStyle(				// Creates a new button style
				spr, font, 0.5f);												// using the new sprite, font, and half button padding.
		
		UIButton playGame = new UIButton("Play Game", style) {	// Creates the button for playing a new game or resuming a game.
			@Override
			public void buttonAction() {
				playGame();
			}
		};
		buttons.add(playGame);
		
		UIButton howToPlay = new UIButton("How To Play", style) {	// Creates the button for running the tutorial.
			@Override
			public void buttonAction() {
				howToPlay();
			}
		};
		buttons.add(howToPlay);
		
		UIButton options = new UIButton("Options", style) {	// Creates the button for opening the options menu.
			@Override
			public void buttonAction() {
				options();
			}
		};
		buttons.add(options);
		
		logo = new UISprite(spr);	// Creates a new sprite to use for the logo.
		table.addRow(0.4f);			// Adds a single cell to the top
		table.addFeature(logo);		// and fills it with the game logo.
		
		for(UIButton b : buttons) {	// For every button on the main menu
			table.addRow(0.2f);		// a new row is added to the table to hold the button
			table.addFeature(b);	// and the button is added to the table.
		}
	}
	
	@Override
	public void resize(Camera camera) {
		for(UIButton b : buttons) b.setCamera(camera);	// Updates the camera being used to draw the buttons.
		
		table.setBounds(camera.position.x - camera.viewportWidth / 2, 	// Sets the bounds of the table to fill the screen entirely.
				camera.position.y - camera.viewportHeight / 2, 			//
				camera.viewportWidth, camera.viewportHeight);			//
	}
	
	@Override
	public boolean update(float delta) {
		return false;
	}
	
	@Override
	public void draw(SpriteBatch batch) {
		logo.draw(batch);
		for(UIButton b : buttons) b.draw(batch);
	}
	
	@Override
	public void dispose() {}
	
	public void enableInput(InputMultiplexer input) {
		this.input = input;									// Records the input processor managing input from the main menu.
		for(UIButton b : buttons) input.addProcessor(0, b);	// Inserts each button into the processor at the beginnning.
	}
	
	public void disableInput() {
		for(UIButton b : buttons) input.removeProcessor(b);
	}
	
	// TODO: implement function to play a new game or resume an old game.
	public void playGame() {}
	
	// TODO: implement function to start a tutorial.
	public void howToPlay() {}
	
	// TODO: implement function to display the options menu.
	public void options() {}

}
