package characters;

import java.util.*;

// The Composite pattern - the Composite role
// A warrior squad consists of any combat units,
// i.e., further warrior squads or individual warriors

public class WarriorSquad implements CombatUnit {
	private String name;
	private List<CombatUnit> units = new ArrayList<>();
	
	public WarriorSquad(String name) {
		this.name = name;
	}
	
	public void addCombatUnit(CombatUnit unit) {
		units.add(unit);
	}
	
	public List<Knight> getWarriors() {		
		List<Knight> individuals = new ArrayList<>();
		
		for (CombatUnit e : units)
			individuals.addAll(e.getWarriors());
		
		return individuals;
	}

	public String report(String tab) {		
		String report = name + ":\n";
		
		for (CombatUnit e : units)
			report += tab + e.report(tab + "\t") + "\n";
		
		return report;
	}
}
