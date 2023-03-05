package characters;

import java.util.*;
import java.io.*;

public class Knight implements Energy, CombatUnit, Serializable {
	private int energy;
	private int additionalEnergy;
	
	protected Sword sword;

	public Knight() {
	}
	public Knight(int energy) {
		setEnergy(energy);
	}	
	public Knight(int energy, Sword sword) {
		setEnergy(energy);
		this.sword = sword;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}
	public void increaseEnergy(int energy) {
		this.energy += energy;
	}
	public void decreaseEnergy(int energy) {
		this.energy -= energy;
	}
	public int getEnergy() {
		return energy + additionalEnergy;
	}
	
	public void attack(Ogre ogre) {
		sword.hit(this, ogre);
		ogre.revenge(this); 
	}
	
	public int showSword() {
		return sword.getProductNumber();
	}

	// The Composite pattern - the Leaf role
	public List<Knight> getWarriors() {
		List<Knight> individuals = new ArrayList<>();
		individuals.add(this);
		return individuals;
	}
	
	public String report(String tab) {
		return "Knight " + energy;
	}
}
