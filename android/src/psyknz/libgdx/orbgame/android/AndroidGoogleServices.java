package psyknz.libgdx.orbgame.android;

import psyknz.libgdx.orbgame.GoogleServicesResolver;

public class AndroidGoogleServices implements GoogleServicesResolver {

	@Override
	public boolean getSignedInGPGS() {
		return false;
	}

	@Override
	public void loginGPGS() {
	}

	@Override
	public void submitScoreGPGS(int score) {
	}

	@Override
	public void unlockAchievementGPGS(String achievementId) {
	}

	@Override
	public void getLeaderboardGPGS() {
	}

	@Override
	public void getAchievementsGPGS() {
	}
	
	

}
