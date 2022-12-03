import java.io.FileNotFoundException;

public class Scheduler {
	// parse notes
	// validate preferences
	// validate partial assignment
	// return problem
	private static Individual result = null;

	public static void main(String[] args)  {
		Scheduler scheduler = new Scheduler();
		Parser parser = null;
		try {
			parser = new Parser(args);
			Problem prob = parser.parse();

			int ctr;
			int genCount = 0;
			boolean noValid = false;
			Population currentPop = new Population(prob);
			ctr = currentPop.control();
//			System.out.println(ctr);
			
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
//			System.out.println(genCount);
			
			if (!noValid) result = currentPop.getBestIndividual();
			scheduler.printResult();
		} catch (FileNotFoundException e) {
			
		}
		
	}
	
	private void printResult() {
		if (result == null) {
			System.out.println("No solution can be found...");
		} else {
			System.out.println("Eval-value: " + result.getFitness());
			System.out.println(result);
		}
	}
}
