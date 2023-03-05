package characters;

public class IronSword extends Sword {
	public IronSword(int productNumber) {
		super(productNumber);
	}

	public void hit(Knight knight, Ogre ogre) {
		if (knight.getEnergy() >= ogre.getEnergy())
			ogre.setEnergy((int) (0.9 * ogre.getEnergy()));

//		System.out.println("Knight - iron sword");
	}
	
	public void hit(BraveKnight knight, Ogre ogre) {
		ogre.setEnergy((int) (0.8 * ogre.getEnergy()));

//		System.out.println("Brave Knight - iron sword");
	}
}
