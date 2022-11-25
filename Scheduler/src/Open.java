
public class Open extends Assignable {
	private int openId;
	
	public Open(String id, int age, int tier, int div, int open) {
		super(id, age, tier, div);
		this.openId = open;
	}
	
	public int getOpenId() {
		return openId;
	}

	@Override
	public String toString() {
		StringBuilder id = new StringBuilder();
		id.append(getLeagueId());
		if (getDiv() != -1) {
			if (getDiv() < 10) id.append(" Div 0");
			else id.append(" Div ");
			id.append(getDiv());
		}
		
		if (openId < 10) id.append(" OPN 0");
		else id.append(" OPN ");
		id.append(openId);
		
		return id.toString();
	}
}
