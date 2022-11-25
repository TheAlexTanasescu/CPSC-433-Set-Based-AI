import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Individual {
	private Problem prob;
	
	private Map<TimeSlot, List<Assignable>> schedule;
	private double fitness;
	
	private int n_games;
	private int n_practices;
	private int n_gameslots;
	private int n_practiceslots;
	private Random rand = new Random();
	private Integer[] allGames;
	private Integer[] allPractices;
	private Integer[] allGTS;
	private Integer[] allPTS;
	
	int sg = 0;
	int sp = 0;

	public Individual() {
		n_games = prob.games.size();
		allGames = new Integer[n_games];
		n_practices = prob.practices.size();
		allPractices = new Integer[n_practices];
		n_gameslots = prob.gameSlots.size();
		allGTS = new Integer[n_gameslots];
		n_practiceslots = prob.practiceSlots.size();
		allPTS = new Integer[n_practiceslots];
		random();
	}
	
	public Individual(Individual schedule) {
		validate(schedule.getSchedule());
	}
	
	public Map<TimeSlot, List<Assignable>> getSchedule() {
		return schedule;
	}
	
	private void generateRandomSeq(Integer[] listToShuffle) {
		for (int i = 0; i < listToShuffle.length; i++) {
			listToShuffle[i] = i;
		}
		Collections.shuffle(Arrays.asList(listToShuffle));
	}
	
	public void random() {
		// generate random
		generateRandomSeq(allGames);
		generateRandomSeq(allPractices);
		
		Assignable toAssign;
		int g = 0;
		int p = 0;
		
		for (int i = 0; i < n_games + n_practices; i++) {
			int next = rand.nextInt(2);
			if (next == 0) {
				toAssign = prob.games.get(allGames[g]);
				ArrayList<TimeSlot> slots = gameSlotChoice(toAssign);
				for (TimeSlot slot : slots) {
					assignment(toAssign, slot, schedule);
				}
				g++;
			} else {
				toAssign = prob.practices.get(allPractices[p]);
				TimeSlot slot = pracSlotChoice(toAssign);
				assignment(toAssign, slot, schedule);
				p++;
			}
		}
		
		// validate
		validate(schedule);
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
	
	public boolean validate(Map<TimeSlot, List<Assignable>> scheduleToValidate) {
		Map<TimeSlot, List<Assignable>> tempSchedule = new HashMap<TimeSlot, List<Assignable>>();
		HashMap<Assignable, TimeSlot> givenSchedule = new HashMap<Assignable, TimeSlot>();
		HashMap<Assignable, TimeSlot> searchState = new HashMap<Assignable, TimeSlot>();
				
		boolean constr = true;
		for (TimeSlot slot : scheduleToValidate.keySet()) {
			for (Assignable assigned : scheduleToValidate.get(slot)) {
				givenSchedule.put(assigned, slot);
				searchState.put(assigned, null);
			}
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
		
		if (constr) scheduleToValidate = tempSchedule;
		else scheduleToValidate = null;
		
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
		
		TimeSlot slot1 = new GameSlot(Day.TU, "1700");
		TimeSlot slot2 = new GameSlot(Day.TU, "1830");
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
	
//	private boolean dayCheck(Assignable assignable) {
//		if (assignable instanceof Game) {
//			if (assignedDay == Day.FR) {
//				return false;
//			}
//		}
//	}
	
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
	
	private void assignment(Assignable toAssign, TimeSlot slot, Map<TimeSlot, List<Assignable>> schedule) {
		if (schedule.containsKey(slot)) {
			if (schedule.get(slot).size() < slot.getMax()) {
				schedule.get(slot).add(toAssign);
			}
		} else {
			schedule.put(slot, new ArrayList<Assignable>());
			if (schedule.get(slot).size() < slot.getMax()) {
				schedule.get(slot).add(toAssign);
			}
		}
	}
	
	public double evaluate(Individual schedule) {
		
		return 0.0;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
