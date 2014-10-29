package psyknz.libgdx.orbgame;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.InputMultiplexer;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.TweenCallback;

public class InputLock implements InputProcessor, TweenCallback {
	
	// Booleans to determine which forms of input are currently locked.
	public boolean keyDown, keyUp, keyTyped, touchDown, touchUp, touchDragged, mouseMoved, scrolled;
	
	private InputMultiplexer input;	// Reference to the InputMultiplexer this InputLock is blocking.
	
	/** Creates a new InputLock with all forms of input currently blocked.
	 * @param input The InputMultiplexer this InputLock will block. */
	public InputLock(InputMultiplexer input) {
		this(input, true, true, true, true, true, true, true, true);
	}
	
	/** Creates a new InputLock which blocks all forms of input set to true.
	 * @param input The InputMultiplexer this InputLock will block.
	 * @param keyDown Whether the user pushing down on a key is locked.
	 * @param keyUp Whether the user releasing a key is locked.
	 * @param keyTyped Whether the user typing normally is locked.
	 * @param touchDown Whether the user touch down on the screen is locked.
	 * @param touchUp Whether the user lifting their finger off of the screen is locked.
	 * @param touchDragged Whether the user dragging their finger across the screen is locked.
	 * @param mouseMoved Whether accessing the mouses movements is locked.
	 * @param scrolled Whether scrolling with the mouse wheel is locked. */
	public InputLock(InputMultiplexer input, boolean keyDown, boolean keyUp, boolean keyTyped, boolean touchDown, 
			boolean touchUp, boolean touchDragged, boolean mouseMoved, boolean scrolled) {
		this.keyDown = keyDown;
		this.keyUp = keyUp;
		this.keyTyped = keyTyped;
		this.touchDown = touchDown;
		this.touchUp = touchUp;
		this.touchDragged = touchDragged;
		this.mouseMoved = mouseMoved;
		this.scrolled = scrolled;
		
		input.addProcessor(0, this);
		this.input = input;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		return keyDown;
	}

	@Override
	public boolean keyUp(int keycode) {
		return keyUp;
	}

	@Override
	public boolean keyTyped(char character) {
		return keyTyped;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return touchDown;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return touchUp;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return touchDragged;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return mouseMoved;
	}

	@Override
	public boolean scrolled(int amount) {
		return scrolled;
	}

	/** Used to allow InputLocks to remove themselves after a tween finishes. Ensure animations are played in full before the
	 *  user can do anything.
	 * @param eventType The type of callback being sent.
	 * @param source Reference to the Tween sending the callback. */
	@Override
	public void onEvent(int eventType, BaseTween<?> source) {
		if(eventType == TweenCallback.COMPLETE) input.removeProcessor(this);
	}

}
