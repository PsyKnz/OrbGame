package psyknz.libgdx.orbgame.android;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.graphics.Color;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdRequest;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import psyknz.libgdx.orbgame.OrbGame;

public class AndroidLauncher extends AndroidApplication {
	
	private static final String AD_UNIT_ID = "ca-app-pub-9619852994332454/7568636522";
	
	private AdView adView;
	private View gameView;
	
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
	}
	
	private AdView createAdView() {
	    adView = new AdView(this);
	    adView.setAdSize(AdSize.SMART_BANNER);
	    adView.setAdUnitId(AD_UNIT_ID);
	    adView.setId(12345); // this is an arbitrary id, allows for relative positioning in createGameView()
	    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
	    params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
	    adView.setLayoutParams(params);
	    adView.setBackgroundColor(Color.BLACK);
	    return adView;
	}
	
	private View createGameView(AndroidApplicationConfiguration cfg) {
	    gameView = initializeForView(new OrbGame(new AndroidGoogleServices()), cfg);
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
}
