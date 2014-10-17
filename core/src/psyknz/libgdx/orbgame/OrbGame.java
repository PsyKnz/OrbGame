package psyknz.libgdx.orbgame;

import psyknz.libgdx.architecture.*;

public class OrbGame extends GameCore {
	
	@Override
	public void create() {
		super.create();
		
		setScreen(new LoadingScreen(this));
	}
}
