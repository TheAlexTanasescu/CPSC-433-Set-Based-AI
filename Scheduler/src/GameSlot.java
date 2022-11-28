
public class GameSlot extends TimeSlot {

	
	public GameSlot(Day day, String time) {
		super(day, time);

    private int gameMax;
	private int gameMin;

	public GameSlot(String day, String time, int gameMax, int gameMin) {
		super.setDay(day);
		super.setStartTime(time);
		setGameMax(gameMax);
		setGameMin(gameMin);

	}

	public void setGameMax(int gameMax) {
		this.gameMax = gameMax;
	}

	public void setGameMin(int gameMin) {
		this.gameMin = gameMin;
	}

	public String toString() {
		return this.getDay() + " " + this.getStartTime() + " " + this.gameMax + " " + this.gameMin;
	}
}
