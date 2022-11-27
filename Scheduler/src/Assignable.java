import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Assignable {
	// Assignable details
	private String leagueId;
	private int ageGroup = -1; // -1 if no age id - use to check overlapping
	private int tier = -1; // -1 if no tier id - use for pen_section
	private int div = -1; // -1 if no div
	private boolean isEvening = false;
	private boolean isSpecial = false;
	
	// Hard constraints
	public final Set<Assignable> notcompatible = new HashSet<Assignable>();
	public final Set<TimeSlot> unwanted = new HashSet<TimeSlot>();
	private TimeSlot partAssign = null;
	
	// Soft constraints
	public final Map<TimeSlot, Integer> preferences = new HashMap<TimeSlot, Integer>();
	public final Set<Assignable> pair = new HashSet<Assignable>();
	
	// For CSMA games - special games are assigned to practice slots
	public Assignable(String id, int age, int tier, int div, boolean isSpecial) {
		this.leagueId = id;
		this.ageGroup = age;
		this.tier = tier;
		this.div = div;
		this.isSpecial = isSpecial;
		if (div == 9) isEvening = true;
	}
	
	// For CSMA practices 
	public Assignable(String id, int age, int tier, int div) {
		this.leagueId = id;
		this.ageGroup = age;
		this.tier = tier;
		this.div = div;
		if (div == 9) isEvening = true;
	}
	
	// For all other organizing bodies games and practices
	public Assignable(String id, int div) {
		this.leagueId = id;
		this.div = div;
		if (div == 9) isEvening = true;
	}

	public TimeSlot getPartAssign() {
		return partAssign;
	}

	public void setPartAssign(TimeSlot partAssign) {
		this.partAssign = partAssign;
	}

	public String getLeagueId() {
		return leagueId;
	}

	public int getAgeGroup() {
		return ageGroup;
	}

	public int getTier() {
		return tier;
	}

	public int getDiv() {
		return div;
	}

	public boolean isEvening() {
		return isEvening;
	}

	public boolean isSpecial() {
		return isSpecial;
	}
	
//	public Assignable(Assignable[] notcompatiables, TimeSlot[] unwanted, TimeSlot partAssign, 
//			Map<TimeSlot, Double> preferences, Assignable[] pair) {
//		
//		for (Assignable assignable : notcompatiables) {
//			super.notcompatible.add(assignable);
//		}
//		
//		for (TimeSlot timeslot : unwanted) {
//			super.unwanted.add(timeslot);
//		}
//		
//		super.preferences.putAll(preferences);
//		
//		for (Assignable assignable : pair) {
//			super.pair.add(assignable);
//		}
//	}
}
