package psyknz.libgdx.orbgame;

import psyknz.libgdx.orbgame.tweenaccessors.Position2dTween;
import psyknz.libgdx.orbgame.tweenaccessors.SpriteTween;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.*;

public class MainMenu extends InputAdapter implements UiElement {
	
	public static final float LOGO_HEIGHT = 0.4f;
	public static final float PADDING_SIZE = 0.1f;
	
	private GameUi ui;
	
	private Sprite background, gameLogo;
	private Array<GameButton> buttons;
	
	public MainMenu(GameUi ui, AssetManager assets) {
		this.ui = ui;
		
		Texture tex = assets.get("white_circle.png", Texture.class);
		background = new Sprite(tex, tex.getWidth() / 2, tex.getHeight(), 1, 1);
		background.setColor(Color.BLACK);
		background.setAlpha(0.75f);
		
		Texture logo = assets.get("game_logo.png", Texture.class);
		gameLogo = new Sprite(logo);
		
		Sprite buttonSpr = new Sprite(background);
		buttonSpr.setColor(Color.GRAY);
		buttons = new Array<GameButton>();
		
		GameButton playGame = new GameButton(new Sprite(buttonSpr)) {
			@Override
			public void buttonAction() {
				callPlayGame();
			}
		};
		playGame.text = new TextElement("Play Game", ui.uiFont, 0, 0);
		buttons.add(playGame);
		
		GameButton options = new GameButton(new Sprite(buttonSpr)) {
			@Override
			public void buttonAction() {
				callOptions();
			}
		};
		options.text = new TextElement("Options", ui.uiFont, 0, 0);
		buttons.add(options);
		
		GameButton googleServices = new GameButton(new Sprite(buttonSpr)) {
			@Override
			public void buttonAction() {
				callGoogleServices();
			}
		};
		googleServices.text = new TextElement("Leaderboard", ui.uiFont, 0, 0);
		buttons.add(googleServices);
	}

	@Override
	public void setCamera(Camera camera) {		
		background.setSize(camera.viewportWidth, camera.viewportHeight);
		background.setCenter(camera.position.x, camera.position.y);
		
		float padding;
		if(background.getWidth() <= background.getHeight()) padding = background.getWidth() * PADDING_SIZE;
		else padding = background.getHeight() * PADDING_SIZE;
		
		float totalHeight = background.getHeight() - padding * (buttons.size + 2);
		
		gameLogo.setSize(background.getWidth() - padding * 2, totalHeight * LOGO_HEIGHT);
		gameLogo.setPosition(background.getX() + padding, 
				background.getY() + background.getHeight() - padding - gameLogo.getHeight());
		gameLogo.setOriginCenter();
		
		Rectangle buttonBounds = new Rectangle(gameLogo.getX(), gameLogo.getY(), 
				gameLogo.getWidth(), (totalHeight - gameLogo.getHeight()) / buttons.size);
		Rectangle textBounds = new Rectangle(buttonBounds.x + buttonBounds.width / 2, 
				buttonBounds.y + buttonBounds.height / 2, 
				buttonBounds.width * (1 - PADDING_SIZE * 2), 
				buttonBounds.height * (1 - PADDING_SIZE * 2));
		
		for(GameButton button : buttons) {
			buttonBounds.y -= padding + buttonBounds.height;
			textBounds.y -= padding + buttonBounds.height;
			button.sprite.setBounds(buttonBounds.x, buttonBounds.y, buttonBounds.width, buttonBounds.height);
			button.text.scaleToFit(textBounds, true);
			button.text.setPosition(textBounds.x, textBounds.y);
			button.camera = camera;
		}
		
		tweenIn(ui.manager);
	}

	@Override
	public void draw(SpriteBatch batch) {
		background.draw(batch);
		gameLogo.draw(batch);
		for(GameButton button : buttons) button.draw(batch);
	}

	@Override
	public Rectangle getBounds() {
		return background.getBoundingRectangle();
	}
	
	public void tweenIn(TweenManager manager) {
		Tween.from(gameLogo, SpriteTween.SCALE_UNIFORM, 3).target(0).ease(Elastic.OUT)
				.setCallback(new TweenCallback() {
			@Override
			public void onEvent(int eventType, BaseTween<?> tween) {
				if(eventType == TweenCallback.COMPLETE);
			}
		}).start(manager);
		for(GameButton button : buttons) {
			Tween.from(button, Position2dTween.Y, 1).cast(Position2d.class)
					.target(buttons.peek().getY() - buttons.peek().sprite.getHeight() * 2)
					.ease(Back.OUT).start(manager);
		}
	}
	
	public void tweenOut(TweenManager manager) {}
	
	private void callPlayGame() {}
	
	private void callOptions() {}
	
	private void callGoogleServices() {}
}
