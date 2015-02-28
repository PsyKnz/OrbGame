package psyknz.libgdx.orbgame;

import psyknz.libgdx.architecture.GameCore;
import psyknz.libgdx.architecture.GoogleServicesResolver;
import psyknz.libgdx.orbgame.screens.LoadingScreen2D;

public class OrbGame extends GameCore {
	
	public OrbGame(GoogleServicesResolver services) {
		super(services);
	}
	
	@Override
	public void create() {
		super.create();
		setScreen(new LoadingScreen2D(this));
	}
}
