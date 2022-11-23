import java.util.HashMap;
import java.util.List;

public class Individual {
	HashMap<TimeSlot, List<Assignable>> schedule;
	double fitness;

	public Individual() {
		random();
	}
	
	public Individual(Individual schedule) {
		validate(schedule);
	}
	
	public Individual random() {
		return null;
	}
	
	public Individual validate(Individual schedule) {
		return null;
	}
	
	public double evaluate(Individual schedule) {
		
		return 0.0;
	}

}
