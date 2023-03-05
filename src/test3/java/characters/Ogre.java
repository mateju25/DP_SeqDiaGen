package characters;

import java.io.*;

public class Ogre implements Energy, Serializable {
	private boolean hungry;
	private int energy;
	
	public Ogre() {
	}
	public Ogre(int energy) {
		this.energy = energy;
	}
	public Ogre(int energy, boolean hungry) {
		this.energy = energy;
		this.hungry = hungry;
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
		return energy;
	}
	
	public void setHungry(boolean hungry) {
		this.hungry = hungry;
	}
	public boolean getHungry() {
		return hungry;
	}

	public void setValues(int energy) {
		this.energy = energy;
	}

	public void setValues(boolean hungry) {
		this.hungry = hungry;
	}
	
	public void setValues(int energy, boolean hungry) {
		this.energy = energy;
		this.hungry = hungry;
	}
	
	public void revenge(Knight knight) {
		if (energy > knight.getEnergy())
			knight.setEnergy((int) (0.9 * knight.getEnergy()));
	}
}
