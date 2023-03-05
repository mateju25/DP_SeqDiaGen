package gui;

import game.*;
import javafx.scene.control.*;

public class EnergyOfWarriors extends TextField implements ClashFollower {
	private Game game;
	private int energy;

	public EnergyOfWarriors(Game game) {	
		super();
		this.game = game;
	}
	
	public void inform() {
		energy = game.knightsEnergy() + game.ogresEnergy();
		setText(Integer.toString(energy));
	}
}
