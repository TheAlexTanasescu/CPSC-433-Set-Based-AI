import java.util.ArrayList;

public class Problem {
	// Input entries
	public ArrayList<Game> games = new ArrayList<Game>();
	public ArrayList<Practice> practices = new ArrayList<Practice>();
	public ArrayList<GameSlot> gameSlots = new ArrayList<GameSlot>();
	public ArrayList<PracticeSlot> practiceSlots = new ArrayList<PracticeSlot>();
	
	// Environment
	private int maxGenerations = 5;
	private int populationSize = 10;
	private double fitnessThreshold = 0.3;
	
	private int wMinFilled;
	private int wPref;
	private int wPair;
	private int wSecDiff;
	
	private int penGameMin;
	private int penPracticeMin;
	private int penNotPaired;
	private int penSection;
	
	public Problem(int wMinFilled, int wPref, int wPair, int wSecDiff, 
			int penGameMin, int penPracticeMin, int penNotPaired, int penSection) {
		this.wMinFilled = wMinFilled;
		this.wPref = wPref;
		this.wPair = wPair;
		this.wSecDiff = wSecDiff;
		
		this.penGameMin = penGameMin;
		this.penPracticeMin = penPracticeMin;
		this.penNotPaired = penNotPaired;
		this.penSection = penSection;
	}

	public int getMaxGenerations() {
		return maxGenerations;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public double getFitnessThreshold() {
		return fitnessThreshold;
	}

	public int getwMinFilled() {
		return wMinFilled;
	}

	public int getwPref() {
		return wPref;
	}

	public int getwPair() {
		return wPair;
	}

	public int getwSecDiff() {
		return wSecDiff;
	}

	public int getPenGameMin() {
		return penGameMin;
	}

	public int getPenPracticeMin() {
		return penPracticeMin;
	}

	public int getPenNotPaired() {
		return penNotPaired;
	}

	public int getPenSection() {
		return penSection;
	}
}