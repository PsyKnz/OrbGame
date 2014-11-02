package psyknz.libgdx.orbgame.desktop;

import psyknz.libgdx.architecture.GoogleServicesResolver;

public class DesktopGoogleServices implements GoogleServicesResolver {

	@Override
	public boolean getSignedInGPGS() {
		System.out.println("DesktopGoogleServices: getSignedInGPGS()");
		return false;
	}

	@Override
	public void loginGPGS() {
		System.out.println("DesktopGoogleServices: loginGPGS()");
	}
	
	@Override
	public void logoutGPGS() {
		System.out.println("DesktopGoogleServices: logoutGPGS()");
	}

	@Override
	public void submitScoreGPGS(int score) {
		System.out.println("DesktopGoogleServices: submitScoreGPGS(" + score + ")");
	}

	@Override
	public void unlockAchievementGPGS(String achievementId) {
		System.out.println("DesktopGoogleServices: unlockAchievementGPGS(" + achievementId + ")");
	}

	@Override
	public void getLeaderboardGPGS() {
		System.out.println("DesktopGoogleServices: getLeaderboardGPGS()");
	}

	@Override
	public void getAchievementsGPGS() {
		System.out.println("DesktopGoogleServices: getAcheivementsGPGS()");
	}

}
