package game;

import characters.*;
import java.util.*;

public class ReversedGameSetup implements GameSetup {
	public void setup(List<Knight> knights, List<Ogre> ogres, int nKnights, int nBraveKnights, int nBadOgres) {
		int numberOfWarriors = nKnights + nBraveKnights;
		
		for (int i = 0; i < nBraveKnights; ++i){
			knights.add(new BraveKnight(100, new LightSword(i)));
		}

		for (int i = nBraveKnights; i < numberOfWarriors; ++i){
			knights.add(new Knight(80, new IronSword(i)));
		}

		for (int i = 0; i < nBadOgres; ++i){
			ogres.add(new BadOgre(100));
		}
		
		for (int i = nBadOgres; i < numberOfWarriors; ++i){
			ogres.add(new Ogre(100));
		}
	}
}
