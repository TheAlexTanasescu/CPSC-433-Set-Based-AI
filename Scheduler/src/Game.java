public class Game extends Assignable {

	// for CSMA
	public Game(String id, int age, int tier, int div) {
		super(id, age, tier, div);		
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
		if (!game.getLeagueId().equals(this.getLeagueId()))  return false;
		if (game.getDiv() != this.getDiv()) return false;
		if (game.isSpecial() != this.isSpecial()) return false;

		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder id = new StringBuilder();
		id.append(getLeagueId());
		if (getDiv() < 10) id.append(" DIV 0");
		else id.append(" DIV ");
		id.append(getDiv());
		
		return id.toString();
	}
	
	@Override
	public int getStringLength() {
		return super.getLeagueId().length() + 7;		
	}
}