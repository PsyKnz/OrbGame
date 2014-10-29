package psyknz.libgdx.orbgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class HighscoreSystem {
	
	public static final String DEFAULT_NAME = "Player";	// Name assigned to default scores.
	
	private int highscoreEntries;		// Number of highscore entries recorded by the GameScore.
	private int[] highscores;			// Array containing all of the current highscore values.
	private String[] highscoreNames;	// Array containing all of the names tied to the respective highscores.
	public String defaultName;			// The name new saved highscores should have associated with them by default.
	
	public int score;	// Players current score.
	
	public HighscoreSystem(int entries) {
		this.highscoreEntries = entries;		// Records the number of highscore entries tracked.
		highscores = new int[entries];			// Initialises the highscore array,
		highscoreNames = new String[entries];	// and the array of corresponding names.
		loadHighscores();						// Loads saved highscores.
		resetScore();							// Resets the current score.
	}
	
	/** Function to load highscores from the game preferences. */
	public void loadHighscores() {
		Preferences scoreData = Gdx.app.getPreferences("ScoreData");			// Accesses the ScoreData preferences information.
		for(int i = 0; i < highscoreEntries; i++) {								// For every highscore entry the game tracks,
			highscores[i] = scoreData.getInteger("highscore" + i, 0);			// The corresponding highscore is loaded, or set to 0 if empty.
			highscoreNames[i] = scoreData.getString("name" + i, DEFAULT_NAME);	// corresponding names are loaded as well, or set to "unknown".
		}
		defaultName = scoreData.getString("default name", DEFAULT_NAME);	// The default name for saving new scores under is loaded as well.
	}
	
	/** Function to save highscores to the game preferences */
	public void saveHighscores() {
		Preferences scoreData = Gdx.app.getPreferences("ScoreData");	// Accesses the ScoreData preferences information.
		for(int i = 0; i < highscoreEntries; i++) {						// For every highscore entry the game tracks,
			scoreData.putInteger("highscore" + i, highscores[i]);		// the highscore is saved into the preferences,
			scoreData.putString("name" + i, highscoreNames[i]);			// as is the corresponding name.
		}
		scoreData.putString("default name", defaultName);	// Saves the default name for saving scores for the future.
		scoreData.flush();									// Flushes the data to the ScoreData file.
	}
	
	/** Adds the current score to the highscore table.
	 * @return The rank the score added acheived in the highscore table, or the size of the table if it didn't rank. */
	public int addScoreToHighscores() {
		for(int i = 0; i < highscoreEntries; i++) {				// Starts with the hihest highscore and works backwards.
			if (score > highscores[i]) {						// If the current score is bigger than a score in the list,
				for(int j = highscoreEntries - 1; j > i; j--) {	// then starting with the lowest score,
					highscores[j] = highscores[j - 1];			// all scores after it are replaced by the score infront of them to make room.
					highscoreNames[j] = highscoreNames[j - 1];	// The associated names are shifted down aswell.
				}
				highscores[i] = score;				// Stores the new highscore,
				highscoreNames[i] = defaultName;	// and gives it the default name.
				return i;							// Returns the index of rank of the score.
			}
		}
		return highscoreEntries;	// If the score doesn't rate on the highscores then the max number of entries is returned.
	}
	
	/** Resets the GameScore for a new game. */
	public void resetScore() {
		score = 0;
	}
	
	/** Returns the desired score from the highscore list.
	 * @param index Position of the score in the list of highscores.
	 * @return The score at the given position on the highscore list. */
	public int getHighscore(int index) {
		return highscores[index];
	}
	
	/** Returns the name associated with the desired highscore vaue.
	 * @param index Position of the score in the list of highscores.
	 * @return The name associated with the target score. */
	public String getHighscoreName(int index) {
		return highscoreNames[index];
	}
	
	/** Sets a new name to be associated with a given highscore.
	 * @param name The new name you want associated with the highscore.
	 * @param index Position of the score whose name you wish to edit. */
	public void setHighscoreName(String name, int index) {
		highscoreNames[index] = name;
	}
	
	/** Returns the number of highscores recorded by this object.
	 * @return Number of highscores recorded by this GameScore. */
	public int getHighscoreEntries() {
		return highscoreEntries;
	}
	
	/** Resets all the highscores back to 0 and a default name. */
	public void resetHighscores() {
		for(int i = 0; i < highscoreEntries; i++) {	// Every highscore recorded by this object,
			highscores[i] =  1000 - i * 1000;		// is set between 1000 and 100,
			highscoreNames[i] = DEFAULT_NAME;		// and the associated name is returned to default.
		}
		defaultName = DEFAULT_NAME;	// Resets the default name for saving highscores.
		saveHighscores();			// Saves the reset highscore information.
	}

}
