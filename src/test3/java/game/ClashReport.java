package game;

public class ClashReport implements ClashFollower {
	Game game;
	
	public ClashReport(Game game) {
		this.game = game;
	}
	
	public void inform() {		
		System.out.println("!!! Knights - Ogres " + game.knightsEnergy() + ":" + game.ogresEnergy() + " !!!");
	}
}
