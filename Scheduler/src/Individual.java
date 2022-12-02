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
	private int nGames;
	private int nPractices;
	private int nGameslots;
	private int nPracticeslots;
	private Integer[] allGames;
	private Integer[] allPractices;
	private Integer[] allGTS;
	private Integer[] allPTS;
	
	int sg = 0;
	int sp = 0;

	public Individual(Problem prob) {
		this.prob = prob;
		initializeRandHelper();
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
		nGames = prob.games.size();
		allGames = new Integer[nGames];
		nPractices = prob.practices.size();
		allPractices = new Integer[nPractices];
		nGameslots = prob.gameSlots.size();
		allGTS = new Integer[nGameslots];
		nPracticeslots = prob.practiceSlots.size();
		allPTS = new Integer[nPracticeslots];
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
		
//		for (int n : allGames) {
//			System.out.print(n + ", ");
//		}
//		System.out.println("end");
//		for (int n : allPractices) {
//			System.out.print(n + ", ");
//		}
//		System.out.println("end");
		
		Assignable toAssign;
		int ag = 0;
		int ap = 0;
		int i = 0;
		
		while (i < nGames + nPractices) {
//			System.out.print(i);
			int next = rand.nextInt(2);
//			System.out.println(": " + next);
			if (next == 0 && ag < nGames) {
//				System.out.println(allGames[ag]);
				toAssign = prob.games.get(allGames[ag]);
				TimeSlot slot = gameSlotChoice(toAssign);
				assignment(toAssign, slot, randomSchedule);
				ag++;
				i++;
			} else if (next == 1 && ap < nPractices) {
//				System.out.println(allPractices[ap]);
				toAssign = prob.practices.get(allPractices[ap]);
				TimeSlot slot = pracSlotChoice(toAssign);
				assignment(toAssign, slot, randomSchedule);
				ap++;
				i++;
			}
		}
		
		// validate
		validate(randomSchedule);
	}
	
	private TimeSlot gameSlotChoice(Assignable toAssign) {
		int randSlot;
		
		if (toAssign.isSpecial()) {
			return gameSpecial(toAssign);
		}
		
		if (toAssign.getPartAssign() != null) {
			return toAssign.getPartAssign();
		}
		
		if (!toAssign.unwanted.isEmpty()) {
			randSlot = rand.nextInt(nGameslots);
			TimeSlot slot = prob.gameSlots.get(randSlot);
			while (toAssign.unwanted.contains(slot) || slot.getMax() == 0) {
				randSlot = rand.nextInt(nGameslots);
				slot = prob.gameSlots.get(randSlot);
			}
			return slot;
		}
		
		randSlot = rand.nextInt(nGameslots);
		while (prob.gameSlots.get(randSlot).getMax() == 0) {
			randSlot = rand.nextInt(nGameslots);
		}
		return prob.gameSlots.get(randSlot);
	}
	
	private TimeSlot pracSlotChoice(Assignable toAssign) {
		TimeSlot slot;
		int randSlot;
		
		if (toAssign.getPartAssign() != null) {
			return toAssign.getPartAssign();
		}
		
		if (!toAssign.unwanted.isEmpty()) {
			randSlot = rand.nextInt(nPracticeslots);
			slot = prob.practiceSlots.get(randSlot);
			while (toAssign.unwanted.contains(slot) || slot.getMax() == 0) {
				randSlot = rand.nextInt(nPracticeslots);
				slot = prob.practiceSlots.get(randSlot);
			}
			return slot;
		}
		
		randSlot = rand.nextInt(nPracticeslots);
		while (prob.practiceSlots.get(randSlot).getMax() == 0) {
			randSlot = rand.nextInt(nPracticeslots);
		}
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
				
		for (Pair assigned : scheduleToValidate) {
//			System.out.println(assigned.first + ":" + assigned.second);
			givenSchedule.put(assigned.first, assigned.second);
			searchState.put(assigned.first, null);
		}
		
		// if schedule is not full, randomly choose unassigns until full
		while (givenSchedule.size() < prob.getIndividualMax()) {
			System.out.println("schedule not full");
			Assignable toAssign;
			if (rand.nextInt(2) == 0) {
				toAssign = prob.games.get(rand.nextInt(nGames));
				while (givenSchedule.containsKey(toAssign)) {
					toAssign = prob.games.get(rand.nextInt(nGames));
				}
			} else {
				toAssign = prob.practices.get(rand.nextInt(nPractices));
				while (givenSchedule.containsKey(toAssign)) {
					toAssign = prob.practices.get(rand.nextInt(nPractices));
				}
			}
			givenSchedule.put(toAssign, null);
			searchState.put(toAssign, null);
		}
		
		for (TimeSlot slot : prob.gameSlots) {
			if (!tempSchedule.containsKey(slot)) {
				tempSchedule.put(slot, new ArrayList<Assignable>());
			}
		}
		
		for (TimeSlot slot : prob.practiceSlots) {
			if (!tempSchedule.containsKey(slot)) {
				tempSchedule.put(slot, new ArrayList<Assignable>());
			}
		}
		
//		System.out.println(givenSchedule.size());
//		System.out.println(prob.getIndividualMax());
		
		boolean constr = true;
		
		// generate random
		generateRandomSeq(allGTS);
		generateRandomSeq(allPTS);

		for (Assignable assignable : searchState.keySet()) {
			// no valid schedule found
			if (!constr) break;
			sg = 0;
			sp = 0;
//			System.out.println(assignable);
			// select a slot to proceed (predefined or random)
			TimeSlot toAssign = givenSchedule.get(assignable);
//			System.out.println(toAssign);
			if (toAssign != null) {
				toAssign = getNextSlot(assignable);
			}
			
//			System.out.println(n_gameslots + " + " + n_practiceslots);
			
			// loop until a valid slot is found or no valid slot can be found for current assignable
			while (sg < nGameslots && sp < nPracticeslots) {
				// can't find a valid slot for this assignable, go the the next assignable
				if (sg >= nGameslots || sp >= nPracticeslots) {
					constr = false;
					break;
				}
//				System.out.println(sg + ": " + sp);
				// all passed, break and try the next assignable
				if (passedHardConstr(assignable, toAssign, tempSchedule, searchState)) {
					searchState.put(assignable, toAssign);
					assignment(assignable, toAssign, tempSchedule);
					assignment(assignable, toAssign, tempScheduleInPair);
					break;
				}
				
				// failed to pass all hard constraints, try the next slot
				toAssign = getNextSlot(assignable);
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
		TimeSlot slot;
		if (assignable instanceof Game) {
			slot = prob.gameSlots.get(allGTS[sg]);
			sg++;
		} else {
			slot = prob.practiceSlots.get(allPTS[sp]);
			sp++;
		}
		return slot;
	}
	
	private boolean passedHardConstr(Assignable assignable, TimeSlot toAssign, 
			Map<TimeSlot, List<Assignable>> tempSchedule, Map<Assignable, TimeSlot> searchState) {
		if (toAssign == null) return false;
		Day assignDay = toAssign.getDay();
		String assignTime = toAssign.getStartTime();
		
		return 
		specialCheck(assignable) &&
		eveningCheck(assignable, toAssign) &&
		meetingCheck(assignable, assignDay, assignTime) &&
		partAssignCheck(assignable, assignDay, assignTime) &&
		unwantedCheck(assignable, toAssign) &&
		maxCheck(assignable, toAssign, tempSchedule) &&
		specialOverlapCheck(12, 1, assignable, toAssign, tempSchedule) &&
		specialOverlapCheck(13, 1, assignable, toAssign, tempSchedule) &&
		divCheck(assignable, toAssign, tempSchedule, searchState) &&
		agetierCheck(assignable, toAssign, tempSchedule) &&
		notcompatibleCheck(assignable, toAssign, tempSchedule, searchState);
	}
	
	private TimeSlot gameSpecial(Assignable toAssign) {
		TimeSlot slot = new PracticeSlot(Day.TU, "1800");
		int index = prob.practiceSlots.indexOf(slot);
		if (index != -1 && prob.practiceSlots.get(index).getMax() > 0) return prob.practiceSlots.get(index);
		return null;
	}
	
	private boolean specialCheck(Assignable assignable) {
		// if special, check timeslot
		if (assignable.isSpecial()) {
			if (gameSpecial(assignable) == null) {
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
		if (tempSchedule.get(toAssign) != null) {
			if (toAssign.getMax() == 0 || tempSchedule.get(toAssign).size() >= toAssign.getMax()) {
				return false;
			}
		}
		return true;
	}
	
	private boolean specialOverlapCheck(int age, int tier, Assignable assignable, TimeSlot toAssign, 
			Map<TimeSlot, List<Assignable>> tempSchedule) {
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
	
	private boolean divCheck(Assignable assignable, TimeSlot toAssign, 
			Map<TimeSlot, List<Assignable>> tempSchedule, Map<Assignable, TimeSlot> searchState) {
		// game and practice for the same div are not overlap		
		for (Assignable assigned : tempSchedule.get(toAssign)) {
			if (assignable.getDiv() == assigned.getDiv()) {
				if (assignable.getLeagueId().equals(assigned.getLeagueId()) || isOverlap(toAssign, searchState.get(assigned))) {
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
	
	private boolean notcompatibleCheck(Assignable assignable, TimeSlot toAssign, 
			Map<TimeSlot, List<Assignable>> tempSchedule, Map<Assignable, TimeSlot> searchState) {
		// notcompatible
		if (!assignable.notcompatible.isEmpty()) {
			for (Assignable assigned : tempSchedule.get(toAssign)) {
				if (assignable.notcompatible.contains(assigned) || isOverlap(toAssign, searchState.get(assigned))) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean isOverlap(TimeSlot t1, TimeSlot t2) {
		if (t1 instanceof GameSlot && t2 instanceof PracticeSlot) {
			if (t1.getDay() == Day.MO && t2.getDay() == Day.MO) {
				if (t1.getStartTime().equals(t2.getStartTime())) return true;
			} else if (t1.getDay() == Day.MO && t2.getDay() == Day.FR) {
				switch(t1.getStartTime()) {
					case "800":
						if (t2.getStartTime().equals("800")) return true;
					case "900":
						if (t2.getStartTime().equals("800")) return true;
					case "1000":
						if (t2.getStartTime().equals("1000")) return true;
					case "1100":
						if (t2.getStartTime().equals("1000")) return true;
					case "1200":
						if (t2.getStartTime().equals("1200")) return true;
					case "1300":
						if (t2.getStartTime().equals("1200")) return true;
					case "1400":
						if (t2.getStartTime().equals("1400")) return true;
					case "1500":
						if (t2.getStartTime().equals("1400")) return true;
					case "1600":
						if (t2.getStartTime().equals("1600")) return true;
					case "1700":
						if (t2.getStartTime().equals("1600")) return true;
					case "1800":
						if (t2.getStartTime().equals("1800")) return true;
					case "1900":
						if (t2.getStartTime().equals("1800")) return true;
					default: break;
				}
			} else if (t1.getDay() == Day.TU && t2.getDay() == Day.TU) {
				switch(t1.getStartTime()) {
					case "800":
						if (t2.getStartTime().equals("800") || t2.getStartTime().equals("900")) return true;
					case "930":
						if (t2.getStartTime().equals("900") || t2.getStartTime().equals("1000")) return true;
					case "1100":
						if (t2.getStartTime().equals("1100") || t2.getStartTime().equals("1200")) return true;
					case "1230":
						if (t2.getStartTime().equals("1200") || t2.getStartTime().equals("1300")) return true;
					case "1400":
						if (t2.getStartTime().equals("1400") || t2.getStartTime().equals("1500")) return true;
					case "1530":
						if (t2.getStartTime().equals("1500") || t2.getStartTime().equals("1600")) return true;
					case "1700":
						if (t2.getStartTime().equals("1700") || t2.getStartTime().equals("1800")) return true;
					case "1830":
						if (t2.getStartTime().equals("1800") || t2.getStartTime().equals("1900")) return true;
					default: break;
				}
			}
		} else if (t1 instanceof PracticeSlot && t2 instanceof GameSlot) {
			if (t1.getDay() == Day.MO && t2.getDay() == Day.MO) { 
				if (t1.getStartTime().equals(t2.getStartTime())) return true;
			} else if (t1.getDay() == Day.FR && t2.getDay() == Day.MO) { 
				switch(t1.getStartTime()) {
				case "800":
					if (t2.getStartTime().equals("800") || t2.getStartTime().equals("900")) return true;
				case "1000":
					if (t2.getStartTime().equals("1000") || t2.getStartTime().equals("1100")) return true;
				case "1200":
					if (t2.getStartTime().equals("1200") || t2.getStartTime().equals("1300")) return true;
				case "1400":
					if (t2.getStartTime().equals("1400") || t2.getStartTime().equals("1500")) return true;
				case "1600":
					if (t2.getStartTime().equals("1600") || t2.getStartTime().equals("1700")) return true;	
				case "1800":
					if (t2.getStartTime().equals("1800") || t2.getStartTime().equals("1900")) return true;
				default: break;
			}
		} else if (t1.getDay() == Day.TU && t2.getDay() == Day.TU) {
				switch(t1.getStartTime()) {
					case "800":
						if (t2.getStartTime().equals("800")) return true;
					case "900":
						if (t2.getStartTime().equals("800") || t2.getStartTime().equals("930")) return true;
					case "1000":
						if (t2.getStartTime().equals("930")) return true;
					case "1100":
						if (t2.getStartTime().equals("1100")) return true;
					case "1200":
						if (t2.getStartTime().equals("1100") || t2.getStartTime().equals("1230")) return true;
					case "1300":
						if (t2.getStartTime().equals("1230")) return true;
					case "1400":
						if (t2.getStartTime().equals("1400")) return true;
					case "1500":
						if (t2.getStartTime().equals("1400") || t2.getStartTime().equals("1530")) return true;
					case "1600":
						if (t2.getStartTime().equals("1530")) return true;
					case "1700":
						if (t2.getStartTime().equals("1700")) return true;
					case "1800":
						if (t2.getStartTime().equals("1700") || t2.getStartTime().equals("1830")) return true;
					case "1900":
						if (t2.getStartTime().equals("1830")) return true;
					default: break;
				}
			}
		}
		return false;
	}
	
	public int evaluate(Individual schedule) {
		ArrayList<Pair> scheduleList = schedule.getSchedule();
		int evalMinfilled = 0; // total penalties for not satisfying slot's minimum
		int evalPref = 0; 		// total penalties from not satisfying preferences
		int evalPair = 0; 		// total penalties from not satisfying pairs
		int evalSecdiff = 0; 	// total penalties from overlapping divisions
		
		Map<TimeSlot, List<Assignable>> transformScheduleTimeSlot = transformScheduleTimeSlot(scheduleList);
		Map<Assignable, TimeSlot> transformScheduleAssignable = transformScheduleAssignable(scheduleList);

		// Part 1: Eval_minfill(assign) function
//		int gameSlotScore = 0; // keeps track of penalties per game slot
		int gameSlotTotal = 0; // keeps track of penalties for all game slots in a schedule
//		int practiceSlotScore = 0; // keeps track of penalties per practice slot
		int practiceSlotTotal = 0; // keeps track of penalties for all game slots in a schedule
		
		for (TimeSlot slot : transformScheduleTimeSlot.keySet()) {
			if (slot instanceof GameSlot) {
				if (transformScheduleTimeSlot.get(slot).size() < slot.getMin()) {
					gameSlotTotal += (slot.getMin() - transformScheduleTimeSlot.get(slot).size()) * prob.getPenGameMin();;
				}
			} else {
				if (transformScheduleTimeSlot.get(slot).size() < slot.getMin()) {
					practiceSlotTotal += (slot.getMin() - transformScheduleTimeSlot.get(slot).size()) * prob.getPenPracticeMin();;
				}
			}
		}

//		// loop through all game slots and compare number of assigned games to the slot's min 
//		for (int i = 0; i < nGameslots; i++){
//			int assignedGames = 0; 							// keeps track of assigned games to current slot 
//			int gameslotMin = prob.gameSlots.get(i).getMin(); 	// keeps track of a slot's min games (i.e. gamemin(s))
//
//			// loop through schedule to find all games assigned to current game slot
//			for (int j = 0; j < scheduleList.size(); j++){
//				if (scheduleList.get(j).second.equals(prob.gameSlots.get(i))){
//					assignedGames += 1;
//				}
//			}
//
//			// add a pen_gamemin for every game under the slot's min
//			if (assignedGames < gameslotMin) {
//				gameSlotScore = (gameslotMin - assignedGames) * prob.getPenGameMin();
//				gameSlotTotal += gameSlotScore;
//			}	
//		}

//		// loop through all practice slots and compare number of assigned practices to the slot's min
//		for (int i = 0; i < nPracticeslots; i++){
//			int assignedPractices = 0; 								// keeps track of assigned practices to current slot 
//			int practiceSlotMin = prob.practiceSlots.get(i).getMin(); 	// keeps track of a slot's min practices (i.e. practicemin(s))
//
//			for (int j = 0; j < scheduleList.size(); j++){
//				if (scheduleList.get(j).second.equals(prob.practiceSlots.get(i))){
//					assignedPractices += 1;
//				}
//			}
//
//			// add a pen_practicemin for every practice under the slot's min
//			if (assignedPractices < practiceSlotMin) {
//				practiceSlotScore = (practiceSlotMin - assignedPractices) * prob.getPenPracticeMin();
//				practiceSlotTotal += practiceSlotScore;
//			}
//		}

		// total penalty points: Eval_minfill(assign) * w_minfilled
		evalMinfilled = (gameSlotTotal + practiceSlotTotal) * prob.getwMinFilled();


		// Part 2: Eval_pref(assign) function
		// note: pen_preferred = total points - fulfilled points
		int totalPoints = 0; 		// keeps track of total preference points from input file?
		int assignedPoints = 0; 	// keeps track of assigned preference points (i.e. game/practice assigned to preferred day+time)

		// loop through preferences to get total used ranking points? are we keeping list of preferences?
		// if hashmap not empty, add preference
		for (int i = 0; i < scheduleList.size(); i++){
			for (Integer points : scheduleList.get(i).first.preferences.values()){
				totalPoints += points;
			}
		}

		// loop through schedule to get assigned games/practices and get assigned points
		for (int i = 0; i < scheduleList.size(); i++){
			if (!scheduleList.get(i).first.preferences.isEmpty()){
				if(scheduleList.get(i).first.preferences.containsKey(scheduleList.get(i).second)){
					assignedPoints += scheduleList.get(i).first.preferences.get(scheduleList.get(i).second);
				}
			}
		}		
	
		// total penalty points: Eval_pref(assign) * w_pref
		evalPref = (totalPoints - assignedPoints) * prob.getwPref();

		// Part 3: Eval_pair(assign) function
		// loop through schedule to get pairs
		for (Pair assigned : scheduleList) {
			if (!assigned.first.pair.isEmpty()) {
				for (Assignable assignInSlot : transformScheduleTimeSlot.get(assigned.second)) {
					if (!assigned.first.pair.contains(assignInSlot) || 
							!isOverlap(assigned.second, transformScheduleAssignable.get(assignInSlot))) {
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
				for (Assignable assignInSlot : transformScheduleTimeSlot.get(assigned.second)) {
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
	
	private Map<Assignable, TimeSlot> transformScheduleAssignable(ArrayList<Pair> schedule) {
		HashMap<Assignable, TimeSlot> transformedSchedule = new HashMap<Assignable, TimeSlot>();
		for (Pair assigned : schedule) {
			transformedSchedule.put(assigned.first, assigned.second);
		}
		return transformedSchedule;
	}
	
	private Map<TimeSlot, List<Assignable>> transformScheduleTimeSlot(ArrayList<Pair> schedule) {
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
			output.append(": ");
			output.append(assigned.second);
			output.append("\n");
		}
		
		return output.toString();
	}
}
