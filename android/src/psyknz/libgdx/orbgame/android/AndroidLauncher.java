package psyknz.libgdx.orbgame.android;

import android.os.Bundle;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.graphics.Color;

import com.google.android.gms.games.Games;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdRequest;

import com.google.example.games.basegameutils.GameHelper;
import com.google.example.games.basegameutils.GameHelper.GameHelperListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import psyknz.libgdx.orbgame.OrbGame;
import psyknz.libgdx.architecture.GoogleServicesResolver;

public class AndroidLauncher extends AndroidApplication implements GameHelperListener, GoogleServicesResolver {
	
	private AdView adView;
	private View gameView;
	private GameHelper helper;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

	    RelativeLayout layout = new RelativeLayout(this);
	    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
	    layout.setLayoutParams(params);

	    AdView admobView = createAdView();
	    layout.addView(admobView);
	    View gameView = createGameView(config);
	    layout.addView(gameView);

	    setContentView(layout);
	    startAdvertising(admobView);
	    
	    if (helper == null) {
	    	helper = new GameHelper(this, GameHelper.CLIENT_GAMES);
	    	helper.enableDebugLog(true);
	    }
	    helper.setup(this);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		helper.onStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		helper.onStop();
	}
	
	@Override
	public void onActivityResult(int request, int response, Intent data) {
		super.onActivityResult(request, response, data);
		helper.onActivityResult(request, response, data);
	}
	
	private AdView createAdView() {
	    adView = new AdView(this);
	    adView.setAdSize(AdSize.SMART_BANNER);
	    adView.setAdUnitId(getString(R.string.ad_unit_id));
	    adView.setId(12345); // this is an arbitrary id, allows for relative positioning in createGameView()
	    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
	    params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
	    adView.setLayoutParams(params);
	    adView.setBackgroundColor(Color.BLACK);
	    return adView;
	}
	
	private View createGameView(AndroidApplicationConfiguration cfg) {
	    gameView = initializeForView(new OrbGame(this), cfg);
	    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
	    params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
	    params.addRule(RelativeLayout.BELOW, adView.getId());
	    gameView.setLayoutParams(params);
	    return gameView;
	}
	
	private void startAdvertising(AdView adView) {
	    AdRequest adRequest = new AdRequest.Builder()
	    	.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
	    	.addTestDevice("32C2DDB695CED8D319F0F8D366C23922").build();
	    adView.loadAd(adRequest);
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    if (adView != null) adView.resume();
	}
	
	@Override
	public void onPause() {
	    if (adView != null) adView.pause();
	    super.onPause();
	}
	
	@Override
	public void onDestroy() {
	    if (adView != null) adView.destroy();
	    super.onDestroy();
	}

	@Override
	public boolean getSignedInGPGS() {
		return helper.isSignedIn();
	}

	@Override
	public void loginGPGS() {
		try {
			runOnUiThread(new Runnable() {
				public void run() {
					helper.beginUserInitiatedSignIn();
				}
			});
		} catch(final Exception ex) {
			Gdx.app.log("Android Launcher", "Failed to login to Google Play Game Services.");
		}
	}
	
	@Override
	public void logoutGPGS() {
		try {
			runOnUiThread(new Runnable() {
				public void run() {
					helper.signOut();
				}
			});
		} catch(final Exception ex) {
			Gdx.app.log("Android Launcher", "Failed to logout of Google Play Game Services.");
		}
	}

	@Override
	public void submitScoreGPGS(int score) {
		Games.Leaderboards.submitScore(helper.getApiClient(), getString(R.string.leaderboard_id), score);
	}

	@Override
	public void unlockAchievementGPGS(String achievementId) {
		Games.Achievements.unlock(helper.getApiClient(), achievementId);
	}

	@Override
	public void getLeaderboardGPGS() {
		if (helper.isSignedIn()) {
			startActivityForResult(Games.Leaderboards.getLeaderboardIntent(helper.getApiClient(), getString(R.string.leaderboard_id)), 100);
		}
		else if (!helper.isConnecting()) loginGPGS();
	}

	@Override
	public void getAchievementsGPGS() {
		if (helper.isSignedIn()) {
			startActivityForResult(Games.Achievements.getAchievementsIntent(helper.getApiClient()), 101);
		}
		else if (!helper.isConnecting()) loginGPGS();
	}

	@Override
	public void onSignInFailed() {
	}

	@Override
	public void onSignInSucceeded() {
	}
}
