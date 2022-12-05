import java.io.FileNotFoundException;
import java.util.Map;

public class Scheduler {
	private static Individual result = null;

	public static void main(String[] args)  {
		
		//System.out.println("Args Length: " + args.length);
		
		
		if (!(args.length == 12 || args.length == 9))
		{
			System.out.println("Improper Arguments");
			return;
		
		}
		
		Scheduler scheduler = new Scheduler();
		Parser parser = null;
		try {
			parser = new Parser(args);
			Problem prob = parser.parse();
			printDebug(prob, false);
			
			
				
				/*
			System.out.println(prob.problemName);
			System.out.println();
			
			for (GameSlot gs : prob.gameSlots)
				System.out.println(gs);
			System.out.println();
			
			for (PracticeSlot gs : prob.practiceSlots)
				System.out.println(gs);
			System.out.println();
			
			for (Game g : prob.games)
				System.out.println(g);
			System.out.println();
			
			for (Practice p : prob.practices)
				System.out.println(p);
			System.out.println();
			*/
			/*
			for (Game g: prob.games) {
				System.out.println("Game " + g + " non-compatible with: ");
				for (Assignable a: g.notcompatible)
					System.out.println(a);
			}
			
			for (Practice p: prob.practices) {
				System.out.println("Practice " + p + " non-compatible with: ");
				for (Assignable a: p.notcompatible)
					System.out.println(a);
			}
			*/
			/*
			for (Game g: prob.games) {
				System.out.println("Game " + g + " unwanted in timeslot: ");
				for (TimeSlot a: g.unwanted)
					System.out.println(a);
			}
			
			for (Practice p: prob.practices) {
				System.out.println("Practice " + p + " unwanted in: ");
				for (TimeSlot a: p.unwanted)
					System.out.println(a);
			}
			*/
			
			/*
			for (Practice p: prob.practices) {
				System.out.println("Practice " + p + " partially assigned to: " + p.getPartAssign());
				
			}
			
			for (Game g: prob.games) {
				System.out.println("Game " + g + " partially assigned to: " + g.getPartAssign());
				
			}
			 */
			/*
			for (Practice p: prob.practices)
			{
				for (Map.Entry<TimeSlot, Integer> a : p.preferences.entrySet())
					System.out.println("Practice "+ p+ " preferences: " + a.getKey() + ", "+ a.getValue());
				
			}
			
			for (Game g: prob.games)
			{
				for (Map.Entry<TimeSlot, Integer> a : g.preferences.entrySet())
					System.out.println("Practice "+ g+ " preferences: " + a.getKey() + ", "+ a.getValue());
				
			}
			*/
			
			/*
			for ( Game g : prob.games){
				for(Assignable a : g.pair)
				{
					System.out.println("Pair "+ g + " with " + a);
				}
				
			}
			
			for ( Practice p : prob.practices){
				for(Assignable a : p.pair)
				{
					System.out.println("Pair "+ p + " with " + a);
				}
				
			}
			*/
			int ctr;
			int genCount = 0;
			boolean noValid = false;
			Population currentPop = new Population(prob);
			ctr = currentPop.control();
			
			while (genCount <= prob.getMaxGenerations() && !noValid) {
				if (ctr == 0) {
					currentPop = currentPop.create();
					if (currentPop == null) {
						noValid = true;
						break;
					}
				} else if (ctr == 1) {
					currentPop = currentPop.existing();
				}
				
				if (currentPop.isBestFitFound()) break;
				ctr = currentPop.control();
				genCount++;
			}
			
			if (!noValid) result = currentPop.getBestIndividual();
			scheduler.printResult();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		
		
	}
	
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
