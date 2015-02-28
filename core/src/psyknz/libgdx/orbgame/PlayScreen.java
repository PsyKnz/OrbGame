package psyknz.libgdx.orbgame;

import psyknz.libgdx.architecture.*;
import psyknz.libgdx.orbgame.misc.GamePalette;
import psyknz.libgdx.orbgame.play.OrbCollisionProcessor;
import psyknz.libgdx.orbgame.play.OrbData;
import psyknz.libgdx.orbgame.tweenaccessors.CameraTween;
import psyknz.libgdx.orbgame.tweenaccessors.SpriteTween;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Quart;

public class PlayScreen extends GameScreen {
	
	public static final float ORB_DIAMETER = 54;	// Size of the orbs in the game.
	public static final int NUM_COLORS = 5;			// Number of different colors an orb can be in a game.
	public static final int NUM_HIGHSCORES = 10;	// Number of highscores which are recorded locally by this game.
	
	private float spawnDistance;	// Distance from the magnet new orbs should be spawned at.
	public float spawnRate = 1.0f;	// How frequently new orbs should spawn in seconds.
	private float spawnTimer = 0;	// Tracks time in seconds until a new orb should spawn.
	
	private int gameOverStage = 0;	// Flag used to determine if the game is over.
	
	private World world; 			// Reference to the box2d simulation world.
	private BodyDef orbBodyDef; 	// Default body definition for new orbs.
	private FixtureDef orbFixDef;	// Fixture definitons for orbs and their sensors.
	private CircleShape orbShape; 	// Shape information for orbs and their sensors.
	private Body border, magnet;	// References to the border and magnet object which are always present.
	
	private Array<Body> orbs = new Array<Body>(); // Creates a blank Array to store references to bodies in the Box2D simulation.
	private PlayerController player;
	private GamePalette palette;
	
	public final HighscoreSystem scores;	
	public final TweenManager tweenManager;
	public final GameEventProcessor eventProcessor;

	private OrbData orbData; // Member variable used to temporarily access orb data.
	
	private GameUi ui;					//
	private OrthographicCamera uiCamera;	//
	
	private Vector2 velocity = new Vector2();
	
	private Box2DDebugRenderer debug = new Box2DDebugRenderer();
	
	public PlayScreen(GameCore game) {
		super(game);
		viewSize = 480; // Sets the length of the shortest screen edge in game units.
		
		palette = new GamePalette(NUM_COLORS);
		tweenManager = new TweenManager();
		eventProcessor = new GameEventProcessor();
		scores = new HighscoreSystem(NUM_HIGHSCORES);
		ui = new GameUi(this, input);
		uiCamera = new OrthographicCamera();
		
		spawnDistance = (float) Math.sqrt(Math.pow(viewSize / 2, 2) + Math.pow(viewSize / 2, 2)) + ORB_DIAMETER; // Sets how far from the magnet orbs should spawn.
		
		world = new World(new Vector2(0, 0), true);	// Creates the Box2D World space.
		//new OrbCollisionProcessor(this, world); 	// Creates a new collision processor to listen to box2d contcts.
		
		orbBodyDef = new BodyDef(); 					// Creates the definition for orbs.
		orbBodyDef.type = BodyDef.BodyType.DynamicBody; // Sets orbs type to dynamic so that it is affected by forces.
		orbShape = new CircleShape(); 					// Creates the circle shape used to define orbs.
		orbShape.setRadius(ORB_DIAMETER / 2); 			// Sets the radius of all circles to ORB_DIAMETER.
		orbFixDef = new FixtureDef(); 					// Creates a new fixture definiton for orbs.
		orbFixDef.shape = orbShape; 					// Sets the fixtures shape to the predefined circle.
		orbFixDef.friction = 0.0f; 						// Sets the orbs friction.
		orbFixDef.density = 0.1f; 						// Sets the orbs density.
		
		player = new PlayerController(world, ui, this);
	}
	
	@Override
	public void show() {
		super.show();		
		generateWorld();
		generateNewGame();
		//ui.displayMainMenu();
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		
		camera.position.set(magnet.getPosition().x, magnet.getPosition().y, 0);	// Places the camera overtop of the magnet.
		camera.update();														// Updates the camera to put the changes into effect.
		
		player.setCamera(camera); // Sets the camera used by the player to process touch co-ordinates.
		
		uiCamera.viewportWidth = width;		//
		uiCamera.viewportHeight = height;	//
		uiCamera.update();					//
		ui.setCamera(uiCamera);				//
	}
	
	@Override
	public void resume() {
		super.resume();
		if(ui.messageStack.size == 0) ui.pauseGame(); // Pauses the game if there are no pending messages on the stack.
	}
	
	@Override
	public void update(float delta) {		
		eventProcessor.update(delta);
		tweenManager.update(delta);	
		if(ui.update(delta)) return;
		
		if(gameOverStage > 1) {
			for(Body orb : orbs) world.destroyBody(orb);
			scores.resetScore();
			spawnRate = 1;
			gameOverStage = 0;
			/*Tween.to(camera, CameraTween.POS, 1).target(magnet.getPosition().x, magnet.getPosition().y, 0)
					.ease(Quart.OUT).start(tweenManager);
			Tween.to(camera, CameraTween.VIEW, 1).target(camera.viewportWidth * 2, camera.viewportHeight * 2)
					.ease(Quart.OUT).start(tweenManager);*/
			generateWorld();
			generateNewGame();
		}
		
		spawnTimer -= delta;			// Counts down the spawn timer.
		while(spawnTimer <= 0) {		// As long as the spawnTimer is less tha 0,
			createOrb();				// a new orb is created,
			spawnTimer += spawnRate;	// and the timer is incremented by the spawn rate to the current spawn rate.
		}
		
		for(Body orb : orbs) {
			velocity.set(0 - orb.getPosition().x, 0 - orb.getPosition().y).limit(viewSize / 2);
			orb.setLinearVelocity(velocity);
		}
		
		world.step(1/60f, 6, 2); // Steps through the Box2D simulation.
		
		player.update();
		
		for(Body orb: orbs) {							// For every orb in the simulation,
			orbData = (OrbData) orb.getUserData();	// it's user data is acessed.
			orbData.update(delta);						// and the position of its bounding box is updated.
		}
	}
	
	@Override
	public void draw(SpriteBatch batch, Rectangle view) {
		orbData = (OrbData) border.getUserData();	// Draws the border to the screen.
		orbData.getSprite().draw(batch);		
		orbData = (OrbData) magnet.getUserData();	// Draws the magnet to the screen.
		orbData.getSprite().draw(batch);
		
		for(Body orb: orbs) if(orb.getType() == BodyDef.BodyType.DynamicBody) {	// Every dynamic orb on the screen,
			orbData = (OrbData) orb.getUserData();							// has its user data accessed,
			orbData.getSprite().draw(batch);									// and is drawn to the screen.
		}
		
		player.draw(batch);
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		
		//debug.render(world, camera.combined);
		
		batch.setProjectionMatrix(uiCamera.combined);
		batch.begin();
		ui.draw(batch);
		batch.end();
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
		orb.setLinearVelocity(0 - x, 0 - y);
		
		Sprite orbSprite = new Sprite(game.assets.get("white_circle.png", Texture.class));	// Creates a sprite for the orb.
		orbSprite.setSize(ORB_DIAMETER, ORB_DIAMETER);	// Sets the size of the sprite as the default orb size.
		orbSprite.setColor(palette.getRandomColor());			// Sets the color of the sprite to a randomly selected color.
		orb.setUserData(new OrbData(null, orb, orbSprite,	// Generates the orbs non-physics related data.
				OrbData.State.FREE));	
		
		world.getBodies(orbs); // Refreshes the list of box2d elements.
		
		return orb; // Returns a reference to the orb which has just been created.
	}
	
	public void selectOrb(Body orb) {
		player.addOrbToSelection(orb);
	}
	
	@Override
	public void dispose() {
		orbShape.dispose(); // Disposes of the orbs circle information.
		world.dispose();	// Disposes of the box2d simulation object.
		super.dispose();
	}
	
	private void placeRingOfOrbs(int num, float offset) {
		float distance = ORB_DIAMETER * num / 6;
		for(int i = 0; i < num; i++) {
			Body orb = createOrb(magnet.getPosition().x + MathUtils.sinDeg(i * 360 / num + offset) * distance, 
					magnet.getPosition().y + MathUtils.cosDeg(i * 360 / num + offset) * distance);
			orbData = (OrbData) orb.getUserData();
			orbData.inPlay = true;
		}
	}
	
	public void gameOver(Body orb) {
		/*Tween.to(camera, CameraTween.POS, 1).target(orb.getPosition().x, orb.getPosition().y, 0)
				.ease(Quart.OUT).start(tweenManager);
		Tween.to(camera, CameraTween.VIEW, 1).target(camera.viewportWidth / 2, camera.viewportHeight / 2)
				.ease(Quart.OUT).start(tweenManager);*/
		player.scoreSelectedOrbs();
		input.removeProcessor(player);
		ui.displayMessage("Game Over");
		eventProcessor.addEvent(new GameEvent() {
			@Override
			public void eventAction() {
				game.services.submitScoreGPGS(scores.score);
				game.services.getLeaderboardGPGS();
				ui.enableInput("Tap to play again");
				/*int scoreResult = scores.addScoreToHighscores();
				if(scoreResult < scores.getHighscoreEntries()) {
					ui.displayHighscores().editHighscoreName(scoreResult);
				}
				else ui.enableInput("Tap to play again");*/
			}
		}).setTimer(1);
		gameOverStage = 2;
	}
	
	public void generateWorld() {
		BodyDef magnetDef = new BodyDef(); 				// Creates a new body definition for the magnet,
		magnetDef.type = BodyDef.BodyType.StaticBody;	// sets it to static,
		magnetDef.position.set(0, 0); 					// and places it at the centre of the game world (0, 0).
		
		magnet = world.createBody(magnetDef);	// Creates a new magnet using the above Body Definition.
		magnet.createFixture(orbShape, 0f);		// Provides the magnet with a single circular fixture of size 0,
		
		Sprite magnetSpr = new Sprite(game.assets.get("white_circle.png", Texture.class));	// Creates the magnets sprite.
		magnetSpr.setSize(ORB_DIAMETER, ORB_DIAMETER);										// Sets it to the size of an orb,
		magnetSpr.setColor(Color.GRAY);														// and makes it gray.
		
		Sprite pulse = new Sprite(game.assets.get("white_torus.png", Texture.class)); 	// Creates a pulse sprite for the magnet.
		pulse.setSize(ORB_DIAMETER, ORB_DIAMETER);										// Sets it to the size of an orb,
		magnetSpr.setColor(Color.GRAY);													// and makes it gray.
		
		OrbData magnetData = new OrbData(null, magnet, magnetSpr, OrbData.State.MAGNET); 	// Generates user data for the magnet.
		//magnetData.setPulse(new PulseElement(pulse, 3.5f, 2, 0.8f, 0));							// Adds the new pulse to the magnet.
		magnetData.inPlay = true;
		magnet.setUserData(magnetData);															// Sets the data to the body.
		
		float borderSize = viewSize - ORB_DIAMETER;		// Determines the size of the border.
		CircleShape borderShape = new CircleShape();	// Creates a new circle,
		borderShape.setRadius(borderSize / 2);			// and sets its size to the size of the border.
		
		FixtureDef borderFixDef = new FixtureDef();	// Creates a new fixture definiton for the border.
		borderFixDef.shape = borderShape;			// Sets the fixtures shape to the new circle,
		borderFixDef.isSensor = true;				// and makes it a sensor so that it has no direct effect on the simulation.
		
		border = world.createBody(magnetDef);	// Creates a new border object using the magnets definition,
		border.createFixture(borderFixDef);		// and generates the borders fixture.
		
		Sprite borderSpr = new Sprite(game.assets.get("white_circle.png", Texture.class));	// Creates a sprite for the border.
		borderSpr.setSize(borderSize, borderSize);											// Sets its size to what was previously calculated,
		borderSpr.setColor(Color.MAROON);													// and makes it MAROON.
		border.setUserData(new OrbData(null, border, borderSpr, OrbData.State.BORDER));		// User data is generated for the border.
		borderShape.dispose();																// The circle created to generate the border is disposed of.
	}
	
	public void generateNewGame() {		
		palette.generatePalette(MathUtils.random(360.0f ), NUM_COLORS);
		
		placeRingOfOrbs(6, 30);									// Places a ring of six orbs,
		placeRingOfOrbs(12, 0);									// and a ring of 12 orbs,
		for(int i = 0; i < 30; i++) world.step(1/60f, 6, 2);	// then simulates the world for half a second to put everything in place.
		
		for(Body orb: orbs) {							// Every orb in the simulation,
			orbData = (OrbData) orb.getUserData();	// has its user data accessed,
			orbData.update(0);							// and is updated to sync its sprite with its physics body.
		}
		
		ui.resetScoreDisplay();
		
		ui.displayMessage("Play Game"); 			// Displays a message while waiting for the player to start a game.
		eventProcessor.addEvent(new GameEvent() {	// Creates a new GameEvent.
			@Override
			public void eventAction() {
				ui.enableInput("Tap screen to start");	// The event enables user input to start the game.
			}
		}).setTimer(0.5f);	// After a half second delay.
		
		input.addProcessor(player);
	}
	
	public Array<Body> getOrbs() {
		return orbs;
	}
	
	public void scoreOrbs() {
		player.scoreSelectedOrbs();
	}

}
