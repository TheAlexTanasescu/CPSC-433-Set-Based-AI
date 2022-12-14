
public class Practice extends Assignable {
	private int pracId; // -1 if special (no div/prac numbers)
	private boolean isPrac; // false if is OPN

	public Practice(String id, int age, int tier, int div, int prac, boolean isPrac, boolean isSpecial) {
		super(id, age, tier, div, isSpecial);
		this.pracId = prac;
		this.isPrac = isPrac;
	}
	
	public Practice(String id, int div, int prac, boolean isPrac) {
		super(id, div);
		this.pracId = prac;
		this.isPrac = isPrac;
	}
	
	public int getPracId() {
		return pracId;
	}

	public boolean isPrac() {
		return isPrac;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (!(other instanceof Practice)) return false;
		Practice practice = (Practice) other;
		if (!practice.getLeagueId().equals(this.getLeagueId())) return false;
		if (practice.getDiv() != this.getDiv()) return false;
		if (practice.getPracId() != this.getPracId()) return false;
		if (practice.isPrac() != this.isPrac()) return false;

		return true;
	}

	@Override
	public String toString() {
		StringBuilder id = new StringBuilder();
		id.append(getLeagueId());
		if (getDiv() != -1) {
			if (getDiv() < 10) id.append(" DIV 0");
			else id.append(" DIV ");
			id.append(getDiv());
		}
		
		if (getPracId() != -1) {
			if (isPrac) {
				if (pracId < 10) id.append(" PRC 0");
				else id.append(" PRC ");
			} else {
				if (pracId < 10) id.append(" OPN 0");
				else id.append(" OPN ");
			}
			id.append(pracId);
		}

		return id.toString();
	}
	
	@Override
	public int getStringLength() {
		if (super.getDiv() != -1) return super.getLeagueId().length() + 14;
		if (super.isSpecial()) return super.getLeagueId().length();
		else return super.getLeagueId().length() + 7;		
	}
}
