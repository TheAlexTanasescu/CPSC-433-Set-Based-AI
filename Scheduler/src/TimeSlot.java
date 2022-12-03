public abstract class TimeSlot {
	private Day day;
	private String startTime;
	private int max = 0;
	private int min = 0;
	private boolean isEvening = false;
	
	public TimeSlot(Day day, String time) {
		this.day = day;
		this.startTime = time;
		if (Integer.parseInt(startTime) >= 1800) {
			isEvening = true;
		}
	}

	public Day getDay() {
		return day;
	}

	public String getStartTime() {
		return startTime;
	}

	public int getMax() {
		return max;
	}
	
	public void setMax(int max) {
		this.max = max;
	}
	
	public int getMin() {
		return min;
	}
	
	public void setMin(int min) {
		this.min = min;
	}
	
	public boolean isEvening() {
		return isEvening;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (!(other instanceof TimeSlot)) return false;
		TimeSlot timeslot = (TimeSlot) other;
		if (!timeslot.getDay().equals(this.day))  return false;
		if (!timeslot.getStartTime().equals(this.startTime)) return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder timeslot = new StringBuilder();
		timeslot.append(day);
		timeslot.append(", ");
		if (startTime.length() == 3) timeslot.append(startTime.charAt(0));
		else timeslot.append(startTime.substring(0, 2));
		timeslot.append(":");
		timeslot.append(startTime.substring(startTime.length() - 2));
		timeslot.append(", ");
		timeslot.append(max);
		timeslot.append(", ");
		timeslot.append(min);
		
		return timeslot.toString();
	}
}