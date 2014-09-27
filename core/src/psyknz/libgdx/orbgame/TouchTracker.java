package psyknz.libgdx.orbgame;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;

public class TouchTracker {
	
	private Array<Vector2> touchCoords; // Array containing all recorded touch co-ordinates.	
	private float maxLength; 			// Total distance recorded touch co-ordinates should cover if strung together in order.
	
	public TouchTracker(float maxLength) {
		touchCoords = new Array<Vector2>();	// Creates a new array to track touch co-ordinates.
		this.maxLength = maxLength;			// Sets the maximum length the array can be in units.
	}
	
	// Adds the given vector to the list of recorded touch co-ordinates.
	public void addTouch(Vector2 touch) {
		touchCoords.insert(0, touch);	// Adds a new touch co-ordinate to the front of the array.
		trimToMaxLength();				// Ensures the length of the array doesn't exceed the current maximum length.
	}
	
	// Adds the given x, y co-ordinate to the list of recorded touch co-ordinates.
	public void addTouch(float x, float y) {
		Vector2 touch = new Vector2(x, y);	// Creates a new Vector2 to represent the touch location.
		addTouch(touch);					// Adds the newly created touch co-ordinate to the list of co-ordinates.
	}
	
	// Reduces the length of the touch co-ordinate array to less than or equal to the maximum length in units.
	public void trimToMaxLength() {
		if(touchCoords.size > 1) {											// If there are two or more recorded co-ordinates,
			float length = 0;												// their length in units is calculated.
			for(int i = 1; i < touchCoords.size; i++) {						// Every recorded co-ordinate,
				length += touchCoords.get(i).dst(touchCoords.get(i - 1));	// Has its distance from the previous co-ordinate added to the length.
				if(length > maxLength) {									// If the length exceeds the maximum length,
					touchCoords.removeRange(i, touchCoords.size - 1);		// Then all additional co-ordinates are removed from the record.
					break;
				}
			}
		}
	}
	
	// Sets a new maximum length for the TouchTracker.
	public void setMaxLength(float maxLength) {
		this.maxLength = maxLength;	// Sets a new maximum length for the array.
		trimToMaxLength();			// Ensures the length of the array is within the newly set maximum length.
	}
	
	// Interpolates the co-ordinate at the given length along the chain of recorded co-ordinates.
	// If length is greater than the current length of recorded co-ordinates, or there are no recorded co-ordinates, null is returned.
	// If there is only one recorded co-ordinate then it is returned.
	public Vector2 interpolateCoord(float targetLength) {
		if(touchCoords.size == 1) {		// If there is only one recorded co-ordinate,
			return touchCoords.get(0);	// it is returned.
		}
		
		else if(touchCoords.size > 1) {
			float length = 0;
			for(int i = 1; i < touchCoords.size; i++) {
				length += touchCoords.get(i).dst(touchCoords.get(i - 1));
				if(length >= targetLength) {
					Vector2 vec = new Vector2(touchCoords.get(i));
					vec.lerp(touchCoords.get(i - 1), (targetLength - length) / touchCoords.get(i).dst(touchCoords.get(i - 1)));
					return vec;
				}
			}
		}
		
		return null;
	}
}
