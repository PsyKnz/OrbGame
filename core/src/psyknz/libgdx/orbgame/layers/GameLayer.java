package psyknz.libgdx.orbgame.layers;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface GameLayer extends Disposable {
	
	public void resize(Camera camera);
	
	public boolean update(float delta);
	
	public void draw(SpriteBatch batch);
	
	public void dispose();
}
