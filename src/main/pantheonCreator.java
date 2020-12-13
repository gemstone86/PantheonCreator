
package main;

import gods.deity;

public class pantheonCreator {

	public static void main(String[] args) {
		//Handle in values
		for(String inArg:args) {
			switch(inArg) {
			case "g":
				try{

				}
				catch(Exception e) {
					System.out.println("Expected value after generation parameter");
				}
			}
		}

		//start program

		pantheonCreator program = new pantheonCreator();

		program.run();
	}


	public void run() {
		runtime runtime = new runtime();

		int generations = 10;
		
		for(int i = 0; i<generations; i++) {
			System.out.println("----" + "Generation " + i +"----");
			for(deity deity:runtime.getListOfDeities()) {
				deity.deityActs();
			}
			for(int j = 0; j<runtime.getListOfDeities().size();j++) {
				if(runtime.getListOfDeities().get(j).update()) {
					runtime.getListOfDeities().remove(j);
				}
				
			}
			for(deity newDeity:main.runtime.getNextGen()) {
				runtime.addDeity(newDeity);
			}
			main.runtime.resetNextGen();
		}
		System.out.println("---Final Pantheon---");
		for(deity deity:runtime.getListOfDeities()) {
			System.out.println("\t"+deity.getName() + " (DvR " + deity.getDvR()+")" + " (" +deity.getGenderPronoun(deity.getGender()) +") " + " "+deity.getDomains().toString());
			for(deity parent:deity.getParents()) {
				System.out.println("\t\tParent:" + parent.getName());
			}
		}
	}

}
