package psyknz.libgdx.orbgame;

public abstract class TimedEvent {
	
	private float timer; 		// Float recording how long the timer should run for in seconds.
	private float currentTime;	// Float tracking how far the timer has counted down in seconds.
	public boolean repeat;		// Boolean to determine whether or not the timer should repeat when finished.
	
	/** Creates a new timer.
	 * @param time How long the timer should run for in seconds.
	 * @param repeat Whether or not the timer should repeat when finished. */
	public TimedEvent(float time, boolean repeat) {
		setTimer(time);			// Sets how long the timer will run for,
		this.repeat = repeat;	// and whether the timer should repeat.
	}
	
	/** Sets how long the timer should run for and resets how far through the previous timer it had counted.
	 * @param time How long the timer should run for in seconds. */
	public void setTimer(float time) {
		assert time >= 0;		// Prevents the timer being set to a negative value, causing unexpected results.
		this.timer = time;		// Sets how long the timer runs for,
		currentTime = timer;	// and resets the counter to start from scratch.
	}
	
	/** Counts down the timer. If it finishes it does its action then starts again if set to repeat.
	 * @param delta Time in seconds to process.
	 * @return Returns true if the timer has finished counting down, or false if it is still counting. */
	public boolean update(float delta) {
		currentTime -= delta;				// Counts down the timer.
		if(currentTime <= 0) {				// If the timer has reached 0,
			timedAction();					// then the timers action occurs,
			if(repeat) currentTime = timer;	// and if set to repeat the timer is reset.
		}
		if(currentTime > 0 || !repeat) return false;	// If the timer is counting or is set to repeat it states it hasn't finished,
		else return true;								// Otherwise it states that it has finished.
	}
	
	public abstract void timedAction();	// Function called when the timer reaches 0. Defined at runtime.

}
