
public class Scheduler {
	// parse notes
	// validate preferences
	// validate partial assignment
	// return problem
	private static Individual result = null;

	public static void main(String[] args) {
		Scheduler scheduler = new Scheduler();
		Parser parser = new Parser();
		Problem prob = parser.parse();
		int ctr;
		int genCount = 0;
		
		Population currentPop = new Population(prob);
		ctr = currentPop.control();
		
		while (genCount < prob.getMaxGenerations()) {
			if (ctr == 0) {
				currentPop = currentPop.create();
				
			} else if (ctr == 1) {
				currentPop = currentPop.existing();
			}
			
			if(currentPop.isBestFitFound()) break;
			genCount++;
		}
		
		result = currentPop.getBestIndividual();
		scheduler.printResult();
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
