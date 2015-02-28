package psyknz.libgdx.orbgame.screens;

import psyknz.libgdx.architecture.GameCore;

import psyknz.libgdx.orbgame.layers.LoadingLayer;

public class LoadingScreen2D extends GameScreen2D {

	public LoadingScreen2D(GameCore game) {
		super(game);
	}
	
	@Override
	public void show() {
		super.show();
		layers.add(new LoadingLayer(this));
	}

}
