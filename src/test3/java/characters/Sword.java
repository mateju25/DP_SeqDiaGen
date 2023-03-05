package characters;

import java.io.*;

public abstract class Sword implements Serializable{
	private int productNumber;

	public Sword(int productNumber) {
		this.productNumber = productNumber;
	}
	
	public int getProductNumber() {
		return productNumber;
	}
	public void setProductNumber(int productNumber) {
		this.productNumber = productNumber;
	}

	// we need a hit method for each kind of a knight
	// however, we can use overloading to avoid having to invent method names
	// the compiler makes the decision which method is to be run according to the type of the knight parameter
	public abstract void hit(Knight knight, Ogre ogre);
	public abstract void hit(BraveKnight knight, Ogre ogre);
}
