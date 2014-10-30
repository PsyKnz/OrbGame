package psyknz.libgdx.orbgame;

import psyknz.libgdx.architecture.*;

public class OrbGame extends GameCore {
	
	public final GoogleServicesResolver services;	// Object allowing platform independent access to Google Play Services.
	
	public OrbGame(GoogleServicesResolver services) {
		this.services = services;
	}
	
	@Override
	public void create() {
		super.create();
		
		setScreen(new LoadingScreen(this));
	}
}
