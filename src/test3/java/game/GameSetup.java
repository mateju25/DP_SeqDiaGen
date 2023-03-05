package game;

import java.util.*;
import characters.*;


// Strategy: differnet strategies of setting up a game
public interface GameSetup {
	void setup(List<Knight> knights, List<Ogre> ogres, int nKnights, int nBraveKnights, int nBadOgres);	
}
