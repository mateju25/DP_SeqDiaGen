package characters;

public class BadOgre extends Ogre {
	public BadOgre() {
		super();
	}
	public BadOgre(int energy) {
		super(energy);
	}
	public BadOgre(int energy, boolean hungry) {
		super(energy, hungry);
	}

	public void revenge(Knight knight) {
		if (getHungry())
			eat(knight);
	}
	public void eat(Knight knight) {
		knight.setEnergy(0);
	}
}
