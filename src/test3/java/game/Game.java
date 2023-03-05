package game;

import characters.*;
import java.util.*;
import java.io.*;
import SLList.*;

public class Game implements Serializable {
	private List<Ogre> ogres;
	private List<Knight> knights;

	transient private Iterator<Ogre> iterOgres;
	transient private Iterator<Knight> iterKnights;
	
	transient private Ogre fightingOgre;
	transient private Knight fightingKnight;

	private int numberOfWarriors;

//	transient private ArrayList<ClashFollower> clashFollowers = new ArrayList<>();
	transient private SLList<ClashFollower> clashFollowers = new SLList<ClashFollower>();
	
	public int getNumberOfWarriors() {
		return numberOfWarriors;
	}
	
	static int findEnergyDifference(Energy being1, Energy being2) {
		return being1.getEnergy() - being2.getEnergy();
	}

	public void createWarriors(int nKnights, int nBraveKnights, int nBadOgres, GameSetup gameSetup) {
		numberOfWarriors = nKnights + nBraveKnights;
		knights = new ArrayList<>();
		ogres = new ArrayList<>();

		gameSetup.setup(knights, ogres, nKnights, nBraveKnights, nBadOgres);

		informClashFollowers();
	}
	
	public Game() {
	}

	public Game(int nKnights, int nBraveKnights, int nBadOgres, GameSetup gameSetup) {
		createWarriors(nKnights, nBraveKnights, nBadOgres, gameSetup);
	}

	public void clash1on1(Knight knight, Ogre ogre) {
		knight.attack(ogre);
		informClashFollowers();
	}

	public String nextClash1on1() {
		fightingKnight = iterKnights.next();
		fightingOgre = iterOgres.next();
		
		clash1on1(fightingKnight, fightingOgre);
		
		return knights.indexOf(fightingKnight) + ":"
			+ "knight " + fightingKnight.getEnergy()
			+ " / " + "ogre " + fightingOgre.getEnergy()
			+ " " + fightingKnight.showSword()
			+ "\n";
	}
	
	public String clash() {
		String report = "";

		iterOgres = ogres.iterator();
		iterKnights = knights.iterator();
		
		for (int i = 0; i < numberOfWarriors; ++i)
			report = report + nextClash1on1();
		
		informClashFollowers();

		return report;
	}
	
	public void followClash(ClashFollower clashFollower) {
//		clashFollowers.add(clashFollower);
		clashFollowers.tailInsert(clashFollower);
	}
	
	public void informClashFollowers() {
//		for (ClashFollower clashFollower : clashFollowers)
//			clashFollower.inform();
		SLLElement<ClashFollower> clashFollower = clashFollowers.getHead();
		
		while (clashFollower != null) {
			clashFollower.getData().inform();
			clashFollower = clashFollowers.getNext(clashFollower);
		}			
	}
	
/*
	public int ogresEnergy() {
		int energy = 0;
		
		for (Energy o : ogres)
			energy += o.getEnergy();
		
		return energy;
	}

	public int knightsEnergy() {
		int energy = 0;
		
		for (Energy k : knights)
			energy += k.getEnergy();
		
		return energy;
	}
*/
//	private int calculateEnergy(List<Energy> list) {
	private int calculateEnergy(List<? extends Energy> list) {
		int energy = 0;
		
		for (Energy e : list)
			energy += e.getEnergy();
		
		return energy;
	}
	
	public int ogresEnergy() {
		return calculateEnergy(ogres);
	}

	public int knightsEnergy() {
		return calculateEnergy(knights);
	}

	public void save() throws ClassNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("game.out"));
		out.writeObject(this);
		out.close();
	}
	
	public void load() throws ClassNotFoundException, IOException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream("game.out"));
		Game loadedGame = (Game) in.readObject();
		in.close();
		
		ogres = loadedGame.ogres;
		knights = loadedGame.knights;
		numberOfWarriors = loadedGame.numberOfWarriors;

		iterOgres = ogres.iterator();
		iterKnights = knights.iterator();
		
		informClashFollowers();
	}
}

