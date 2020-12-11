package gods;

import java.util.LinkedList;

import behaviours.*;
import main.runtime;

public class deity {

	private String name;
	private String domains[];
	private int divineRank;
	private LinkedList<deity> parents;
	private LinkedList<deity> children;
	private LinkedList<behaviour> behaviours;
	private boolean mateFound = false;
	private int willingness;
	private deity mate;
	private int age = 0;
	
	
	private int sexuality;
	private int gender;

	private int random = (int) Math.random();

	public deity(String name, String[] domains, int DvR, int Sex, int sexuality) {
		this.name = name;
		this.domains = domains;
		this.divineRank = DvR;
		this.gender = Sex;
		this.sexuality = sexuality;

		behaviours = new LinkedList<behaviour>();

		behaviours.add(new findMate(this));
		behaviours.add(new createConcept(this));
		willingness = random(30,70);
	}
	
	public deity (String name, int DvR, int Sex, int Sexuality, LinkedList<deity> parents) {
		this.name = name;
		this.domains = domains;
		this.divineRank = DvR;
		this.gender = Sex;
		this.sexuality = sexuality;

		behaviours = new LinkedList<behaviour>();

		behaviours.add(new findMate(this));
		behaviours.add(new createConcept(this));
		willingness = random(30,70);		
	}

	public void addBehaviour(behaviour behaviour) {
		this.behaviours.add(behaviour);
	}

	public void deityActs(runtime runtime) {
		System.out.println("\t"+this.getName() + " acts");
		for(behaviour i:behaviours) {
			i.act(runtime);
		}
		age++;
	}

	public int getGender() {
		return gender;
	}

	public String name() {
		return name;
	}

	public int getSexuality() {
		return sexuality;
	}

	public boolean checkCompatibility(int suitorGender) {
		if((sexuality == 2) & suitorGender == (1|3) | sexuality == suitorGender) {
			return true;
		}
		else if(sexuality == 2 & suitorGender == 2 & (random(1,100) < 51)) {
			return true;
		}
		return false;
	}

	public boolean checkIfMateFound() {
		return mateFound;
	}
	public void setMateFound(boolean set, deity seducer) {
		mateFound = set;
		mate = seducer;
	}
	public void resetMate() {
		mateFound = false;
		mate = null;
	}
	
	public int random(int start, int end) {
		return (int) ((Math.random() * (end - start)) + start);
	}

	public String getName() {
		return name;
	}

	public LinkedList<deity> getChildren(){
		return children;
	}
	
	public int getDvR() {
		return divineRank;
	}
	
	public boolean seduce(deity seducer) {
		int modifier = 0;
		for(deity parent:parents) {
			if(parent == seducer) {
				modifier +=20;
			}
			if(parent.getChildren().contains(seducer)) {
				modifier +=5;
			}
		}
		
		if((random(1,100)+modifier) <= willingness) {
			return true;
		}
		return false;
	}

}
