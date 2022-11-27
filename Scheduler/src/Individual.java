import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Individual {
	private Problem prob;
	private ArrayList<Pair> scheduleInPair;
	private int fitness = Integer.MAX_VALUE;
	private Random rand = new Random();
	private int n_games;
	private int n_practices;
	private int n_gameslots;
	private int n_practiceslots;
	private Integer[] allGames;
	private Integer[] allPractices;
	private Integer[] allGTS;
	private Integer[] allPTS;
	
	int sg = 0;
	int sp = 0;

	public Individual(Problem prob) {
		this.prob = prob;
		initializeRandHelper();
		random();
	}
	
	public Problem getProb() {
		return prob;
	}
	
	public int getFitness() {
		return fitness;
	}
		
	public ArrayList<Pair> getSchedule() {
		return scheduleInPair;
	}
	
	public boolean isBestFitFound() {
		if (fitness == 0) return true;
		return false;
	}
	
	public ArrayList<Pair> removeFact(int index) {
		scheduleInPair.remove(index);
		return scheduleInPair;
	}
	
	public ArrayList<Pair> swapFacts(int index, Individual bestFit) {
		ArrayList<Pair> toCross = bestFit.getSchedule();
		scheduleInPair.subList(0, index).addAll(toCross.subList(index, toCross.size()));
		return scheduleInPair;
	}
	
	private void initializeRandHelper() {
		n_games = prob.games.size();
		allGames = new Integer[n_games];
		n_practices = prob.practices.size();
		allPractices = new Integer[n_practices];
		n_gameslots = prob.gameSlots.size();
		allGTS = new Integer[n_gameslots];
		n_practiceslots = prob.practiceSlots.size();
		allPTS = new Integer[n_practiceslots];
	}
	
	private void generateRandomSeq(Integer[] listToShuffle) {
		for (int i = 0; i < listToShuffle.length; i++) {
			listToShuffle[i] = i;
		}
		Collections.shuffle(Arrays.asList(listToShuffle));
	}
	
	public void random() {
		ArrayList<Pair> randomSchedule = new ArrayList<Pair>();
				
		// generate random
		generateRandomSeq(allGames);
		generateRandomSeq(allPractices);
		
		Assignable toAssign;
		int ag = 0;
		int ap = 0;
		
		for (int i = 0; i < n_games + n_practices; i++) {
			int next = rand.nextInt(2);
			if (next == 0) {
				toAssign = prob.games.get(allGames[ag]);
				ArrayList<TimeSlot> slots = gameSlotChoice(toAssign);
				for (TimeSlot slot : slots) {
					assignment(toAssign, slot, randomSchedule);
				}
				ag++;
			} else {
				toAssign = prob.practices.get(allPractices[ap]);
				TimeSlot slot = pracSlotChoice(toAssign);
				assignment(toAssign, slot, randomSchedule);
				ap++;
			}
		}
		
		// validate
		validate(randomSchedule);
	}
	
	private ArrayList<TimeSlot> gameSlotChoice(Assignable toAssign) {
		ArrayList<TimeSlot> slots = new ArrayList<TimeSlot>();
		int randSlot;
		
		if (toAssign.isSpecial()) {
			return gameSpecial(toAssign);
		}
		
		if (toAssign.getPartAssign() != null) {
			slots.add(toAssign.getPartAssign());
			return slots;
		}
		
		if (!toAssign.unwanted.isEmpty()) {
			randSlot = rand.nextInt(n_gameslots);
			TimeSlot slot = prob.gameSlots.get(randSlot);
			
			while (toAssign.unwanted.contains(slot)) {
				randSlot = rand.nextInt(n_gameslots);
				slot = prob.gameSlots.get(randSlot);
			}
			
			slots.add(slot);
			return slots;
		}
		
		randSlot = rand.nextInt(n_gameslots);
		slots.add(prob.gameSlots.get(randSlot));

		return slots;
	}

	private TimeSlot pracSlotChoice(Assignable toAssign) {
		TimeSlot slot;
		int randSlot;
		
		if (toAssign.getPartAssign() != null) {
			return toAssign.getPartAssign();
		}
		
		if (!toAssign.unwanted.isEmpty()) {
			randSlot = rand.nextInt(n_practiceslots);
			slot = prob.practiceSlots.get(randSlot);
			
			while (toAssign.unwanted.contains(slot)) {
				randSlot = rand.nextInt(n_practiceslots);
				slot = prob.practiceSlots.get(randSlot);
			}
			
			return slot;
		}
		
		randSlot = rand.nextInt(n_practiceslots);

		return prob.practiceSlots.get(randSlot);
	}
	
	private void assignment(Assignable toAssign, TimeSlot slot, ArrayList<Pair> schedule) {
		schedule.add(new Pair(toAssign, slot));
	}
	
	private void assignment(Assignable toAssign, TimeSlot slot, Map<TimeSlot, List<Assignable>> schedule) {
		if (schedule.containsKey(slot)) {
			schedule.get(slot).add(toAssign);
		} else {
			schedule.put(slot, new ArrayList<Assignable>());
			schedule.get(slot).add(toAssign);
		}
	}
		
	public boolean validate(ArrayList<Pair> scheduleToValidate) {
		ArrayList<Pair> tempScheduleInPair = new ArrayList<Pair>();
		Map<TimeSlot, List<Assignable>> tempSchedule = new HashMap<TimeSlot, List<Assignable>>();
		HashMap<Assignable, TimeSlot> givenSchedule = new HashMap<Assignable, TimeSlot>();
		HashMap<Assignable, TimeSlot> searchState = new HashMap<Assignable, TimeSlot>();
				
		boolean constr = true;
		for (Pair assigned : scheduleToValidate) {
			givenSchedule.put(assigned.first, assigned.second);
			searchState.put(assigned.first, null);
		}
		
		// if schedule is not full, randomly choose unassigns until full
		while (givenSchedule.size() < prob.getIndividualMax()) {
			Assignable toAssign;
			if (rand.nextInt(2) == 0) {
				toAssign = prob.games.get(rand.nextInt(n_games));
				while (givenSchedule.containsKey(toAssign)) {
					toAssign = prob.games.get(rand.nextInt(n_games));
				}
			} else {
				toAssign = prob.practices.get(rand.nextInt(n_practices));
				while (givenSchedule.containsKey(toAssign)) {
					toAssign = prob.practices.get(rand.nextInt(n_practices));
				}
			}
			givenSchedule.put(toAssign, null);
			searchState.put(toAssign, null);
		}
		
		// generate random
		generateRandomSeq(allGTS);
		generateRandomSeq(allPTS);
		
		for (Assignable assignable : searchState.keySet()) {
			// no valid schedule found
			if (!constr) break;
			sg = 0;
			sp = 0;
			
			// select a slot to proceed (predefined or random)
			TimeSlot toAssign = givenSchedule.get(assignable);
			if (toAssign != null) {
				toAssign = getNextSlot(assignable);
			}
			
			// loop until a valid slot is found or no valid slot can be found for current assignable
			while(sg < n_gameslots || sp < n_practiceslots) {
				// all passed, break and try the next assignable
				if (passedHardConstr(assignable, toAssign, tempSchedule)) {
					searchState.put(assignable, toAssign);
					assignment(assignable, toAssign, tempSchedule);
					assignment(assignable, toAssign, tempScheduleInPair);
					break;
				}
				
				// failed to pass all hard constraints, try the next slot
				toAssign = getNextSlot(assignable);
				
				// can't find a valid slot for this assignable, go the the next assignable
				if (sg == n_gameslots && sp == n_practiceslots) {
					constr = false;
				}
			}
		}
		
		if (constr) {
			scheduleInPair = tempScheduleInPair;
			evaluate(this);
		}
		else scheduleInPair = null;
		
		return constr;
	}
	
	private TimeSlot getNextSlot(Assignable assignable) {
		if (assignable instanceof Game) {
			sg++;
			return prob.gameSlots.get(allGTS[sg]);
		} else {
			sp++;
			return prob.practiceSlots.get(allPTS[sp]);
		}
	}
	
	private boolean passedHardConstr(Assignable assignable, TimeSlot toAssign, Map<TimeSlot, List<Assignable>> tempSchedule) {
		Day assignDay = toAssign.getDay();
		String assignTime = toAssign.getStartTime();
		
		return 
		specialCheck(assignable) &&
		eveningCheck(assignable, toAssign) &&
		meetingCheck(assignable, assignDay, assignTime) &&
		partAssignCheck(assignable, assignDay, assignTime) &&
		unwantedCheck(assignable, toAssign) &&
		maxCheck(assignable, toAssign, tempSchedule) &&
		overlapCheck(12, 1, assignable, toAssign, tempSchedule) &&
		overlapCheck(13, 1, assignable, toAssign, tempSchedule) &&
		divCheck(assignable, toAssign, tempSchedule) &&
		agetierCheck(assignable, toAssign, tempSchedule) &&
		notcompatibleCheck(assignable, toAssign, tempSchedule);
	}
	
	private ArrayList<TimeSlot> gameSpecial(Assignable toAssign) {
		ArrayList<TimeSlot> slots = new ArrayList<TimeSlot>();
		
		TimeSlot slot1 = new PracticeSlot(Day.TU, "1700");
		TimeSlot slot2 = new PracticeSlot(Day.TU, "1830");
		int index1 = prob.practiceSlots.indexOf(slot1);
		int index2 = prob.practiceSlots.indexOf(slot2);
		
		if (index1 != -1 && index2 != -1) {
			slots.add(prob.practiceSlots.get(index1));
			slots.add(prob.practiceSlots.get(index2));
		}
		
		return slots;
	}
	
	private boolean specialCheck(Assignable assignable) {
		// if special, check timeslot
		if (assignable.isSpecial()) {
			if (gameSpecial(assignable).size() != 2) {
				return false;
			}
		}
		return true;
	}
	
	private boolean eveningCheck(Assignable assignable, TimeSlot toAssign) {
		// if div 9, in evening slot
		if (assignable.getDiv() == 9) {
			if (!toAssign.isEvening()) return false;
		}
		return true;
	}
	
	private boolean meetingCheck(Assignable assignable, Day assignDay, String assignTime) {
		// if game, not on TU, 11:00
		if (assignable instanceof Game) {
			if (assignDay == Day.TU && assignTime == "1100") {
				return false;
			}
		}
		return true;
	}
	
	private boolean partAssignCheck(Assignable assignable, Day assignDay, String assignTime) {
		// if partassign, check fulfilled
		if (assignable.getPartAssign() != null) {
			if (assignDay != assignable.getPartAssign().getDay() || assignTime != assignable.getPartAssign().getStartTime()) {
				return false;
			}
		}
		return true;
	}
	
	private boolean unwantedCheck(Assignable assignable, TimeSlot toAssign) {
		// unwanted
		if (!assignable.unwanted.isEmpty()) {
			if (assignable.unwanted.contains(toAssign)) return false;
		}
		return true;
	}
	
	private boolean maxCheck(Assignable assignable, TimeSlot toAssign, Map<TimeSlot, List<Assignable>> tempSchedule) {
		// gamemax, practicemax
		if (tempSchedule.get(toAssign).size() >= toAssign.getMax()) {
			return false;
		}
		return true;
	}
	
	private boolean overlapCheck(int age, int tier, Assignable assignable, TimeSlot toAssign, Map<TimeSlot, List<Assignable>> tempSchedule) {
		// if U12T1 / U13T1, check overlap with special
		if (assignable.getAgeGroup() == age && assignable.getTier() == tier) {
			for (Assignable assigned : tempSchedule.get(toAssign)) {
				if (assigned.isSpecial() && assigned.getAgeGroup() == age && assignable.getTier() == tier) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean divCheck(Assignable assignable, TimeSlot toAssign, Map<TimeSlot, List<Assignable>> tempSchedule) {
		// game and practice for the same div are not overlap			
		for (Assignable assigned : tempSchedule.get(toAssign)) {
			if (assignable.getDiv() == assigned.getDiv()) {
				if (assignable.getLeagueId().equals(assigned.getLeagueId())) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean agetierCheck(Assignable assignable, TimeSlot toAssign, Map<TimeSlot, List<Assignable>> tempSchedule) {
		// if U15/U16/U17/U19 game, not overlap with other tiers
		if (assignable instanceof Game) {
			if (assignable.getAgeGroup() == 15 || assignable.getAgeGroup() == 16 || 
					assignable.getAgeGroup() == 17 || assignable.getAgeGroup() == 19) {
				for (Assignable assigned : tempSchedule.get(toAssign)) {
					if (assigned.getAgeGroup() == 15 || assigned.getAgeGroup() == 16 || 
							assigned.getAgeGroup() == 17 || assigned.getAgeGroup() == 19) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private boolean notcompatibleCheck(Assignable assignable, TimeSlot toAssign, Map<TimeSlot, List<Assignable>> tempSchedule) {
		// notcompatible
		if (!assignable.notcompatible.isEmpty()) {
			for (Assignable assigned : tempSchedule.get(toAssign)) {
				if (assignable.notcompatible.contains(assigned)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public int evaluate(Individual schedule) {
		ArrayList<Pair> scheduleList = schedule.getSchedule();
		int evalMinfilled = 0; // total penalties for not satisfying slot's minimum
		int evalPref = 0; 		// total penalties from not satisfying preferences
		int evalPair = 0; 		// total penalties from not satisfying pairs
		int evalSecdiff = 0; 	// total penalties from overlapping divisions

		// Part 1: Eval_minfill(assign) function
		int gameSlotScore = 0; // keeps track of penalties per game slot
		int gameSlotTotal = 0; // keeps track of penalties for all game slots in a schedule

		// loop through all game slots and compare number of assigned games to the slot's min 
		for (int i =0; i < n_gameslots; i++){
			int assignedGames = 0; 							// keeps track of assigned games to current slot 
			int gameslotMin = prob.gameSlots.get(i).getMin(); 	// keeps track of a slot's min games (i.e. gamemin(s))

			// loop through schedule to find all games assigned to current game slot
			for (int j=0; j < scheduleList.size(); j++){
				if (scheduleList.get(j).second.equals(prob.gameSlots.get(i))){
					assignedGames += 1;
				}
			}

			// add a pen_gamemin for every game under the slot's min
			if (assignedGames < gameslotMin) {
				gameSlotScore = (gameslotMin - assignedGames) * prob.getPenGameMin();
				gameSlotTotal += gameSlotScore;
			}	
		}

		int practiceSlotScore = 0; // keeps track of penalties per practice slot
		int practiceSlotTotal = 0; // keeps track of penalties for all game slots in a schedule

		// loop through all practice slots and compare number of assigned practices to the slot's min
		for (int i=0; i < n_practiceslots; i++){
			int assignedPractices = 0; 								// keeps track of assigned practices to current slot 
			int practiceSlotMin = prob.practiceSlots.get(i).getMin(); 	// keeps track of a slot's min practices (i.e. practicemin(s))

			for (int j=0; j < scheduleList.size(); j++){
				if (scheduleList.get(j).second.equals(prob.practiceSlots.get(i))){
					assignedPractices += 1;
				}
			}

			
			// add a pen_practicemin for every practice under the slot's min
			if (assignedPractices < practiceSlotMin) {
				practiceSlotScore = (practiceSlotMin - assignedPractices) * prob.getPenPracticeMin();
				practiceSlotTotal += practiceSlotScore;
			}
		}

		// total penalty points: Eval_minfill(assign) * w_minfilled
		evalMinfilled = (gameSlotTotal + practiceSlotTotal) * prob.getwMinFilled();


		// Part 2: Eval_pref(assign) function
		// note: pen_preferred = total points - fulfilled points
		int totalPoints = 0; 		// keeps track of total preference points from input file?
		int assignedPoints = 0; 	// keeps track of assigned preference points (i.e. game/practice assigned to preferred day+time)

		// loop through preferences to get total used ranking points? are we keeping list of preferences?
		// if hashmap not empty, add preference
		for (int i=0; i < scheduleList.size(); i++){
			for (Integer points : scheduleList.get(i).first.preferences.values()){
				totalPoints += points;
			}
		}

		// loop through schedule to get assigned games/practices and get assigned points
		for (int i=0; i < scheduleList.size(); i++){
			if (!scheduleList.get(i).first.preferences.isEmpty()){
				if(scheduleList.get(i).first.preferences.containsKey(scheduleList.get(i).second)){
					assignedPoints += scheduleList.get(i).first.preferences.get(scheduleList.get(i).second);
				}
			}
		}		
	
		// total penalty points: Eval_pref(assign) * w_pref
		evalPref = (totalPoints - assignedPoints) * prob.getwPref();

		// Part 3: Eval_pair(assign) function
		Map<TimeSlot, List<Assignable>> transformSchedule = transformSchedule(scheduleList);

		// loop through schedule to get pairs
		for (Pair assigned : scheduleList) {
			if (!assigned.first.pair.isEmpty()) {
				for (Assignable assignInSlot : transformSchedule.get(assigned.second)) {
					if (!assigned.first.pair.contains(assignInSlot)) {
						evalPair += prob.getPenNotPaired();
					}
				}
			}
		}

		// total penalty points: Eval_pair(assign) * w_pair
		evalPair *= prob.getwPair();

		// Part 4: Eval_secdiff(assign) function 
		for (Pair assigned : scheduleList) {
			int ageGroup = assigned.first.getAgeGroup();
			int tier = assigned.first.getTier();
			if (assigned.first instanceof Game) {
				for (Assignable assignInSlot : transformSchedule.get(assigned.second)) {
					if (assignInSlot instanceof Game && assignInSlot.getAgeGroup() == ageGroup && assignInSlot.getTier() == tier) {
						evalSecdiff += prob.getPenSection();						
					}
				}
			}
		}		

		// total penalty points: Eval_secdiff(assign) * w_secdiff
		evalSecdiff *= prob.getwSecDiff();

		// Part 5: complete Eval(assign) function 
		fitness = evalMinfilled + evalPref + evalPair + evalSecdiff;

		return fitness;
	}
	
	private Map<TimeSlot, List<Assignable>> transformSchedule(ArrayList<Pair> schedule) {
		Map<TimeSlot, List<Assignable>> transformedSchedule = new HashMap<TimeSlot, List<Assignable>>();
		for (Pair assigned : schedule) {
			if (transformedSchedule.containsKey(assigned.second)) {
				transformedSchedule.get(assigned.second).add(assigned.first);
			} else {
				transformedSchedule.put(assigned.second, new ArrayList<Assignable>());
				transformedSchedule.get(assigned.second).add(assigned.first);
			}			
		}
		return transformedSchedule;
	}

	private static Comparator<Pair> comparator = new Comparator<Pair>() {
		@Override
		public int compare(Pair o1, Pair o2) {
			return (o1.first.getLeagueId()).compareToIgnoreCase(o2.first.getLeagueId());
		}
    };
	
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		Collections.sort(scheduleInPair, comparator);
		
		for (Pair assigned : scheduleInPair) {
			output.append(assigned.first);
			output.append(" :");
			output.append(assigned.second);
		}
		
		return output.toString();
	}
}
