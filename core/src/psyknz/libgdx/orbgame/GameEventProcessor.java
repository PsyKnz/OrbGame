package psyknz.libgdx.orbgame;

import com.badlogic.gdx.utils.Array;

public class GameEventProcessor {
	
	private Array<GameEvent> events;	// Array containing all timed events being processed.
	
	/** Creates a new TimedEventProcessor which manages all TimedEvents. */
	public GameEventProcessor() {
		events = new Array<GameEvent>();	// Initialises the Array storing TimedEvents.
	}
	
	/** Game logic for the processor.
	 * @param delta Time in milliseconds since last game loop cycle. */
	public void update(float delta) {
		for(GameEvent event : events) {									// Every event currently being managed,
			if(event.update(delta)) events.removeValue(event, true);	// is counted down and if it's finished it's removed.
		}
	}
	
	/** Adds a timed event to the list of events being managed by the processor.
	 * @param event Reference to the event which needs to be tracked.
	 * @return Returns a reference to the event that's been added for chaining. */
	public GameEvent addEvent(GameEvent event) {
		events.add(event);
		return event;
	}

}
