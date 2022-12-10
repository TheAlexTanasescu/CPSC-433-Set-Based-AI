/*  
Authors:
- Alex Tanasescu (30041538)
- Jordana Wintjes (30069597)
- Matthew Newton (30094756)
- Fu-Yin Lin (10132321)
- Tiffany Hung (10149429)
- Aleksandr Valianski (30055443)
Class: CPSC 433
Date: 2022-12-09

Program Description: hybrid set-based search system for finding an optimal 
assignment of games/practices to time slots in the week for the City of Calgary.
Specifically, the hybrid set-based search system uses a genetic algorithm with an
or-tree to validate schedules.
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

public class CPSC433F22Main {
	private static Individual result = null;
	public static void main(String[] args)  {
		
		// Check if command line argument has correct number of arguments
		if (!(args.length == 12 || args.length == 9))
		{
			System.out.println("Improper number of arguments");
			System.out.println("Please run again with a proper number of arguments");
			return;
		}
		
		// Checks if the first argument is a file
		File inputFile = new File(args[0]);
		boolean isFile = inputFile.isFile();
		
		if(!isFile)
		{
			System.out.println("Invalid file name");
			System.out.println("Please run again with a valid file name");
			return;
		}

		// If all the above checks are passed, parse input text file
		CPSC433F22Main scheduler = new CPSC433F22Main();
		Parser parser = null;
		try {
			parser = new Parser(args);
			Problem prob = parser.parse();
			printDebug(prob, false);

			// Initialize generation and population values 
			int ctr;
			int genCount = 0;
			boolean noValid = false;
			Population currentPop = new Population(prob);
			ctr = currentPop.control();
			
			// Generate next generation from current population until we reach max generations or no valid solutions
			// (i.e. this is fwert selecting which extension rule to use)
			while (genCount <= prob.getMaxGenerations() && !noValid) {
				// If current pop size is 0 (i.e. fwert evaluates to 0), use the create() extension rule
				if (ctr == 0) {
					currentPop = currentPop.create();
					// If the population still has no valid schedules after trying to create, exit loop
					if (currentPop == null) {
						noValid = true;
						break;
					}
				// If current pop size is greater than 0 (i.e. fwert evaluates to 1), use the existing() extension rule
				} else if (ctr == 1) {
					currentPop = currentPop.existing();
				}
				// If we find a schedule with the best fitness, exit loop
				if (currentPop.isBestFitFound()) break;
				// Otherwise, we increment counters
				ctr = currentPop.control();
				genCount++;
			}
			
			// If final generation is valid, output the schedule with the best fitness 
			if (!noValid) result = currentPop.getBestIndividual();
			scheduler.printResult();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	// Function for debugging
	private static void printDebug(Problem prob, Boolean debug)
	{
		
		if (debug)
		{
			System.out.println();
			System.out.println(prob.problemName);
			System.out.println();
			
			System.out.println("Gameslots:");
			for (GameSlot gs : prob.gameSlots)
				System.out.println(gs);
			System.out.println();
			
			System.out.println("Practiceslots:");
			for (PracticeSlot gs : prob.practiceSlots)
				System.out.println(gs);
			System.out.println();
			
			System.out.println("Games:");
			for (Game g : prob.games)
				System.out.println(g);
			System.out.println();
			
			System.out.println("Practices:");
			for (Practice p : prob.practices)
				System.out.println(p);
			System.out.println();
			
			
			System.out.println("Non-compatible Games:");
			for (Game g: prob.games) {
				System.out.println("Game " + g + " non-compatible with: ");
				for (Assignable a: g.notcompatible)
					System.out.println(a);
			}
			System.out.println();
			
			System.out.println("Non-compatible Practices:");
			for (Practice p: prob.practices) {
				System.out.println("Practice " + p + " non-compatible with: ");
				for (Assignable a: p.notcompatible)
					System.out.println(a);
			}
			System.out.println();
			
			System.out.println("Unwanted Games:");
			for (Game g: prob.games) {
				System.out.println("Game " + g + " unwanted in timeslot: ");
				for (TimeSlot a: g.unwanted)
					System.out.println(a);
			}
			System.out.println();

			System.out.println("Unwanted Practices:");
			for (Practice p: prob.practices) {
				System.out.println("Practice " + p + " unwanted in timeslot: ");
				for (TimeSlot a: p.unwanted)
					System.out.println(a);
			}
			System.out.println();

			
			
			System.out.println("Partially assigned Practices:");
			for (Practice p: prob.practices) {
				System.out.println("Practice " + p + " partially assigned to: " + p.getPartAssign());
				
			}
			System.out.println();

			
			System.out.println("Partially assigned Games:");
			for (Game g: prob.games) {
				System.out.println("Game " + g + " partially assigned to: " + g.getPartAssign());
				
			}
			System.out.println();

			 
			System.out.println("Practice Preferences:");
			for (Practice p: prob.practices)
			{
				for (Map.Entry<TimeSlot, Integer> a : p.preferences.entrySet())
					System.out.println("Practice "+ p+ " preferences: " + a.getKey() + ", "+ a.getValue());
				
			}
			System.out.println();

			System.out.println("Game Preferences:");
			for (Game g: prob.games)
			{
				for (Map.Entry<TimeSlot, Integer> a : g.preferences.entrySet())
					System.out.println("Practice "+ g+ " preferences: " + a.getKey() + ", "+ a.getValue());
				
			}
						System.out.println();

			
			
			System.out.println("Game Pairs :");
			for ( Game g : prob.games){
				for(Assignable a : g.pair)
				{
					System.out.println("Pair "+ g + " with " + a);
				}
				
			}
			System.out.println();

			System.out.println("Practice Pairs :");
			for ( Practice p : prob.practices){
				for(Assignable a : p.pair)
				{
					System.out.println("Pair "+ p + " with " + a);
				}
				
			}
			System.out.println();

			
		}
		
	}
	
	// Function for printing schedule of found solution
	private void printResult() {
		if (result == null) {
			System.out.println("No solution can be found...");
		} else {
			System.out.println("---------------------Result---------------------");
			System.out.println("Eval-value: " + result.getFitness());
			System.out.println(result);
		}
	}
}
