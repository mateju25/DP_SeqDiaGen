package characters;

public class BraveKnight extends Knight {

	public BraveKnight() {
		super();
	}
	public BraveKnight(int energy) {
		super(energy);
	}
	public BraveKnight(int energy, Sword sword) {
		super(energy, sword);
	}
	
	public void attack(Ogre ogre) {		
		sword.hit(this, ogre);
	}

	public String report(String tab) {
		return "Brave Knight " + getEnergy();
	}
}
