package game;

public class ClashWinners implements ClashFollower {
	Game game;
	
	public ClashWinners(Game game) {
		this.game = game;
	}
	
	public void inform() {
		if (game.knightsEnergy() > game.ogresEnergy()) 
			System.out.println("*** Knights are winninig. ***");
		else
			System.out.println("*** Ogres are winninig. ***");			
	}
}
