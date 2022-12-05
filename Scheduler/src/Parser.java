import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
	private String args[];
	private Problem prob;
	private int totalAvailableSlotsFromInput = 0;
	private Pattern ageTierPattern;
	private Pattern gamePattern;
	private Pattern practicePattern;
	private Pattern assignablePattern;
	private Pattern notCompatiblePattern;
	private Pattern unwantedPattern;
	private Pattern preferencesPattern;
	private Pattern pairPattern;
	private Pattern partialAssignPattern;

	public Parser(String[] args) throws FileNotFoundException {
		this.args = args;
	}
	
	public Problem parse() {
		System.out.println("Start parsing...");
		// parse command line weight and penalties
		// population size needs to be greater than 2, if no entry or input <= 2, use default value
		int maxGenerations = 3;
		int populationSize = 3;
		int fitnessThreshold = 30;
		
		ArrayList<Integer> parsed = new ArrayList<>();
		
		
		
		
		for (int i = 1; i < args.length; i++) {
			if (Integer.parseInt(args[i]) >= 0) {
				parsed.add(Integer.parseInt(args[i]));
			} else {
				throw new IllegalArgumentException(String.format("Invalid command line input: %s", args[i])); 
			}
		}
		
		int wMinFilled = parsed.get(0);
		int wPref = parsed.get(1);
		int wPair = parsed.get(2);
		int wSecDiff = parsed.get(3);
		int penGameMin = parsed.get(4);
		int penPracticeMin = parsed.get(5);
		int penNotPaired = parsed.get(6);
		int penSection = parsed.get(7);
		
		if (parsed.size() > 8) {
			if (parsed.get(8) > 1) maxGenerations = parsed.get(8);
			if (parsed.get(9) > 2) populationSize = parsed.get(9);
			if (parsed.get(10) <= 100) fitnessThreshold = parsed.get(10);
		} 
		
		// create problem object
		prob = new Problem(wMinFilled, wPref, wPair, wSecDiff, penGameMin, penPracticeMin, penNotPaired, penSection, 
				maxGenerations, populationSize, fitnessThreshold);
		
//		System.out.println(wMinFilled);
//		System.out.println(wPref);
//		System.out.println(wPair);
//		System.out.println(wSecDiff);
//		System.out.println(penGameMin);
//		System.out.println(penPracticeMin);
//		System.out.println(penNotPaired);
//		System.out.println(penSection);
//		System.out.println(maxGenerations);
//		System.out.println(populationSize);
//		System.out.println(fitnessThreshold);
		
		// initialize time slots
		initializeTimeSlots();
		
		// parse text file
//		transformInputText(args[0]);
		ageTierPattern = Pattern.compile("^[\\s]*([A-Za-z]{1})+([0-9]+)+([A-Za-z]{1})+([0-9]+)+(?:(S)){0,1}[\\s]*");
		gamePattern = Pattern.compile("^[\\s]*([A-Za-z]{4})[\\s][\\s]*+([0-9A-Za-z\\s]+)[\\s][\\s]*+DIV[\\s]+([0-9]+)");
		practicePattern = Pattern.compile("^[\\s]*([A-Za-z]{4})[\\s]++([0-9A-Za-z]+)[\\s]++(?:DIV[\\s]++([0-9]+)[\\s]+){0,1}+(?:(PRC|OPN))[\\s]++([0-9]+)[\\s]*");
		notCompatiblePattern = Pattern.compile("^[\\s]*([0-9A-Za-z\\s]*)[\\s]*,[\\s]*([0-9A-Za-z\\s]*)[\\s]*");
		unwantedPattern = Pattern.compile("^[\\s]*([0-9A-Za-z\\s]*)[\\s]*,[\\s]*([A-Z]{2})[\\s]*,[\\s]*([0-9]{1,2}:[0-9]{2})[\\s]*");
		preferencesPattern = Pattern.compile("^[\\s]*([A-Z]{2})[\\s]*,[\\s]*([0-9]{1,2}:[0-9]{2})[\\s]*,[\\s]*([0-9A-Za-z\\s]*)[\\s]*,[\\s]*([0-9]*)[\\s]*");
		pairPattern = Pattern.compile("^[\\s]*([0-9A-Za-z\\s]*)[\\s]*,[\\s]*([0-9A-Za-z\\s]*)[\\s]*");
		partialAssignPattern = Pattern.compile("^[\\s]*([0-9A-Za-z\\s]*)[\\s]*,[\\s]*([A-Z]{2})[\\s]*,[\\s]*([0-9]{1,2}:[0-9]{2})[\\s]*");
		parseBuffer(args[0]);
		
		prob.setMaxSlots(totalAvailableSlotsFromInput);
		
//		for (TimeSlot slot : prob.gameSlots) {
//			System.out.println(slot);
//		}
//		
//		for (TimeSlot slot : prob.practiceSlots) {
//			System.out.println(slot);
//		}
		
		System.out.println("Parsing finished...");
		return prob;
	}
	
	public int getTotalAvailableSlotsFromInput() {
		return totalAvailableSlotsFromInput;
	}

	private void timeSlotsMWF(Day day, int start, int inc, String type) {
		int endMWF = 2000;
		
		if (type.equals("game")) {
			for (int i = start; i <= endMWF; i += inc) {
				prob.gameSlots.add(new GameSlot(day, Integer.toString(i)));
			}
		} else if (type.equals("practice")) {
			if (day == Day.FR) {
				for (int i = start; i < endMWF; i += inc) {
					prob.practiceSlots.add(new PracticeSlot(day, Integer.toString(i)));
				}
			} else {
				for (int i = start; i <= endMWF; i += inc) {
					prob.practiceSlots.add(new PracticeSlot(day, Integer.toString(i)));
				}
			}
		}
	}
	
	private void initializeTimeSlots() {
		int start = 800;
		int endTT = 1830;
		int incTT = 300;
				
		timeSlotsMWF(Day.MO, start, 100, "game");
		timeSlotsMWF(Day.MO, start, 100, "practice");
		timeSlotsMWF(Day.TU, start, 100, "practice");
		timeSlotsMWF(Day.FR, start, 200, "practice");
		
		// Tuesday games
		for (int i = start; i <= endTT; i += incTT) {
			prob.gameSlots.add(new GameSlot(Day.TU, Integer.toString(i)));
			prob.gameSlots.add(new GameSlot(Day.TU, Integer.toString(i + 130)));
		}
	}
	
//	private void transformInputText(String filename) {
//		try (BufferedReader reader = new BufferedReader(new FileReader(filename)); 
//				BufferedWriter writer = new BufferedWriter(new FileWriter("input.txt"))) {
//			for (String line = reader.readLine(); line != null; line = reader.readLine()) { 
//		        if (!line.trim().isEmpty()) {
//		        	writer.write(line);
//		        }
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	public void parseBuffer(String filename) {
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (line != null){
					line = line.stripTrailing().toLowerCase();
				}
				switch (line) {	
					case "name:": parseName(reader); break;
					case "game slots:": parseGameSlots(reader); break;
					case "practice slots:": parsePracticeSlots(reader); break;
					case "games:": parseGames(reader); break;
					case "practices:": parsePractices(reader); break;
					case "not compatible:": parseNotCompatible(reader); break;
					case "unwanted:": parseUnwanted(reader); break;
					case "preferences:": parsePreferences(reader); break;
					case "pair:": parsePair(reader); break;
					case "partial assignments:": parsePartialAssignments(reader); break;
					default: 
						if (line.length() > 0)
							throw new IllegalArgumentException(String.format("Cannot parse line: %s", line));
						else break;
				}
			}
//			for (TimeSlot slot : prob.gameSlots) {
//				System.out.println(slot);
//			}
//			for (TimeSlot slot : prob.practiceSlots) {
//				System.out.println(slot);
//			}
//			for (Game game : prob.games) {
//				System.out.println(game);
//			}
//			for (Practice practice : prob.practices) {
//				System.out.println(practice);
//			}
//			for (Game game : prob.games) {
//				System.out.print(game + ":");
//				for (Assignable a : game.notcompatible) {
//					System.out.print(a + ", ");
//				}	
//				System.out.println("break");
//			}
//			for (Practice practice : prob.practices) {
//				System.out.print(practice + ":");
//				for (Assignable p : practice.notcompatible) {
//					System.out.print(p + ", ");
//				}
//				System.out.println("break");
//			}
//			for (Game game : prob.games) {
//				System.out.print(game + ":");
//				for (TimeSlot a : game.unwanted) {
//					System.out.print(a + ", ");
//				}	
//				System.out.println("break");
//			}
//			for (Practice practice : prob.practices) {
//				System.out.print(practice + ":");
//				for (TimeSlot p : practice.unwanted) {
//					System.out.print(p + ", ");
//				}
//				System.out.println("break");
//			}
//			for (Game game : prob.games) {
//				System.out.print(game + ":");
//				for (TimeSlot a : game.preferences.keySet()) {
//					System.out.print(a + ": " + game.preferences.get(a) + ", ");
//				}	
//				System.out.println("break");
//			}
//			for (Practice practice : prob.practices) {
//				System.out.print(practice + ":");
//				for (TimeSlot p : practice.preferences.keySet()) {
//					System.out.print(p + ": " + practice.preferences.get(p) + ", ");
//				}
//				System.out.println("break");
//			}
//			for (Game game : prob.games) {
//				System.out.print(game + ":");
//				for (Assignable a : game.pair) {
//					System.out.print(a + ", ");
//				}	
//				System.out.println("break");
//			}
//			for (Practice practice : prob.practices) {
//				System.out.print(practice + ":");
//				for (Assignable p : practice.pair) {
//					System.out.print(p + ", ");
//				}
//				System.out.println("break");
//			}
//			for (Game game : prob.games) {
//				System.out.print(game + ":");
//				if (game.getPartAssign() != null) System.out.print(game.getPartAssign() + ", ");
//				System.out.println("break");
//			}
//			for (Practice practice : prob.practices) {
//				System.out.print(practice + ":");
//				if (practice.getPartAssign() != null) System.out.print(practice.getPartAssign() + ", ");
//				System.out.println("break");
//			}
			reader.close();
		} catch (FileNotFoundException e){
			e.printStackTrace();
	    } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private GameSlot parseGameSlot(String identifier) {
		GameSlot gameSlot = null;
		identifier = identifier.replaceAll("\\s", "");
		String[] parts = identifier.split(",");
		if (parts.length > 0) {
			String newTime = parts[1].replace(":", "");
			switch (parts[0]) {
				case "MO":
					gameSlot = new GameSlot(Day.MO, newTime);
					break;
				case "TU":
					gameSlot = new GameSlot(Day.TU, newTime);
					break;
				default: 
					gameSlot = null;
					break;
			}
			gameSlot = getGameSlot(gameSlot);
			if (gameSlot != null && parts.length == 4) {
				gameSlot.setMax(Integer.parseInt(parts[2]));
				gameSlot.setMin(Integer.parseInt(parts[3]));
				totalAvailableSlotsFromInput += Integer.parseInt(parts[2]);
			}
		}
		return gameSlot;
	}
	
	private PracticeSlot parsePracticeSlot(String identifier) {
		PracticeSlot practiceSlot = null;
		identifier = identifier.replaceAll("\\s", "");
		String[] parts = identifier.split(",");
		if (parts.length > 0) {
			String newTime = parts[1].replace(":", "");
			switch (parts[0]) {
				case "MO":
					practiceSlot = new PracticeSlot(Day.MO, newTime);
					break;
				case "TU":
					practiceSlot = new PracticeSlot(Day.TU, newTime);
					break;
				case "FR":
					practiceSlot = new PracticeSlot(Day.FR, newTime);
					break;
				default: 
					practiceSlot = null;
					break;
			}
			practiceSlot = getPracticeSlot(practiceSlot);
			if (practiceSlot != null && parts.length == 4) {
				practiceSlot.setMax(Integer.parseInt(parts[2]));
				practiceSlot.setMin(Integer.parseInt(parts[3]));
				totalAvailableSlotsFromInput += Integer.parseInt(parts[2]);
			}
		}
		return practiceSlot;
	}
	
	private Game parseGame(String identifier) {
		Matcher m = gamePattern.matcher(identifier);
		if (m.find()) {
			String id = m.group(1) + " " + m.group(2);
			int div = Integer.parseInt(m.group(3));
			if (m.group(1).equals("CSMA")) {
				boolean isSpecial = false;
				Matcher mat = ageTierPattern.matcher(m.group(2));
				if (mat.find()) { 
					int age = Integer.parseInt(mat.group(2));
					int tier = Integer.parseInt(mat.group(4));
					if (mat.group(5) != null) isSpecial = true;
					return new Game(id, age, tier, div, isSpecial);	
				}
			} else {
				return new Game(id, div);	
			}
		}
		return null;
	}
	
	private Practice parsePractice(String identifier) {
		Matcher m = practicePattern.matcher(identifier);
		if (m.find()) {
			// 1: id, 2: age/tier, 3: div, 4: PRC/OPN, 5: pracid
			boolean isPrac = true;
			String id = m.group(1) + " " + m.group(2);
			int div = -1;
			if (m.group(3) != null) div = Integer.parseInt(m.group(3));
			if (m.group(4).equals("OPN")) isPrac = false;
			int prac = Integer.parseInt(m.group(5));
			if (m.group(1).equals("CSMA")) {
				Matcher mat = ageTierPattern.matcher(m.group(2));
				if (mat.find()) { 
					int age = Integer.parseInt(mat.group(2));
					int tier = Integer.parseInt(mat.group(4));
					return new Practice(id, age, tier, div, prac, isPrac);		
				}
			} else {
				return new Practice(id, div, prac, isPrac);
			}
		}
		return null;
	}
	
	private GameSlot getGameSlot(GameSlot toFind) {
		
		int index = prob.gameSlots.indexOf(toFind);
		if (index != -1) return prob.gameSlots.get(index);
		return null;
	}
	
	private PracticeSlot getPracticeSlot(PracticeSlot toFind) {
		int index = prob.practiceSlots.indexOf(toFind);
		if (index != -1) return prob.practiceSlots.get(index);
		return null;
	}
	
	private Game getGame(Game toFind) {
		int index = prob.games.indexOf(toFind);
		if (index != -1) return prob.games.get(index);
		return null;
	}
	
	private Practice getPractice(Practice toFind) {
		int index = prob.practices.indexOf(toFind);
		if (index != -1) return prob.practices.get(index);
		return null;
	}
	

	private void parseName(BufferedReader reader) throws IOException {
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			if (line.isEmpty()) return;
			prob.problemName = line;
		}
	}
	
	
	private void parseGameSlots(BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.isEmpty() || line.trim().equals("") || line.trim().equals("\n")) {
				return;
			} else {
				parseGameSlot(line);
			}
		}
	}
	
	private void parsePracticeSlots(BufferedReader reader) throws NumberFormatException, IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.isEmpty() || line.trim().equals("") || line.trim().equals("\n")) {
				return;
			} else {
				parsePracticeSlot(line);
			}
		}
	}
	

	
	private void parseGames(BufferedReader reader) throws NumberFormatException, IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() > 0) {
				Game game = parseGame(line);
				if (game != null) prob.games.add(game);
			} else return;
		}
	}
	
	private void parsePractices(BufferedReader reader) throws NumberFormatException, IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() > 0) {
				Practice practice = parsePractice(line);
				if (practice != null) prob.practices.add(practice);
			} else return;
		}
	}
	

	
	private boolean isGame(String identifier) {
		if (identifier.contains("PRC") || identifier.contains("OPN")) return false;
		return true;
	}
	private void parseNotCompatible(BufferedReader reader) throws NumberFormatException, IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() > 0) {
				Matcher m = notCompatiblePattern.matcher(line);
				if (m.find()) {
					String first = m.group(1).trim();
					String second = m.group(2).trim();
					
					if (isGame(first) && isGame(second)) {
						Game a1 = getGame(parseGame(first));
						Game a2 = getGame(parseGame(second));
						if (a1 != null && a2 != null) {
							a1.notcompatible.add(a2);
							a2.notcompatible.add(a1);
						} else System.out.println(String.format("Ignore invalid not compatible: %s", line));	
					}
					else if (isGame(first) && !isGame(second)) {
						Game a1 = getGame(parseGame(first));
						Practice a2 = getPractice(parsePractice(second));
						if (a1 != null && a2 != null) {
							a1.notcompatible.add(a2);
							a2.notcompatible.add(a1);
						} else System.out.println(String.format("Ignore invalid not compatible: %s", line));	
					}
					else if (!isGame(first) && isGame(second)) {
						Practice a1 = getPractice(parsePractice(first));
						Game a2 = getGame(parseGame(second));
						if (a1 != null && a2 != null) {
							a1.notcompatible.add(a2);
							a2.notcompatible.add(a1);
						} else System.out.println(String.format("Ignore invalid not compatible: %s", line));	
					}
					else if (!isGame(first) && !isGame(second)) {
						Practice a1 = getPractice(parsePractice(first));
						Practice a2 = getPractice(parsePractice(second));
						if (a1 != null && a2 != null) {
							a1.notcompatible.add(a2);
							a2.notcompatible.add(a1);
						} else System.out.println(String.format("Ignore invalid not compatible: %s", line));	
					}
				}
			} else return;
		}
	}
	
	private void parseUnwanted(BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() > 0) {
				Matcher m = unwantedPattern.matcher(line);
				if (m.find()) {
					Assignable assignable;
					TimeSlot unwantedSlot;
					String slot = m.group(2) + "," + m.group(3);
					if (isGame(m.group(1))) {
						assignable = getGame(parseGame(m.group(1)));
						unwantedSlot = parseGameSlot(slot);
						if (assignable != null && unwantedSlot != null) {
							assignable.unwanted.add(unwantedSlot);
						} else System.out.println(String.format("Ignore invalid unwanted: %s", line));			
					} else {
						assignable = getPractice(parsePractice(m.group(1)));
						unwantedSlot = parsePracticeSlot(slot);
						if (assignable != null && unwantedSlot != null) {
							assignable.unwanted.add(unwantedSlot);
						} else System.out.println(String.format("Ignore invalid unwanted: %s", line));		
					}
				}				
			} else return;
		}
	}
	
	private void parsePreferences(BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() > 0) {
				Matcher m = preferencesPattern.matcher(line);
				if (m.find()) {
					Assignable assignable;
					TimeSlot unwantedSlot;
					String identifier = m.group(3);
					String slot = m.group(1) + "," + m.group(2);
					if (isGame(identifier)) {
						assignable = getGame(parseGame(identifier));
						unwantedSlot = parseGameSlot(slot);
						if (assignable != null && unwantedSlot != null) {
							assignable.preferences.put(unwantedSlot, Integer.parseInt(m.group(4)));
						} else System.out.println(String.format("Ignore invalid preference: %s", line));			
					} else {
						assignable = getPractice(parsePractice(identifier));
						unwantedSlot = parsePracticeSlot(slot);
						if (assignable != null && unwantedSlot != null) {
							assignable.preferences.put(unwantedSlot, Integer.parseInt(m.group(4)));
						} else System.out.println(String.format("Ignore invalid preference: %s", line));	
					}
				}
			} else return;
		}
	}
	
	private void parsePair(BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() > 0) {
				Matcher m = pairPattern.matcher(line);
				if (m.find()) {
					String first = m.group(1).trim();
					String second = m.group(2).trim();
					
					if (isGame(first) && isGame(second)) {
						Game a1 = getGame(parseGame(first));
						Game a2 = getGame(parseGame(second));
						if (a1 != null && a2 != null) {
							a1.pair.add(a2);
						} else System.out.println(String.format("Ignore invalid pair: %s", line));	
					}
					else if (isGame(first) && !isGame(second)) {
						Game a1 = getGame(parseGame(first));
						Practice a2 = getPractice(parsePractice(second));
						if (a1 != null && a2 != null) {
							a1.pair.add(a2);
						} else System.out.println(String.format("Ignore invalid pair: %s", line));
					}
					else if (!isGame(first) && isGame(second)) {
						Practice a1 = getPractice(parsePractice(first));
						Game a2 = getGame(parseGame(second));
						if (a1 != null && a2 != null) {
							a1.pair.add(a2);
						} else System.out.println(String.format("Ignore invalid pair: %s", line));
					}
					else if (!isGame(first) && !isGame(second)) {
						Practice a1 = getPractice(parsePractice(first));
						Practice a2 = getPractice(parsePractice(second));
						if (a1 != null && a2 != null) {
							a1.pair.add(a2);
						} else System.out.println(String.format("Ignore invalid pair: %s", line));
					}
				}
			} else return;
		}
	}
	
	private void parsePartialAssignments(BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() > 0) {
				Matcher m = partialAssignPattern.matcher(line);
				if (m.find()) {
					Assignable assignable;
					TimeSlot toAssignSlot;
					String slot = m.group(2) + "," + m.group(3);
					if (isGame(m.group(1))) {
						assignable = getGame(parseGame(m.group(1)));
						toAssignSlot = parseGameSlot(slot);
						if (toAssignSlot == null) System.out.println(String.format("Ignore invalid partial assignment: %s", line));
						else assignable.setPartAssign(toAssignSlot);		
					} else {
						assignable = getPractice(parsePractice(m.group(1)));
						toAssignSlot = parsePracticeSlot(slot);
						if (toAssignSlot == null) System.out.println(String.format("Ignore invalid partial assignment: %s", line));
						else assignable.setPartAssign(toAssignSlot);
					}
				}				
			} else return;
		}
	}
}
