import java.util.ArrayList;
import java.util.List;

public class Population {
	List<Individual> population;

	public Population() {
		population = new ArrayList<Individual>();
	}
	
	public List<Individual> getPopulation() {
		return population;
	}

	public void addIndividual(Individual schedule) {
		
	}
	
	public int fwert(Population current) {
		if (current.getPopulation().isEmpty()) return 0;
		else return 1;
	}
	
	public Population create() {
		return null;
	}
	
	public Population existing() {
		return null;
	}

	public Individual mutation() {
		return null;
	}
	
	public Individual crossover() {
		return null;
	}
}
