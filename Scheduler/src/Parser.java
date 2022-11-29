import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {


	String args[];
/* 
	public ArrayList<GameSlot> GameSlots = new ArrayList<GameSlot>();
	public ArrayList<PracticeSlot> PracticeSlots = new ArrayList<PracticeSlot>();
	public ArrayList<Game> Games = new ArrayList<Game>();
	public ArrayList<Practice> Practices = new ArrayList<Practice>();

	public ArrayList<NonCompatible> NonCompatibles = new ArrayList<NonCompatible>();
	public ArrayList<Unwanted> Unwanteds = new ArrayList<Unwanted>();
	public ArrayList<Preference> Preferences = new ArrayList<Preference>();
	public ArrayList<Pair> Pairs = new ArrayList<Pair>();
	public ArrayList<PartialAssignment> PartialAssignments = new ArrayList<PartialAssignment>();
 */

	public Parser(String[] args) throws FileNotFoundException {
			
		this.args = args;
	}

	public void miniParse(String filename, Problem prob) throws FileNotFoundException
	{
		File file = new File(filename);
		Scanner miniParser = new Scanner(file);
		while (miniParser.hasNextLine())
		{
			String line = miniParser.nextLine();
			String currentLine;
			switch(line)
			{
				
				case "Game slots:":
					currentLine = miniParser.nextLine();
					//System.out.println(currentLine);
					while (!currentLine.equals("")) {
						currentLine = currentLine.replaceAll("\\s", "");
						String[] parts = currentLine.split(",");
						String newTime=parts[1].replace(":", "");
						
						GameSlot gameSlot;
						int index;
						switch (parts[0]){
							case "MO":
								gameSlot = new GameSlot(Day.MO, newTime);
								index = prob.gameSlots.indexOf(gameSlot);
								gameSlot = prob.gameSlots.get(index);
								
								gameSlot.setGameMax(Integer.parseInt(parts[2]));
								gameSlot.setGameMin(Integer.parseInt(parts[3]));
							case "TU":
								gameSlot = new GameSlot(Day.TU, newTime);
								index = prob.gameSlots.indexOf(gameSlot);
								gameSlot = prob.gameSlots.get(index);
								gameSlot.setGameMax(Integer.parseInt(parts[2]));
								gameSlot.setGameMin(Integer.parseInt(parts[3]));

						}
							
						for(GameSlot gSlot: prob.gameSlots)
						{
							System.out.println(gSlot);
						}
						
						//GameSlot gameSlot = new GameSlot(parts[0], parts[1]);//, Integer.valueOf(parts[2]), Integer.valueOf(parts[3]));
						//GameSlots.add(gameSlot);
						 
						currentLine = miniParser.nextLine();
					}

				case "Practice slots:":
					currentLine = miniParser.nextLine();
					//System.out.println(currentLine);
					while (!currentLine.equals("")) {
						currentLine = currentLine.replaceAll("\\s", "");
						String[] parts = currentLine.split(",");
						for (String part : parts) {
							System.out.println(part);
						}
						/* 
						GameSlot gameSlot = new GameSlot(parts[0], parts[1]);//, Integer.valueOf(parts[2]), Integer.valueOf(parts[3]));
						GameSlots.add(gameSlot);
						*/						
						
						currentLine = miniParser.nextLine();
					}
			}

		}
	}




	public static void main(String[] args) throws Exception
	{
		Parser parser = new Parser(args);

		parser.parse("/Users/alextanasescu/Desktop/Coding/CPSC-433-Set-Based-AI/Scheduler/src/input.txt");
		/* for(int i = 0; i < parser.GameSlots.size(); i ++) {
			System.out.println(parser.GameSlots.get(i).toString());
		} */		
	}


	public Problem parse(String filename) {
		// parse command line weight and penalties
		// population size needs to be greater than 2, if no entry or input <= 2, use default value
		
		
		// create problem object
		Problem prob = new Problem(0,0,0,0,0,0,0,0);
		
		
		// initialize time slots
		initializeTimeSlots(prob);
		
		// parse text file
		try {
			miniParse(filename, prob);
		} catch (FileNotFoundException e) {

		}
		
		return prob;
	}
	
	private void timeSlotsMWF(Day day, int start, int inc, String type, Problem prob) {
		int endMWF = 2000;
		
		if (type.equals("game")) {
			for (int i = start; i < endMWF; i += inc) {
				prob.gameSlots.add(new GameSlot(day, Integer.toString(i)));
			}
		} else if (type.equals("practice")) {
			for (int i = start; i < endMWF; i += inc) {
				prob.practiceSlots.add(new PracticeSlot(day, Integer.toString(i)));
			}
		}
	}
	
	private void initializeTimeSlots(Problem prob) {
		int start = 800;
		int endTT = 1830;
		int incTT = 300;
				
		timeSlotsMWF(Day.MO, start, 100, "game", prob);
		timeSlotsMWF(Day.MO, start, 100, "practice", prob);
		timeSlotsMWF(Day.TU, start, 100, "practice", prob);
		timeSlotsMWF(Day.FR, start, 200, "practice", prob);
		
		// Tuesday games
		for (int i = start; i < endTT; i += incTT) {
			prob.gameSlots.add(new GameSlot(Day.TU, Integer.toString(i)));
			prob.gameSlots.add(new GameSlot(Day.TU, Integer.toString(i + 130)));
		}
	}

}
