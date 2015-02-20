package psyknz.libgdx.orbgame;

import com.badlogic.gdx.utils.Array;

public class TimedEventProcessor {
	
	private Array<TimedEvent> events;	// Array containing all timed events being processed.
	
	/** Creates a new TimedEventProcessor which manages all TimedEvents. */
	public TimedEventProcessor() {
		events = new Array<TimedEvent>();	// Initialises the Array storing TimedEvents.
	}
	
	/** Game logic for the processor.
	 * @param delta Time in milliseconds since last game loop cycle. */
	public void update(float delta) {
		for(TimedEvent event : events) {								// Every event currently being managed,
			if(!event.update(delta)) events.removeValue(event, true);	// is counted down and if it's finished it's removed.
		}
	}
	
	/** Adds a timed event to the list of events being managed by the processor.
	 * @param event Reference to the event which needs to be tracked. */
	public void addTimedEvent(TimedEvent event) {
		events.add(event);
	}

}
