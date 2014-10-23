package psyknz.libgdx.orbgame;

public abstract class GameEvent {
	
	private float delay, timer;	// How long a delay there should be before the event occurs, and a timer for the delays progress.
	private int repeatNum;		// How many times the event should repeat before it stops occuring. Repeats indefinately if set to -1.
	
	/** Creates a new GameEvent with its timer at 0 without any repeats. */
	public GameEvent() {
		setTimer(0);	// Sets the timer to 0 to ensure the event occurs immediately.
	}
	
	/** Game logic for the event. Counts down the timer for the event which will only occur if its conditions are met. By default events
	 * occur immediately and have no conditions to satisfy. Returns false if the event hasn't occured, and true if it has. 
	 * @param delta Time in seconds that the events timer should be updated by.
	 * @return Whether or not the event has finished. */
	public boolean update(float delta) {
		if(eventCondition()) {										// Provided the eventCondition is currently satisfied,
			timer -= delta;											// The timer counts down.
			if(timer <= 0) {										// If the timer reaches 0,
				eventAction();										// the event occurs.
				if(repeatNum > 0) setTimer(delay, repeatNum--);		// If there are still repeats pending,
				else if(repeatNum < 0) setTimer(delay, repeatNum);	// or if set to repeat indefinately, the timer is reset.
				else return true;									// Otherwise the event reports that it has completed.
			}
		}
		return false;	// Returns false while there is time remaining on the timer.
	}
	
	/** Sets a timer for the event that runs once.
	 * @param time Time in seconds you want the timer to run for. */
	public void setTimer(float time) {
		setTimer(time, false);
	}
	
	/** Sets a timer for the event which will repeat if true.
	 * @param time How long the timer should run for in seconds.
	 * @param repeat Whether or not the event should repeat. */
	public void setTimer(float time, boolean repeat) {
		if(repeat) setTimer(time, -1);	// If repeat is true then repeatNum is set to -1 to repeat indefinately.
		else setTimer(time, 0);			// Otherwise it is set to 0 to prevent repeats.
	}
	
	/** Sets a timer for the event and how many times it should be repeated.
	 * @param time How long the timer should run for in seconds before the event occurs.
	 * @param repeatNum Number of times the event should repeat before it stops. */
	public void setTimer(float time, int repeatNum) {
		this.delay = timer = time;	// Sets the delay on the timer and starts the timer.
		this.repeatNum = repeatNum; // Sets how many times the timer should repeat.
	}
	
	/** Function to determine whether or not the event can occur. Should be overridden if you want a conditional event.
	 * @return Whether or not the conditions required for the event to occur have been satisfied. */
	protected boolean eventCondition() {
		return true;
	}
	
	public abstract void eventAction();	// What the event does when it occurs.

}
