public class Game extends Assignable {

	// for CSMA
	public Game(String id, int age, int tier, int div, boolean isSpecial) {
		super(id, age, tier, div, isSpecial);		
	}
	
	// all other organizing bodies
	public Game(String id, int div) {
		super(id, div);		
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (!(other instanceof Game)) return false;
		Game game = (Game) other;
		if (game.getLeagueId().equals(this.getLeagueId()))  return false;
		if (game.getDiv()== this.getDiv()) return false;
		if (game.isSpecial() == this.isSpecial()) return false;

		return true;
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
