package characters;

public class LightSword extends Sword {
	public LightSword(int productNumber) {
		super(productNumber);
	}

	public void hit(Knight knight, Ogre ogre) {
		if (knight.getEnergy() >= ogre.getEnergy())
			ogre.setEnergy((int) (0.5 * ogre.getEnergy()));
		
//		System.out.println("Knight - light sword");
	}
	
	public void hit(BraveKnight knight, Ogre ogre) {
		ogre.setEnergy((int) (0.3 * ogre.getEnergy()));
		
//		System.out.println("Brave Knight - light sword");		
	}
}
