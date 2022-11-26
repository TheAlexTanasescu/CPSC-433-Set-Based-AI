import java.util.ArrayList;

public class Parser {
	

	public Parser() {

	}
	
	public Problem parse() {
		// parse command line weight and penalties
		// population size needs to be greater than 2, if no entry or input <= 2, use default value
		
		
		// create problem object
		Problem prob = new Problem();
		
		
		// initialize time slots
		initializeTimeSlots(prob);
		
		// parse text file
		
		
		return prob;
	}
	
	private void timeSlotsMWF(Day day, int start, int inc, String type, Problem prob) {
		int endMWF = 2000;
		
		if (type.equals("game")) {
			for (int i = start; i < endMWF; i += inc) {
				prob.gameSlots.add(new GameSlot(day, Integer.toString(i)));
			}
		} else if (type.equals("practice")) {
			for (int i = start; i < endMWF; i += inc) {
				prob.practiceSlots.add(new PracticeSlot(day, Integer.toString(i)));
			}
		}
	}
	
	private void initializeTimeSlots(Problem prob) {
		int start = 800;
		int endTT = 1830;
		int incTT = 300;
				
		timeSlotsMWF(Day.MO, start, 100, "game", prob);
		timeSlotsMWF(Day.MO, start, 100, "practice", prob);
		timeSlotsMWF(Day.TU, start, 100, "practice", prob);
		timeSlotsMWF(Day.FR, start, 200, "practice", prob);
		
		// Tuesday games
		for (int i = start; i < endTT; i += incTT) {
			prob.gameSlots.add(new GameSlot(Day.TU, Integer.toString(i)));
			prob.gameSlots.add(new GameSlot(Day.TU, Integer.toString(i + 130)));
		}
	}
}
