package psyknz.libgdx.orbgame.screens;

import psyknz.libgdx.architecture.GameCore;

import psyknz.libgdx.orbgame.layers.*;
import psyknz.libgdx.orbgame.misc.CameraController;
import psyknz.libgdx.orbgame.misc.GamePalette;
import psyknz.libgdx.orbgame.debug.OrbDebugger;

public class PlayScreen2D extends GameScreen2D {
	
	public static final int PLAY_AREA_SIZE = 48;		// The size of the play area (which is square) in in-game units.
	public static final float MAX_DELTA_TIME = 1.0f;	// Maximum length of time which may pass between updates in seconds.
	
	private CameraController camControl;	// Camera controller to improve resizing and provide zoom control.

	private OrbDebugger debug;	// Debugger used to deal with issues on the Orb Layer.
	
	/**
	 * Creates a new PlayScreen and loads al shared resources.
	 * @param game Reference to the game object managing this screen.
	 */
	public PlayScreen2D(GameCore game) {
		super(game);		
		camControl = new CameraController(camera, PLAY_AREA_SIZE, 	// Creates a new camera controller with a square target size that
				PLAY_AREA_SIZE, CameraController.FIT_TO_SCREEN);	// will fit itself to screen, displaying space out of the play area.
		
		// TODO: get the orb layer displayed and self sufficient on screen.
		OrbLayer orbLayer = new OrbLayer(game.assets, new GamePalette(5));
		
		layers.add(orbLayer);
		//input.addProcessor(orbLayer.player);
		
		debug = new OrbDebugger(orbLayer);
		input.addProcessor(debug);
	}
	
	@Override
	public void resize(int width, int height) {
		camControl.resize(width, height);
		super.resize(width, height);
		
		debug.setCamera(camera);
	}
	
	@Override
	public void render(float delta) {
		if(delta > MAX_DELTA_TIME) delta = MAX_DELTA_TIME;
		super.render(delta);
		debug.draw();
	}
	
	@Override
	public void show() {
		super.show();
		//showMainMenu();
	}
	
	public void showMainMenu() {
		MainMenuLayer menu = new MainMenuLayer(game.assets);
		menu.enableInput(input);
		layers.add(menu);
	}

}
