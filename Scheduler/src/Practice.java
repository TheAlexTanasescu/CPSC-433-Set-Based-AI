
public class Practice extends Assignable {
	private int pracId; 

	public Practice(String id, int age, int tier, int div, int prac) {
		super(id, age, tier, div);
		this.pracId = prac;
	}
	
	public int getPracId() {
		return pracId;
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
		
		if (pracId < 10) id.append(" PRC 0");
		else id.append(" PRC ");
		id.append(pracId);
		
		return id.toString();
	}
}