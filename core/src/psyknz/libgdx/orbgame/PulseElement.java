package psyknz.libgdx.orbgame;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PulseElement {
	
	private Sprite sprite; 				// Sprite used to draw the pulse;
	private float pulseStart, pulseEnd; // Size of the pulse before and after it pulses out.
	private float pulseSpeed; 			// How many times the pulse should fire every second.
	private float alphaStart, alphaEnd;	// How much the alpha should change during a pulse.
	
	private float currentSize, currentAlpha;	// The current size and alpha of the pulse at any given point in time.
	private Vector2 currentPos = new Vector2();	// The current position of the pulse in the game world.
	
	private float newPulseEnd = -1; // Variable used to determine if the pulse needs to set a new size.
	
	public PulseElement(Sprite sprite, float pulseScale, float pulseSpeed, float alphaStart, float alphaEnd) {
		this.sprite = sprite;								// Sets the sprite to use for the pulse.
		this.pulseStart = currentSize = sprite.getHeight();	// Sets the starting pulse size and current size to the size of the sprite.
		this.pulseEnd = pulseStart * pulseScale;			// Determines the final pulse size based on the scaling factor.
		this.pulseSpeed = pulseSpeed;						// Sets how many times the should fire each second.
		this.alphaStart = currentAlpha = alphaStart;		// Sets the starting alpha and current alpha to the given value.
		this.alphaEnd = alphaEnd;							// Sets the finishing alpha to the given value.
	}
	
	public void update(float delta) {
		currentSize += (pulseEnd - pulseStart) * pulseSpeed * delta;	// Updates the size of the sprite.
		currentAlpha += (alphaEnd - alphaStart) * pulseSpeed * delta;	// Updates the sprites alpha.
		
		while(currentSize > pulseEnd) {	// While the current size is greater than the final size,
			if(newPulseEnd >= 0) {		// if a new pulse size has been specified,
				pulseEnd = newPulseEnd;	// the pulse size is adjusted,
				newPulseEnd = -1;		// and the new pulse size reset.
			}
			currentSize -= (pulseEnd - pulseStart);	// the current size is reduced back towards the starting size.
		}
		while(currentAlpha < alphaEnd) currentAlpha -= (alphaEnd - alphaStart); // as is the alpha value.
		
		sprite.setSize(currentSize, currentSize);	// The width and height of the sprite is updated to match the current size.
		setCenterPos(currentPos);					// It is re-centered to adjust for being resized.
		sprite.setAlpha(currentAlpha);				// Its alpha is updated.
	}
	
	 // Draws the pulse sprite to the screen.
	public void draw(SpriteBatch batch) {
		sprite.draw(batch);
	}
	
	// Sets the position of the center of the pulse using a Vector.
	public void setCenterPos(Vector2 pos) {
		setCenterPos(pos.x, pos.y);
	}
	
	// Sets the position of the center of the pulse using individual coordinates.
	public void setCenterPos(float x, float y) {
		sprite.setCenter(x, y);
		currentPos.set(x, y);
	}
	
	// Sets a new pulse scale which will take effect once the current pulse is finished.
	public void setPulseScale(float scale) {
		newPulseEnd = pulseStart * scale;
	}

}
