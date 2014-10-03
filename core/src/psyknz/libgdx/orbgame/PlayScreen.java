package psyknz.libgdx.orbgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Body;

import psyknz.libgdx.architecture.*;

public class PlayScreen extends GameScreen {
	
	public static final float ORB_DIAMETER = 42;	// Size of the orbs in the game.
	public static final int POINTS_PER_ORB = 10;	// Number of points earned per orb.
	
	private float spawnDistance;	// Distance from the magnet new orbs should be spawned at.
	private float spawnRate = 1.0f;	// How frequently new orbs should spawn in seconds.
	private float spawnTimer = 0;	// Tracks time in seconds until a new orb should spawn.
	
	private float orbAcceleration; // Variables to spawn, place, and move the orbs.
	
	// Array containing all the potential colors an orb can be.
	private Color[] orbColors = {Color.BLUE, Color.PURPLE, Color.RED, Color.MAGENTA, Color.CYAN};
	private Array<Color> colorList = new Array<Color>(); // Array which contains the above colours in random order.
	
	private TouchTracker touchTracker;	// Reference to the object recording user input.
	private ScoreBarElement scores;		// Reference to the object recording and displaying the players score.
	private boolean gameOver = false;	// Flag used to determine if the game is over.
	
	private World world; 			// Reference to the box2d simulation world.
	private BodyDef orbBodyDef; 	// Default body definition for new orbs.
	private FixtureDef orbFixDef;	// Fixture definitons for orbs and their sensors.
	private CircleShape orbShape; 	// Shape information for orbs and their sensors.
	private Body border, magnet;	// References to the border and magnet object which are always present.
	
	private Array<Body> orbs = new Array<Body>(); // Creates a black Array to store references to bodies in the Box2D simulation.
	
	private boolean debugOn = false; 			// Flag to determine if the game is in debug mode.
	private GameDebug debug;					// Object to provide custom in-game debug options.
	private Box2DDebugRenderer debugRenderer;	// Object to display debug information for the box2d simulation.
	
	private GameMessage currentMsg = null;	// Holds any currently displayed message.
	
	private Vector3 touchCoords = new Vector3(); // Vector3 used for processing and converting user touch co-ordinates.
	private OrbElement orbDataA; // Member variable used to temporarily access orb data.
	private Array<Body> selectedOrbs = new Array<Body>(); // Array to store orbs currently selected by the player.
	private int fingerUsedToSelect;
	private Body orbToAdd = null; // Flag to indicate an orb which should be added to the selection when possible.
	private Array<Vector2> selectedCoords = new Array<Vector2>();
	
	public PlayScreen(GameCore game) {
		super(game);
		viewSize = 480; // Sets the length of the shortest screen edge in game units.
		
		input.addProcessor(this); 						// Adds the PlayScreen as an input processor for the game.
		touchTracker = new TouchTracker(ORB_DIAMETER);	// Creates a TouchTracker to movement of the players finger across the screen.
		
		world = new World(new Vector2(0, 0), true);					// Creates the Box2D World space.
		world.setContactListener(new OrbCollisionProcessor(this)); 	// Creates a new collision processor to listen to box2d contcts.
		
		// Sets the distance new orbs should spawn from the magnet to the distance from the magnet to the furtherest corner of the screen plus the radius of an orb.
		spawnDistance = (float) Math.sqrt(Math.pow(viewSize / 2, 2) + Math.pow(viewSize / 2, 2)) + ORB_DIAMETER;
		orbAcceleration = spawnDistance * viewSize; // Sets the base acceleration rate for orbs.
		
		FreeTypeFontLoader.FreeTypeFontParameter p = new FreeTypeFontLoader.FreeTypeFontParameter();
		p.size = 20;
		game.assets.load("kenpixel_blocks.ttf", BitmapFont.class, p);
		game.assets.load("white_circle.png", Texture.class);
		game.assets.load("white_torus.png", Texture.class);
		game.assets.finishLoading();
		
		orbShape = new CircleShape(); 					// Creates the circle shape used to define orbs.
		orbShape.setRadius(ORB_DIAMETER / 2); 			// Sets the radius of all circles based on the pre-defined ORB_DIAMETER.
		
		orbBodyDef = new BodyDef(); 					// Creates the definition for orbs.
		orbBodyDef.type = BodyDef.BodyType.DynamicBody; // Sets orbs type to dynamic so that it is affected by forces.
		
		orbFixDef = new FixtureDef(); 	// Creates a new fixture definiton for orbs.
		orbFixDef.shape = orbShape; 	// Sets the fixtures shape to the predefined circle.
		orbFixDef.friction = 0.0f; 		// Sets the orbs friction.
		orbFixDef.density = 0.1f; 		// Sets the orbs density.
		orbFixDef.restitution = 0.0f; 	// Sets the orbs restitution.
		
		if(debugOn) {									// If debug mode is enabled at start-up,
			debug = new GameDebug(this, world);			// then a game debug object is initialised,
			debugRenderer = new Box2DDebugRenderer();	// as is a box2d debug object.
			input.addProcessor(0, debug);				// The GameDebug object becomes the primary input processor.
		}
	}
	
	@Override
	public void show() {
		super.show();
		
		BodyDef magnetDef = new BodyDef(); 					// Creates a new definition for the magnet.
		magnetDef.type = BodyDef.BodyType.StaticBody; 		// Sets the magnet to static.
		magnetDef.position.set(0, 0); 					// Places the magnet in its spot.
		magnet = world.createBody(magnetDef); 				// Creates the magnet body.
		magnet.createFixture(orbShape, 0f); 				// Sets the fixture for the magnet to a circle.
		Sprite magnetSprite = new Sprite(game.assets.get("white_circle.png", Texture.class));			// Creates the sprite to use for drawing the magnet.
		magnetSprite.setSize(ORB_DIAMETER, ORB_DIAMETER);	// Sets the sprite to the size of the orbs.
		magnetSprite.setColor(Color.GRAY);					// Sets the color of the magnet to gray.
		Sprite pulse = new Sprite(game.assets.get("white_torus.png", Texture.class));						// Creates the sprite to use for drawing the magnet pulse.
		pulse.setSize(ORB_DIAMETER, ORB_DIAMETER);					// Sets the size of the sprite to the size of the orbs.
		pulse.setColor(Color.GRAY);// Sets the color of the pulse to white.
		OrbElement magnetData = new OrbElement(magnet, magnetSprite, OrbElement.State.MAGNET);
		magnetData.setPulse(new PulseElement(pulse, 3.5f, 2, 0.8f, 0));	// Creates the pulse element so that it doubles in size as it pulses, pulses twice a second, and goes from an alpha of 0.8 to 0.3 as it pulses.
		magnet.setUserData(magnetData);	// Creates Orb Data for the magnet.
		
		CircleShape borderShape = new CircleShape();		// Creates a new circle.
		float borderSize = viewSize - ORB_DIAMETER;			// Creates a float which will store the size of the border.
		borderShape.setRadius(borderSize / 2);				// Sets the size of the circle to the size the border should be.
		FixtureDef borderFixDef = new FixtureDef();			// Creates a fixture to represent the border.
		borderFixDef.shape = borderShape;					// Sets the fixtures shape as the border circle.
		borderFixDef.isSensor = true;						// Sets the fixture as a sensor to prevent physics affecting it.
		border = world.createBody(magnetDef);				// Places a body in the box2d simulation representing the border.
		border.createFixture(borderFixDef);					// Creates the circular fixture for the body.
		Sprite borderSprite = new Sprite(game.assets.get("white_circle.png", Texture.class));			// Creates a sprite to use when drawing the border.
		borderSprite.setSize(borderSize, borderSize);		// Sets the sprites size equal to the size of the border.
		borderSprite.setColor(Color.MAROON);				// Sets the color of the border.
		border.setUserData(new OrbElement(border, 
				borderSprite, OrbElement.State.BORDER));	// Sets the borders user data as the sprite used to draw it.
		borderShape.dispose();								// Disposes of the circle information for the border.	
		
		world.getBodies(orbs); 	// Updates the list of Box2D entities.
		
		placeRingOfOrbs(6, 30);
		placeRingOfOrbs(12, 0);
		for(int i = 0; i < 30; i++) world.step(1/60f, 6, 2);
		for(Body orb: orbs) {
			orbDataA = (OrbElement) orb.getUserData();
			orbDataA.update(0);
		}
		
		currentMsg = displayMessage("Tap Screen to Start"); // Waits for user input before starting a game.
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		camera.position.x = magnet.getPosition().x;	// Sets the x position of the camera to above the magnet.
		camera.position.y = magnet.getPosition().y;	// Sets the y position of the camera to above the magnet.
		camera.update();							// Updates the camera to put the changes into effect.
		
		float panelHeight = (camera.viewportHeight - viewSize) / 2;
		Texture tex = game.assets.get("white_circle.png", Texture.class);
		Sprite topPanel = new Sprite(tex, tex.getWidth() / 2, tex.getHeight() / 2, 1, 1);
		topPanel.setBounds(0 - viewSize / 2, viewSize / 2, viewSize, panelHeight);
		topPanel.setColor(Color.DARK_GRAY);
		Sprite bottomPanel = new Sprite(tex, tex.getWidth() / 2, tex.getHeight() / 2, 1, 1);
		bottomPanel.setBounds(0 - viewSize / 2, 0 - viewSize / 2 - panelHeight, viewSize, panelHeight);
		bottomPanel.setColor(Color.DARK_GRAY);
		BitmapFont font = game.assets.get("kenpixel_blocks.ttf", BitmapFont.class);
		scores = new ScoreBarElement(topPanel, bottomPanel, font);
	}
	
	@Override
	public void resume() {
		super.resume();
		currentMsg = displayMessage("Tap Screen to Resume"); // Waits for user input before resuming a game.
	}
	
	@Override
	public void update(float delta) {
		if(debugOn) debug.update(delta); // Updates the debugger.
		
		if(currentMsg != null) return; // If there is a message being displayed on screen then the rest of the update step is skipped.
		if(gameOver) nextScreen = new PlayScreen(game);
		
		spawnTimer -= delta;			// Counts down the spawn timer.
		while(spawnTimer <= 0) {		// As long as the spawnTimer is less tha 0,
			createOrb();				// a new orb is created,
			spawnTimer += spawnRate;	// and the timer is incremented by the spawn rate to the current spawn rate.
		}
		
		touchTracker.interpolateCoords(selectedCoords, selectedOrbs.size, ORB_DIAMETER);				// Gets the co-ordinates for where selected orbs should be placed.
		for(int i = 0; i < selectedOrbs.size; i++) {													// Each currently selected orb,
			selectedOrbs.get(i).setTransform(selectedCoords.get(i), selectedOrbs.get(i).getAngle());	// Is placed along the array of the interpolated string of touchCoords.
		}
		
		for (Body orb : orbs) {
			if(orb.getType() == BodyDef.BodyType.DynamicBody) {
				orbDataA = (OrbElement) orb.getUserData();
				orbDataA.attractTo(magnet.getPosition(), orbAcceleration * delta);
			}
		}
		
		world.step(1/60f, 6, 2); // Steps through the Box2D simulation.
		
		if(orbToAdd != null) {				// If an orb is flagged to be added to the selection,
			addOrbToSelection(orbToAdd); 	// its added,
			orbToAdd = null;				// and the flag is set back to null.
		}
		
		for(Body orb: orbs) {							// For every orb in the simulation,
			orbDataA = (OrbElement) orb.getUserData();	// it's user data is acessed.
			orbDataA.update(delta);						// and the position of its bounding box is updated.
		}
		
		scores.update(delta);
	}
	
	@Override
	public void draw(SpriteBatch batch, Rectangle view) {
		orbDataA = (OrbElement) border.getUserData();	// Draws the border to the screen.
		orbDataA.getSprite().draw(batch);		
		orbDataA = (OrbElement) magnet.getUserData();	// Draws the magnet to the screen.
		orbDataA.getSprite().draw(batch);
		
		for(Body orb: orbs) if(orb.getType() == BodyDef.BodyType.DynamicBody) {	// Every dynamic orb on the screen,
			orbDataA = (OrbElement) orb.getUserData();							// has its user data accessed,
			orbDataA.getSprite().draw(batch);									// and is drawn to the screen.
		}
		
		for(Body orb: selectedOrbs) {					// Every orb the user currently has selected,
			orbDataA = (OrbElement) orb.getUserData();	// has its user data accessed,
			orbDataA.getSprite().draw(batch);			// and is drawn to the screen.
		}
		
		for(Body orb: orbs) {													// Every orb on the screen,
			orbDataA = (OrbElement) orb.getUserData();							// has its user data accessed,
			if(orbDataA.getPulse() != null) orbDataA.getPulse().draw(batch);	// and if it has a pulse its pulse is drawn.
		}
		
		scores.draw(batch);
		
		if(debugOn) debug.draw(batch); // Draws an FPS counter in the top left coner of the screen if debug is on.
		if(currentMsg != null) currentMsg.draw(batch);
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);		
		if(debugOn) debugRenderer.render(world, camera.combined); // Renders all Box2D entities if in debug mode.
	}
	
	// Creates a new orb at a random location on the screen.
	public Body createOrb() {
		float angleSpawn = MathUtils.random(360.0f); // Randomly selects where to spawn the orb relative to the magnet.
		
		return createOrb(magnet.getPosition().x + MathUtils.sinDeg(angleSpawn) * spawnDistance, // Creates a new orb at a position based
				magnet.getPosition().y + MathUtils.cosDeg(angleSpawn) * spawnDistance);			// on the randomly generated angle.
	}
	
	// Creates a new orb at the given position.
	public Body createOrb(float x, float y) {
		orbBodyDef.position.set(x, y); // Sets the x, y co-ordinates for the new orb.
		
		Body orb = world.createBody(orbBodyDef);	// Creates a new orb in the physics world.
		orb.createFixture(orbFixDef); 				// Generates the fixture representing the physical orb.
		orb.setLinearVelocity(0 - x / 2, 0 - y / 2);
		
		Sprite orbSprite = new Sprite(game.assets.get("white_circle.png", Texture.class));			// Creates a sprite for the orb.
		orbSprite.setSize(ORB_DIAMETER, ORB_DIAMETER);	// Sets the size of the sprite as the default orb size.
		orbSprite.setColor(getRandomColor());			// Sets the color of the sprite to a randomly selected color.
		orb.setUserData(new OrbElement(orb, orbSprite,	// Generates the orbs non-physics related data.
				OrbElement.State.FREE));	
		
		world.getBodies(orbs); // Refreshes the list of box2d elements.
		
		return orb; // Returns a reference to the orb which has just been created.
	}
	
	public void selectOrb(Body orb) {
		orbToAdd = orb;
	}
	
	// Adds the given orb to the list of currently selected orbs.
	private void addOrbToSelection(Body orb) {
		if(selectedOrbs.size > 0) {										// If there are already orbs selected by the player,
			orbDataA = (OrbElement) selectedOrbs.peek().getUserData();	// the data for the last orb is selected,
			orbDataA.setState(OrbElement.State.SELECTED);				// and its state is set to SELECTED (From ACTIVE_SELECTED).
			orbDataA.getPulse().setPulseScale(1.3f);					// Reduces the pulse scale.
		}
		selectedOrbs.add(orb); 														// Adds the given orb to the selection,
		orbDataA = (OrbElement) selectedOrbs.peek().getUserData();					// and accesses its user data.
		orbDataA.setState(OrbElement.State.ACTIVE_SELECTED);						// It's state is set to ACTIVE_SELECTED.
		Sprite pulseSprite = new Sprite(game.assets.get("white_torus.png", Texture.class));
		pulseSprite.setSize(ORB_DIAMETER, ORB_DIAMETER);
		pulseSprite.setColor(orbDataA.getSprite().getColor());
		orbDataA.setPulse(new PulseElement(pulseSprite, 2, 3, 0.8f, 0.0f));
		touchTracker.setMaxLength(selectedOrbs.size * ORB_DIAMETER + ORB_DIAMETER); // Increases the length of the TouchTracker to accomodate another orb.
		orb.setType(BodyDef.BodyType.StaticBody);									// Sets the selected orb to static so that it isn't effected by forces.
		orb.getFixtureList().first().setSensor(true);								// Sets the selected orb as a sensor so that it can drag over orbs.
	}
	
	// Scores all currently selected orbs and removes them from the game.
	public void scoreSelectedOrbs() {
		scores.addToScore((int) Math.pow(selectedOrbs.size, 2) * POINTS_PER_ORB);
		spawnRate *= 0.95f;
		orbDataA = (OrbElement) selectedOrbs.peek().getUserData();
		orbDataA.setState(OrbElement.State.SELECTED);
		for(Body orb : selectedOrbs) world.destroyBody(orb);	// Every currently selected orb is removed from the box2d simulation.
		selectedOrbs.clear();
		touchTracker.setMaxLength(ORB_DIAMETER);				// Resets the length of the touchTracker to the diameter of an orb.
		world.getBodies(orbs); 									// Refreshes the list of box2d elements in the simulation.
	}
	
	@Override
	public void dispose() {
		if(debugOn) debugRenderer.dispose(); 	// Disposes of the debugRenderer if there is one.
		orbShape.dispose(); 					// Disposes of the orbs circle information.
		super.dispose();
	}
	
	/** Processes the player touching down on the screen. If the player touches on top of an orb it becomes selected so that it can
	 *  follow the users finger across the screen. 
	 * @see com.badlogic.gdx.InputAdapter#touchDown(int, int, int, int) */
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(currentMsg != null) return true; // If a message is currently being displayed on the screen no user input is accepted.
		
		touchCoords.set(screenX, screenY, 0); 	// Sets the current co-ordinates for the users touch input.
		camera.unproject(touchCoords); 			// Transforms the touch co-ordinates from screen space to world space.
		
		for(Body orb : orbs) { 														// Every orb is checked,
			if(orb.getType() == BodyDef.BodyType.DynamicBody) { 					// to see if it has a Dynamic body.
				orbDataA = (OrbElement) orb.getUserData(); 							// If it does, it's OrbElement is accessed,
				if(orbDataA.getBounds().contains(touchCoords.x, touchCoords.y)) {	// and its bounding box is tested against the touch co-ordinates.
					fingerUsedToSelect = pointer;
					addOrbToSelection(orb);											// If the player touched inside the bounding box, the orb becomes selected.
					touchTracker.addTouch(touchCoords.x, touchCoords.y);			// Adds the current touch-coordinate to the TouchTracker.
					return true;
				}
			}
		}
		return false;
	}
	
	/** Processes the player dragging their finger across the screen. Provided the player currently has some orbs selected the movement
	 *  is recorded by the touchTracker object.
	 * @see com.badlogic.gdx.InputAdapter#touchDragged(int, int, int) */
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(currentMsg != null) return true; // If a message is currently being displayed on the screen no user input is accepted.
		
		if(selectedOrbs.size > 0 && pointer == fingerUsedToSelect) {// Only processed if the player currently has orbs selected.
			touchCoords.set(screenX, screenY, 0); 					// Stores the co-ordinates where the player has touched the screen.
			camera.unproject(touchCoords); 							// Transforms the touch co-ordinates from screen space to world space.
			touchTracker.addTouch(touchCoords.x, touchCoords.y);	// Adds the current touch-coordinate to the TouchTracker.
			return true;
		}
		return false;
	}
	
	/** Processes the player lifting their finger off of the screen. If the player has orbs selected they are scored for the player.
	 * @see com.badlogic.gdx.InputAdapter#touchUp(int, int, int, int) */
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(currentMsg != null) {	// If a message is currently being displayed,
			currentMsg = null;		// it is removed from the screen.
			return true; 			// No more input is accepted.
		}
		
		if(selectedOrbs.size > 0 && pointer == fingerUsedToSelect) {	// If there are currently some orbs selected,
			scoreSelectedOrbs();										// each selected orb is removed from the box2d simulation.
			return true;
		}
		return false;
	}
	
	private void placeRingOfOrbs(int num, float offset) {
		float distance = ORB_DIAMETER * num / 6;
		for(int i = 0; i < num; i++) {
			createOrb(magnet.getPosition().x + MathUtils.sinDeg(i * 360 / num + offset) * distance, 
					magnet.getPosition().y + MathUtils.cosDeg(i * 360 / num + offset) * distance);
		}
	}
	
	// Returns a random color from the list of predefined colors using a shuffled list without replacement.
	public Color getRandomColor() {
		if(colorList.size <= 1) {							// If the list of colors to choose from is empty a new list is generated.
			for(int num = 0; num < 3; num++) {				// The list is filled with three of each color,
				for(int i = 0; i < orbColors.length; i++) {	// using colors available in the current theme.
					colorList.add(orbColors[i]);			// Colors are added sequentially.
				}
			}
			colorList.shuffle(); // Shuffles the newly generated list to allow for randomisation.
		}
		return colorList.pop(); // Returns the color at the end of the list.
	}
	
	public GameMessage displayMessage(String text) {
		GameMessage msg = new GameMessage(text, game.assets.get("kenpixel_blocks.ttf", BitmapFont.class), magnet.getPosition().x, magnet.getPosition().y);
		Texture tex = game.assets.get("white_circle.png", Texture.class);
		Sprite msgBackgroundSpr = new Sprite(tex, tex.getWidth() / 2, tex.getHeight() / 2, 1, 1);
		msgBackgroundSpr.setColor(Color.BLACK);
		msgBackgroundSpr.setAlpha(0.75f);
		msg.setBackground(msgBackgroundSpr, Gdx.graphics.getWidth(), 50);
		return msg;
	}
	
	public void gameOver(Body orb) {
		currentMsg = displayMessage("Game Over");
		gameOver = true;
		orbDataA = (OrbElement) orb.getUserData();
		orbDataA.getSprite().setColor(Color.WHITE);
	}
	
	public Array<Body> getSelectedOrbs() {
		return selectedOrbs;
	}

}
