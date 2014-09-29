package psyknz.libgdx.orbgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import psyknz.libgdx.architecture.*;

public class OrbGame extends GameCore {
	
	@Override
	public void create() {
		super.create();
		
		setScreen(new PlayScreen(this));
	}
}
