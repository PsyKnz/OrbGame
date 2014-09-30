package psyknz.libgdx.orbgame;

import psyknz.libgdx.architecture.*;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;

public class Box2dDebugger extends InputAdapter{
	
	private PlayScreen screen;	// Reference to the screen this debugger is functioning on.
	
	private BitmapFont font; 				// Font to use when displaying debug information.
	private int fps = 0, displayNum = 0; 	//
	private float displayTime = 0;			//
	
	private World world;		// Reference to the box2d world this debugger is accessing information from.
	private Body body = null; 	// Reference to the currently selected Body the debugger is displaying information for.
	
	private Vector3 touchCoords = new Vector3();	// Temporary vector to store and translate screen -> game co-ordinates.
	private Array<Body> bodies = new Array<Body>();	// Temporary array to store all bodies in the box2d simulation.
	private OrbElement orbData;						// Temporary variable used to access orb user data.
	
	public Box2dDebugger(PlayScreen screen, World world) {
		this.screen = screen;
		font = new BitmapFont();
		this.world = world;
	}
	
	public void update(float delta) {
		// Every second the number of frames displayed is calculated.
		displayTime += delta;
		displayNum++;
		if(displayTime > 1) {
			fps = displayNum;
			displayNum = 0;
			displayTime -= 1;
		}
	}
	
	public void draw(SpriteBatch batch) {
		font.draw(batch, "FPS: " + fps, screen.getCamera().position.x - screen.getCamera().viewportWidth / 2,
				screen.getCamera().position.y + screen.getCamera().viewportHeight / 2); 
		if(body != null) font.draw(batch, "VEL X: " + body.getLinearVelocity().x + ", Y: " + body.getLinearVelocity().y,
				body.getPosition().x, body.getPosition().y);
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(screen.debugRenderOn) return true;	// Prevents touchDown from being called at all if in debug mode.
		else return false;						// Otherwise the input is passed on.
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		touchCoords.set(screenX, screenY, 0);
		screen.getCamera().unproject(touchCoords);
		world.getBodies(bodies);
		for(Body orb : bodies) {
			orbData = (OrbElement) orb.getUserData();
			if(orbData.getBounds().contains(touchCoords.x, touchCoords.y) && orbData.getState() == OrbElement.State.FREE) {
				body = orb;
				return true;
			}
		}
		
		body = null;	// If no FREE body is selected then body is set to null,
		return false;	// and the function passes the event to the next input processor.
	}

}
