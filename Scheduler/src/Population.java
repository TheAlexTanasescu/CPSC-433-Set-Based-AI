import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Population {
	List<Individual> populationList = new ArrayList<Individual>();
	Problem prob;
	Random random = new Random();
	boolean isBestFitFound = false;

	public Population(Problem prob) {
		this.prob = prob;
	}
	
	public List<Individual> getPopulationList() {
		return populationList;
	}
	
	public Individual getBestIndividual() {
		Collections.sort(populationList, comparator);
		return populationList.get(0);
	}
	
	public boolean isBestFitFound() {
		return isBestFitFound;
	}

	// fwert
	public int control() {
		if (populationList.isEmpty()) return 0;
		else return 1;
	}
	
	public Population create() {
		populationList = new ArrayList<Individual>();
		Individual randSche;
//		System.out.println(populationList.size());
//		System.out.println(prob.getPopulationSize());
		while (populationList.size() < prob.getPopulationSize()) {
			randSche = new Individual(prob);
			randSche.random();
			if (randSche.getSchedule() != null) {
				populationList.add(randSche);
				if (randSche.isBestFitFound()) {
					isBestFitFound = true;
					break;
				}
			}
		}
		return this;
	}
	
	public Population existing() {
		List<Individual> currentPop = populationList;
		List<Individual> newPop = new ArrayList<Individual>();

		// ensuring that better fitted schedule has higher probably being selected
		RandomCollection<Individual> rc = new RandomCollection<Individual>();
		List<Integer> weights = calculateWeights(currentPop);
		
		for (int i = 0; i < currentPop.size(); i++) {
			rc.add(weights.get(i), currentPop.get(i));
		}
		
		// select a random schedule and perform mutation or crossover
		while (newPop.size() < prob.getPopulationSize() - 2) {
			Individual parent = rc.next();
			Individual child;
			if (parent.getFitness() > prob.getFitnessThreshold()) {
				// mutation
				child = mutation(parent);
			} else {
				// crossover
				child = crossover(parent);
			}
			if (child != null) newPop.add(child);
		}
		
		// add two best schedules
		Collections.sort(populationList, comparator);
		newPop.add(populationList.get(0));
		newPop.add(populationList.get(1));
		
		populationList = newPop;
		return this;
	}
	
	private List<Integer> calculateWeights(List<Individual> individual) {
		List<Integer> fitnesses = new ArrayList<Integer>();
		int fitnessTotal = individual.stream().mapToInt(Individual::getFitness).sum();
		for (Individual idv : individual) {
			fitnesses.add(fitnessTotal - idv.getFitness());
		}
		int sum = fitnesses.stream().mapToInt(Integer::intValue).sum();
		fitnesses.stream().map(n -> (n / sum) * 100);
		return fitnesses;
	}

	public Individual mutation(Individual toMutate) {
		int p = random.nextInt(toMutate.getSchedule().size());
		boolean isValid = toMutate.validate(toMutate.removeFact(p));
		
		if (isValid) return toMutate;
		else return null;
	}
	
	public Individual crossover(Individual toCrossOver) {
		Individual bestFit = getBestIndividual();
		if (bestFit.equals(toCrossOver)) bestFit = populationList.get(0);
		
		int c = random.nextInt(toCrossOver.getSchedule().size());
		boolean isValid = toCrossOver.validate(toCrossOver.swapFacts(c, bestFit));
		
		if (isValid) return toCrossOver;
		else return null;
	}
	
	private static Comparator<Individual> comparator = new Comparator<Individual>() {
		@Override
		public int compare(Individual o1, Individual o2) {
			return o2.getFitness() - o1.getFitness();
		}
    };
}
