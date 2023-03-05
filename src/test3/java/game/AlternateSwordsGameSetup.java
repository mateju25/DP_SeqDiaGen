package game;

import characters.*;
import java.util.*;

public class AlternateSwordsGameSetup implements GameSetup {
	public void setup(List<Knight> knights, List<Ogre> ogres, int nKnights, int nBraveKnights, int nBadOgres) {
		int numberOfWarriors = nKnights + nBraveKnights;
		
		for (int i = 0; i < nKnights; ++i){
			if (i % 2 == 0) // we evenly alternate kinds of swords
				knights.add(new Knight(100, new LightSword(i)));
			else
				knights.add(new Knight(100, new IronSword(i)));				
		}
		
		for (int i = nKnights; i < numberOfWarriors; ++i){
			if (i % 2 == 0)
				knights.add(new BraveKnight(100, new LightSword(i)));
			else
				knights.add(new BraveKnight(100, new IronSword(i)));				
		}
		
		for (int i = 0; i < nBadOgres; ++i){
			ogres.add(new BadOgre(100));
		}
		
		for (int i = nBadOgres; i < numberOfWarriors; ++i){
			ogres.add(new Ogre(100));
		}
		
	}
}
