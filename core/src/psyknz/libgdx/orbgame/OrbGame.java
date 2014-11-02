package psyknz.libgdx.orbgame;

import psyknz.libgdx.architecture.*;

public class OrbGame extends GameCore {
	
	public OrbGame(GoogleServicesResolver services) {
		super(services);
	}
	
	@Override
	public void create() {
		super.create();
		setScreen(new LoadingScreen(this));
	}
}
