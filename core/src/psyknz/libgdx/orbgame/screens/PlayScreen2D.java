package psyknz.libgdx.orbgame.screens;

import aurelienribon.tweenengine.Tween;
import psyknz.libgdx.architecture.GameCore;

import psyknz.libgdx.orbgame.layers.*;
import psyknz.libgdx.orbgame.misc.CameraController;
import psyknz.libgdx.orbgame.misc.GamePalette;
import psyknz.libgdx.orbgame.debug.*;

public class PlayScreen2D extends GameScreen2D {
	
	public static final int PLAY_AREA_SIZE = 48;		// The size of the play area (which is square) in in-game units.
	public static final float MAX_DELTA_TIME = 1.0f;	// Maximum length of time which may pass between updates in seconds.
	
	private CameraController camControl;	// Camera controller to improve resizing and provide zoom control.
	
	private OrbDebugger debug;				// Debugger used to deal with issues on the Orb Layer.
	private PaletteDebugger paletteDebug;	// Debugger used to deal with issues in the palette.
	private boolean debugEnabled = false;	// Whether or not debug should be enabled.
	
	/**
	 * Creates a new PlayScreen and loads al shared resources.
	 * @param game Reference to the game object managing this screen.
	 */
	public PlayScreen2D(GameCore game) {
		super(game);		
		camControl = new CameraController(camera, PLAY_AREA_SIZE, 	// Creates a new camera controller with a square target size that
				PLAY_AREA_SIZE, CameraController.FIT_TO_SCREEN);	// will fit itself to screen, displaying space out of the play area.
		
		GamePalette palette = new GamePalette(5);
		OrbLayer orbLayer = new OrbLayer(game.assets, palette);
		AIPlayer ai = new AIPlayer(game.assets, orbLayer);
		orbLayer.enableAI(ai);
		layers.add(orbLayer);
		layers.add(ai);
		
		debug = new OrbDebugger(orbLayer);
		paletteDebug = new PaletteDebugger(palette, game.assets);
		//debugEnabled = true;
		input.addProcessor(paletteDebug);
	}
	
	@Override
	public void resize(int width, int height) {
		camControl.resize(width, height);
		super.resize(width, height);
		
		debug.setCamera(camera);
		paletteDebug.resize(width, height);
	}
	
	@Override
	public void render(float delta) {
		if(delta > MAX_DELTA_TIME) delta = MAX_DELTA_TIME;
		super.render(delta);
		
		if(debugEnabled) {
			debug.draw();
			paletteDebug.draw();
		} 
	}
	
	@Override
	public void show() {
		super.show();
		//showMainMenu();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		debug.dispose();
		paletteDebug.dispose();
	}
	
	public void showMainMenu() {
		MainMenuLayer menu = new MainMenuLayer(game.assets);
		menu.enableInput(input);
		layers.add(menu);
	}

}
