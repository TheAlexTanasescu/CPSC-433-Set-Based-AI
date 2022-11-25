public class Game extends Assignable {

	public Game(String id, int age, int tier, int div, boolean isSpecial) {
		super(id, age, tier, div, isSpecial);		
	}
	
	@Override
	public String toString() {
		StringBuilder id = new StringBuilder();
		id.append(getLeagueId());
		if (getDiv() < 10) id.append(" Div 0");
		else id.append(" Div ");
		id.append(getDiv());
		
		return id.toString();
	}
}
