package psyknz.libgdx.orbgame.screens;

import psyknz.libgdx.architecture.GameCore;

import psyknz.libgdx.orbgame.layers.GameLayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class GameScreen2D implements Screen {
	
	protected GameCore game;	// Reference to the game object managing this screen.
	
	private SpriteBatch batch;				// SpriteBatch used to batch all draw commands for the screen.
	protected OrthographicCamera camera;	// Camera used to render the game space in 2D.
	
	protected InputMultiplexer input;	// Input processor to manage all input from layers on this screen.
	
	protected Array<GameLayer> layers;	// Array containing all game layers which need to be drawn to the screen.
	
	private GameScreen2D nextScreen;	// Reference to the next screen to be displayed.
	
	/**
	 * Creates a new game screen managed by the given game.
	 * @param game The game object managing this screen.
	 */
	public GameScreen2D(GameCore game) {
		this.game = game;	// Saves a reference to the game managing this screen.
		
		layers = new Array<GameLayer>();							// Instantiates the array to manage all game layers.
		camera = new OrthographicCamera(							// Instantiates a blank Camera for this screen to use
				Gdx.graphics.getWidth(), Gdx.graphics.getHeight());	// and sets its dimensions to the current size of the screen.
		input = new InputMultiplexer();								// Instantiates the input processor for this screen.
	}
	
	@Override
	public void show() {
		batch = new SpriteBatch();			// Instantiates a new batcher for drawing layers on this screen.
		Gdx.input.setInputProcessor(input);	// Sets this screens input processor as the processor the game should use.
	}
	
	@Override
	public void resize(int width, int height) {
		for(GameLayer l : layers) l.resize(camera);	// resizes all layers using the dimensions of the screen layers.
	}
	
	@Override
	/**
	 * Runs the game logic for all GameLayers, starting with the topmost layer, before clearing the screen and drawing the
	 * contents of each layers, starting with the lowest layer.
	 * @param delta Time in ms since render was last called.
	 */
	public void render(float delta) {
		for(int i = layers.size - 1; i >= 0; i--) {	// Starting with the top most layer
			if(layers.get(i).update(delta)) break;	// the logic for each layer is updated, if one returns true updating stops.
		}
		
		Gdx.gl.glClearColor(0, 0, 0, 1);			// Sets the color to use when clearing the screen to opaque black,
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);	// then clears the screen.
		
		batch.setProjectionMatrix(camera.combined);	// before setting the projection matrix for the batch.
		batch.begin();								// Batching of draw commands begins.
		for(GameLayer l : layers) l.draw(batch);	// Every GameLayer is drawn to the screen.
		batch.end();								// Batching ends and all draw commands are rendered.
		
		if(nextScreen != null) {		// If there is currently a screen queued up to shift to
			game.setScreen(nextScreen);	// then that screen is loaded
			nextScreen = null;			// and the queue is cleared.
		}
	}
	
	@Override
	public void resume() {}
	
	@Override
	public void pause() {}

	@Override
	public void hide() {
		this.dispose();
	}
	
	@Override
	public void dispose() {
		batch.dispose();						// Disposes of the batch used by this screen.
		for(GameLayer l : layers) l.dispose();	// All layers dispose of their assets not collectively managed.
	}
	
	/**
	 * Queues up the next screen which needs to be displayed.
	 * @param screen
	 */
	public void setScreen(GameScreen2D screen) {
		nextScreen = screen;	// Records the next screen which needs to be shown.
	}
	
	/**
	 * @return Game managing this screen.
	 */
	public GameCore getGame() {
		return game;
	}
}
