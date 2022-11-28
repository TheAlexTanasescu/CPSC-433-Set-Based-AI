import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {


	public ArrayList<GameSlot> GameSlots = new ArrayList<GameSlot>();
	public ArrayList<PracticeSlot> PracticeSlots = new ArrayList<PracticeSlot>();
	public ArrayList<Game> Games = new ArrayList<Game>();
	public ArrayList<Practice> Practices = new ArrayList<Practice>();
	/*
	public ArrayList<NonCompatible> NonCompatibles = new ArrayList<NonCompatible>();
	public ArrayList<Unwanted> Unwanteds = new ArrayList<Unwanted>();
	public ArrayList<Preference> Preferences = new ArrayList<Preference>();
	public ArrayList<Pair> Pairs = new ArrayList<Pair>();
	public ArrayList<PartialAssignment> PartialAssignments = new ArrayList<PartialAssignment>();
 */

	public Parser() throws FileNotFoundException {
		



		File file = new File("/Users/alextanasescu/Desktop/Coding/CPSC-433-Set-Based-AI/Scheduler/src/input.txt");
		Scanner miniParser = new Scanner(file);
		while (miniParser.hasNextLine())
		{
			String line = miniParser.nextLine();
	
			switch(line)
			{
				
				case "Game slots:":
			
					String currentLine = miniParser.nextLine();
					System.out.println(currentLine);
					while (!currentLine.equals("")) {
						currentLine = currentLine.replaceAll("\\s", "");
						String[] parts = currentLine.split(",");
						GameSlot gameSlot = new GameSlot(parts[0], parts[1], Integer.valueOf(parts[2]), Integer.valueOf(parts[3]));
						GameSlots.add(gameSlot);
						currentLine = miniParser.nextLine();
					}
				break;

			}

		}
			
		
	}

	public static void main(String[] args) throws Exception
	{
		Parser parser = new Parser();

		for(int i = 0; i < parser.GameSlots.size(); i ++) {
			System.out.println(parser.GameSlots.get(i).toString());
		}		
	}

}
