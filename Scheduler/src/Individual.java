import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
	
	private int sg = 0;
	private int sp = 0;

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
		
		Assignable toAssign;
		int ag = 0;
		int ap = 0;
		int i = 0;
		
		while (i < nGames + nPractices) {
			int next = rand.nextInt(2);
			if (next == 0 && ag < nGames) {
				toAssign = prob.games.get(allGames[ag]);
				TimeSlot slot = gameSlotChoice(toAssign);
				assignment(toAssign, slot, randomSchedule);
				ag++;
				i++;
			} else if (next == 1 && ap < nPractices) {
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
		TimeSlot slot;
		int randSlot;
		
		if (toAssign.isSpecial()) {
			return gameSpecial(toAssign);
		}
		
		if (toAssign.getPartAssign() != null) {
			return toAssign.getPartAssign();
		}
		
		if (!toAssign.unwanted.isEmpty()) {
			randSlot = rand.nextInt(nGameslots);
			slot = prob.gameSlots.get(randSlot);
			while (toAssign.unwanted.contains(slot)|| slot.getMax() == 0) {
				System.out.println("Max " + slot.getMax());
				//System.out.println("Min " + slot.getMin());
				System.out.println("N size " + toAssign.unwanted.size());
				randSlot = rand.nextInt(nGameslots);
				slot = prob.gameSlots.get(randSlot);
			}
			return slot;
		}
		
		if (!toAssign.preferences.isEmpty()) {
			List<TimeSlot> keysAsArray = new ArrayList<TimeSlot>(toAssign.preferences.keySet());
			slot = keysAsArray.get(rand.nextInt(keysAsArray.size()));
			while (slot.getMax() == 0) {
				slot = keysAsArray.get(rand.nextInt(keysAsArray.size()));
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
		
		if (!toAssign.preferences.isEmpty()) {
			List<TimeSlot> keysAsArray = new ArrayList<TimeSlot>(toAssign.preferences.keySet());
			slot = keysAsArray.get(rand.nextInt(keysAsArray.size()));
			while (slot.getMax() == 0) {
				slot = keysAsArray.get(rand.nextInt(keysAsArray.size()));
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
		Map<TimeSlot, List<Assignable>> tempSchedule = transformScheduleTimeSlot(null);
		HashMap<Assignable, TimeSlot> givenSchedule = new HashMap<Assignable, TimeSlot>();
		HashMap<Assignable, TimeSlot> searchState = new HashMap<Assignable, TimeSlot>();
				
		for (Pair assigned : scheduleToValidate) {
			givenSchedule.put(assigned.first, assigned.second);
			searchState.put(assigned.first, null);
		}
		
		// if schedule is not full, randomly choose unassigns until full
		while (givenSchedule.size() < prob.getIndividualMax()) {
//			System.out.println("schedule not full");
			Assignable toAssign;
			int counter = 1;
			if (rand.nextInt(2) == 0) {
				toAssign = prob.games.get(rand.nextInt(nGames));
				while (counter < nGames && givenSchedule.containsKey(toAssign)) {
					toAssign = prob.games.get(rand.nextInt(nGames));
					counter++;
				}
			} else {
				toAssign = prob.practices.get(rand.nextInt(nPractices));
				while (counter < nPractices && givenSchedule.containsKey(toAssign)) {
					toAssign = prob.practices.get(rand.nextInt(nPractices));
					counter++;
				}
			}
			givenSchedule.put(toAssign, null);
			searchState.put(toAssign, null);
		}
		
		int constr = 0;
		
		// generate random
		generateRandomSeq(allGTS);
		generateRandomSeq(allPTS);

		for (Assignable assignable : searchState.keySet()) {
			sg = 0;
			sp = 0;
			// select a slot to proceed (predefined or random)
			TimeSlot toAssign = givenSchedule.get(assignable);
			if (toAssign == null) toAssign = getNextSlot(assignable);
			
			// loop until a valid slot is found or no valid slot can be found for current assignable
			while (sg <= nGameslots || sp <= nPracticeslots) {
				// can't find a valid slot for this assignable, go the the next assignable
				if (sg >= nGameslots || sp >= nPracticeslots) break;
				
				// all passed, break and try the next assignable
				if (passedHardConstr(assignable, toAssign, tempSchedule, searchState)) {
					searchState.put(assignable, toAssign);
					assignment(assignable, toAssign, tempSchedule);
					assignment(assignable, toAssign, tempScheduleInPair);
					constr++;
					break;
				}
				
				// failed to pass all hard constraints, try the next slot
				toAssign = getNextSlot(assignable);
			}
		}
		
		if (constr > 0) {
			scheduleInPair = tempScheduleInPair;
			evaluate(this);
			return true;
		}
		else scheduleInPair = null;
		
		return false;
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
		ArrayList<TimeSlot> overlappingSlots = new ArrayList<TimeSlot>();
		if (toAssign instanceof GameSlot) {
			for (TimeSlot overlapped : getOverlappingSlots(toAssign)) {
				overlappingSlots.add(getPracticeSlot(overlapped));
			}
		} else {
			for (TimeSlot overlapped : getOverlappingSlots(toAssign)) {
				overlappingSlots.add(getGameSlot(overlapped));
			}
		}
		overlappingSlots.add(toAssign);
		
		return 
		specialCheck(assignable) &&
		eveningCheck(assignable, toAssign) &&
		meetingCheck(assignable, assignDay, assignTime) &&
		partAssignCheck(assignable, assignDay, assignTime) &&
		unwantedCheck(assignable, toAssign) &&
		maxCheck(assignable, toAssign, tempSchedule) &&
		specialOverlapCheck(12, 1, assignable, toAssign, tempSchedule) &&
		specialOverlapCheck(13, 1, assignable, toAssign, tempSchedule) &&
		divCheck(assignable, tempSchedule, overlappingSlots) &&
		agetierCheck(assignable, toAssign, tempSchedule) &&
		notcompatibleCheck(assignable, toAssign, tempSchedule, overlappingSlots);
	}
	
	private GameSlot getGameSlot(TimeSlot toFind) {
		int index = prob.gameSlots.indexOf(toFind);
		if (index != -1) return prob.gameSlots.get(index);
		return null;
	}
	
	private PracticeSlot getPracticeSlot(TimeSlot toFind) {
		int index = prob.practiceSlots.indexOf(toFind);
		if (index != -1) return prob.practiceSlots.get(index);
		return null;
	}
	
	private TimeSlot gameSpecial(Assignable toAssign) {
		TimeSlot slot = getPracticeSlot(new PracticeSlot(Day.TU, "1800"));
		if (slot.getMax() > 0) return slot;
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
			if (assignDay == Day.TU && assignTime.equals("1100")) {
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
		if (toAssign.getMax() == 0 || tempSchedule.get(toAssign).size() >= toAssign.getMax()) {
			return false;
		}
		return true;
	}
	
	private boolean specialOverlapCheck(int age, int tier, Assignable assignable, TimeSlot toAssign, 
			Map<TimeSlot, List<Assignable>> tempSchedule) {
		// if U12T1 / U13T1, check overlap with special
		// game -> game slot -> might overlap
		if (assignable instanceof Game && assignable.getAgeGroup() == age && assignable.getTier() == tier) {
			TimeSlot specialSlot = getPracticeSlot(new PracticeSlot(Day.TU, "1800"));
			// continue only if prac slot TU 18 has that special age and tier assigned game
			boolean hasSpecial = false;
			for (Assignable assigned : tempSchedule.get(specialSlot)) {
				if (assigned.isSpecial() && assigned.getAgeGroup() == age && assigned.getTier() == tier) hasSpecial = true;
			}
	
			// game slots overlapping prac slot TU 18
			if (hasSpecial) {
				ArrayList<TimeSlot> overlappedGameSlots = getOverlappingSlots(specialSlot);
				for (TimeSlot overlapped : overlappedGameSlots) {
					for (Assignable assigned : tempSchedule.get(overlapped)) {
						if (assigned.getAgeGroup() == age && assigned.getTier() == tier) return false;
					}
				}
			}
		} else if (assignable instanceof Practice && assignable.getAgeGroup() == age && assignable.getTier() == tier) {
			// prac -> prac slot -> below should be sufficient
			for (Assignable assigned : tempSchedule.get(toAssign)) {
				if (assigned.isSpecial() && assigned.getAgeGroup() == age && assignable.getTier() == tier) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean divCheck(Assignable assignable, Map<TimeSlot, List<Assignable>> tempSchedule, ArrayList<TimeSlot> overlappingSlots) {
		// game and practice for the same div are not overlap		
		Set<String> existed = new HashSet<String>();
		String current = assignable.getLeagueId() + assignable.getDiv();
		existed.add(current);
		for (TimeSlot overlapped : overlappingSlots) {
			for (Assignable assigned : tempSchedule.get(overlapped)) {
				current = assigned.getLeagueId() + assigned.getDiv();
				if (existed.contains(current)) return false;
				else existed.add(current);
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
			Map<TimeSlot, List<Assignable>> tempSchedule, ArrayList<TimeSlot> overlappingSlots) {
		// notcompatible
		if (!assignable.notcompatible.isEmpty()) {
			for (TimeSlot overlapped : overlappingSlots) {
				for (Assignable assigned : tempSchedule.get(overlapped)) {
					if (assignable.notcompatible.contains(assigned)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private ArrayList<TimeSlot> getOverlappingSlots(TimeSlot targetSlot) {
		ArrayList<TimeSlot> overlappedSlots = new ArrayList<TimeSlot>();
		if (targetSlot instanceof GameSlot) {
			if (targetSlot.getDay() == Day.MO) {
				overlappedSlots.add(new PracticeSlot(Day.MO, targetSlot.getStartTime()));
				switch(targetSlot.getStartTime()) {
					case "800":
						overlappedSlots.add(new PracticeSlot(Day.FR, "800"));
						break;
					case "900":
						overlappedSlots.add(new PracticeSlot(Day.FR, "800"));
						break;
					case "1000":
						overlappedSlots.add(new PracticeSlot(Day.FR, "1000"));
						break;
					case "1100":
						overlappedSlots.add(new PracticeSlot(Day.FR, "1000"));
						break;
					case "1200":
						overlappedSlots.add(new PracticeSlot(Day.FR, "1200"));
						break;
					case "1300":
						overlappedSlots.add(new PracticeSlot(Day.FR, "1200"));
						break;
					case "1400":
						overlappedSlots.add(new PracticeSlot(Day.FR, "1400"));
						break;
					case "1500":
						overlappedSlots.add(new PracticeSlot(Day.FR, "1400"));
						break;
					case "1600":
						overlappedSlots.add(new PracticeSlot(Day.FR, "1600"));
						break;
					case "1700":
						overlappedSlots.add(new PracticeSlot(Day.FR, "1600"));
						break;
					case "1800":
						overlappedSlots.add(new PracticeSlot(Day.FR, "1800"));
						break;
					case "1900":
						overlappedSlots.add(new PracticeSlot(Day.FR, "1800"));
						break;
					default: break;
				}
			} else if (targetSlot.getDay() == Day.TU) {
				switch(targetSlot.getStartTime()) {
					case "800":
						overlappedSlots.add(new PracticeSlot(Day.TU, "800"));
						overlappedSlots.add(new PracticeSlot(Day.TU, "900"));
						break;
					case "930":
						overlappedSlots.add(new PracticeSlot(Day.TU, "900"));
						overlappedSlots.add(new PracticeSlot(Day.TU, "1000"));
						break;
					case "1100":
						overlappedSlots.add(new PracticeSlot(Day.TU, "1100"));
						overlappedSlots.add(new PracticeSlot(Day.TU, "1200"));
						break;
					case "1230":
						overlappedSlots.add(new PracticeSlot(Day.TU, "1200"));
						overlappedSlots.add(new PracticeSlot(Day.TU, "1300"));
						break;
					case "1400":
						overlappedSlots.add(new PracticeSlot(Day.TU, "1400"));
						overlappedSlots.add(new PracticeSlot(Day.TU, "1500"));
						break;
					case "1530":
						overlappedSlots.add(new PracticeSlot(Day.TU, "1500"));
						overlappedSlots.add(new PracticeSlot(Day.TU, "1600"));
						break;
					case "1700":
						overlappedSlots.add(new PracticeSlot(Day.TU, "1700"));
						overlappedSlots.add(new PracticeSlot(Day.TU, "1800"));
						break;
					case "1830":
						overlappedSlots.add(new PracticeSlot(Day.TU, "1800"));
						overlappedSlots.add(new PracticeSlot(Day.TU, "1900"));
						break;
					default: break;
				}
			}
		} else if (targetSlot instanceof PracticeSlot) {
			if (targetSlot.getDay() == Day.MO) {
				overlappedSlots.add(new GameSlot(Day.MO, targetSlot.getStartTime()));
			} else if (targetSlot.getDay() == Day.FR) {
				switch(targetSlot.getStartTime()) {
					case "800":
						overlappedSlots.add(new GameSlot(Day.MO, "800"));
						overlappedSlots.add(new GameSlot(Day.MO, "900"));
						break;
					case "1000":
						overlappedSlots.add(new GameSlot(Day.MO, "1000"));
						overlappedSlots.add(new GameSlot(Day.MO, "1100"));
						break;
					case "1200":
						overlappedSlots.add(new GameSlot(Day.MO, "1200"));
						overlappedSlots.add(new GameSlot(Day.MO, "1300"));
						break;
					case "1400":
						overlappedSlots.add(new GameSlot(Day.MO, "1400"));
						overlappedSlots.add(new GameSlot(Day.MO, "1500"));
						break;
					case "1600":
						overlappedSlots.add(new GameSlot(Day.MO, "1600"));
						overlappedSlots.add(new GameSlot(Day.MO, "1700"));
						break;
					case "1800":
						overlappedSlots.add(new GameSlot(Day.MO, "1800"));
						overlappedSlots.add(new GameSlot(Day.MO, "1900"));
						break;
					default: break;
				}
			} else if (targetSlot.getDay() == Day.TU) {
				switch(targetSlot.getStartTime()) {
					case "800":
						overlappedSlots.add(new GameSlot(Day.TU, "800"));
						break;
					case "900":
						overlappedSlots.add(new GameSlot(Day.TU, "800"));
						overlappedSlots.add(new GameSlot(Day.TU, "930"));
						break;
					case "1000":
						overlappedSlots.add(new GameSlot(Day.TU, "930"));
						break;
					case "1100":
						overlappedSlots.add(new GameSlot(Day.TU, "1100"));
						break;
					case "1200":
						overlappedSlots.add(new GameSlot(Day.TU, "1100"));
						overlappedSlots.add(new GameSlot(Day.TU, "1230"));
						break;
					case "1300":
						overlappedSlots.add(new GameSlot(Day.TU, "1230"));
						break;
					case "1400":
						overlappedSlots.add(new GameSlot(Day.TU, "1400"));
						break;
					case "1500":
						overlappedSlots.add(new GameSlot(Day.TU, "1400"));
						overlappedSlots.add(new GameSlot(Day.TU, "1530"));
						break;
					case "1600":
						overlappedSlots.add(new GameSlot(Day.TU, "1530"));
						break;
					case "1700":
						overlappedSlots.add(new GameSlot(Day.TU, "1700"));
						break;
					case "1800":
						overlappedSlots.add(new GameSlot(Day.TU, "1700"));
						overlappedSlots.add(new GameSlot(Day.TU, "1830"));
						break;
					case "1900":
						overlappedSlots.add(new GameSlot(Day.TU, "1830"));
						break;
					default: break;
				}
			}
		}
		return overlappedSlots;
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
		int evalMinfilled = 0;  // total penalties for not satisfying slot's minimum
		int evalPref = 0; 		// total penalties from not satisfying preferences
		int evalPair = 0; 		// total penalties from not satisfying pairs
		int evalSecdiff = 0; 	// total penalties from overlapping divisions
		
		Map<TimeSlot, List<Assignable>> transformScheduleTimeSlot = transformScheduleTimeSlot(scheduleList);
		Map<Assignable, TimeSlot> transformScheduleAssignable = transformScheduleAssignable(scheduleList);

		// Part 1: Eval_minfill(assign) function
		for (TimeSlot slot : prob.gameSlots) {
			if (transformScheduleTimeSlot.get(slot).size() < slot.getMin()) {
				evalMinfilled += (slot.getMin() - transformScheduleTimeSlot.get(slot).size()) * prob.getPenGameMin();;
			}
		}
		
		for (TimeSlot slot : prob.practiceSlots) {
			if (transformScheduleTimeSlot.get(slot).size() < slot.getMin()) {
				evalMinfilled += (slot.getMin() - transformScheduleTimeSlot.get(slot).size()) * prob.getPenPracticeMin();;
			}
		}
		
		// total penalty points: Eval_minfill(assign) * w_minfilled
		evalMinfilled *= prob.getwMinFilled();

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
				if (scheduleList.get(i).first.preferences.containsKey(scheduleList.get(i).second)){
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
				for (Assignable pairing : assigned.first.pair) {
					if (!isOverlap(transformScheduleAssignable.get(pairing), assigned.second)) evalPair += prob.getPenNotPaired();
				}
			}
		}

		// total penalty points: Eval_pair(assign) * w_pair
		evalPair *= prob.getwPair();

		// Part 4: Eval_secdiff(assign) function
		Set<Assignable> checked = new HashSet<Assignable>();
		for (Pair assigned : scheduleList) {
			if (assigned.first instanceof Game) {
				int ageGroup = assigned.first.getAgeGroup();
				int tier = assigned.first.getTier();
				for (Assignable assignInSlot : transformScheduleTimeSlot.get(assigned.second)) {
					if (!assignInSlot.equals(assigned.first) && assignInSlot.getAgeGroup() == ageGroup && assignInSlot.getTier() == tier) {
						if (checked.contains(assignInSlot)) evalSecdiff += prob.getPenSection();	
						else checked.add(assigned.first);
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
		
		for (TimeSlot slot : prob.gameSlots) {
			if (!transformedSchedule.containsKey(slot)) {
				transformedSchedule.put(slot, new ArrayList<Assignable>());
			}
		}
		
		for (TimeSlot slot : prob.practiceSlots) {
			if (!transformedSchedule.containsKey(slot)) {
				transformedSchedule.put(slot, new ArrayList<Assignable>());
			}
		}

		if (schedule != null) {
			for (Pair assigned : schedule) {
				transformedSchedule.get(assigned.second).add(assigned.first);		
			}
		}
		
		return transformedSchedule;
	}

	Comparator<Assignable> comparator = Comparator
	        .comparing(Assignable::getLeagueId)
	        .thenComparing(Assignable::getStringLength)
	        .thenComparing(Assignable::toString);
	
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		Map<Assignable, TimeSlot> transformedSchedule = transformScheduleAssignable(scheduleInPair);
		List<Assignable> keys = new ArrayList<>(transformedSchedule.keySet());
		Collections.sort(keys, comparator);

		for (Assignable assigned : keys) {
			output.append(assigned);
			for (int i = 0; i < 30 - assigned.getStringLength(); i++) {
				output.append(" ");
			}
			output.append(": ");
			output.append(transformedSchedule.get(assigned));
			output.append("\n");
		}
		
		return output.toString();
	}
}
