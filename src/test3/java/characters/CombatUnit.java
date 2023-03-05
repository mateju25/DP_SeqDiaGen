package characters;

import java.util.*;

// The Composite pattern - the Component role

public interface CombatUnit {
	List<Knight> getWarriors();
	String report(String tab);
}
