package uk.ac.cam.jhah2.oop.tick3;

import java.lang.StringBuilder;
import java.util.*;
import java.io.*;

public class GameOfLife
{
	private PatternStore patternStore;
	private World world;

	public GameOfLife(PatternStore ps)
	{
		patternStore = ps;
	}

	public void print()
	{
		// Generation line
		StringBuilder sb = new StringBuilder("- ");
		sb.append(world.getGenerationCount());
		sb.append('\n');

		// Board
		for (int y = 0; y < world.getHeight(); ++y)
		{ 
			for (int x = 0; x < world.getWidth(); ++x)
				sb.append(world.getCell(y, x) ? "#" : "_"); 
			sb.append('\n');
		}

		System.out.println(sb.toString());
	}


	public void play() throws IOException, PatternFormatException {
        
  String response="";
  BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    
  System.out.println("Please select a pattern to play (l to list:");
  while (!response.equals("q")) {
    response = in.readLine();
    System.out.println(response);
    if (response.equals("f")) {
      if (world == null) {
        System.out.println("Please select a pattern to play (l to list):");
      }
      else {
        world.nextGeneration();
        print();
      }
    }
    else if (response.equals("l")) {
      List<Pattern> names = patternStore.getPatternsNameSorted();
      int i = 0;
      for (Pattern p : names) {
        System.out.println(i+" "+p.getName()+"  ("+p.getAuthor()+")");
        i++;
      }
    }
    else if (response.startsWith("p")) {
      List<Pattern> names = patternStore.getPatternsNameSorted();
      int patIdx = Integer.parseInt(response.substring(1, response.length()));
      Pattern p = names.get(patIdx);

      if (p.getWidth() * p.getHeight() > 64)
      	world = new ArrayWorld(p);
      else
      	try
      	{
      	world = new PackedWorld(p);
      }
      catch (PatternFormatException e)
      {
      	throw e;
      }
      catch (Exception e)
      { // We'll just pretend this isn't horribly bad practice
      }

      print();
    } 
  }
}

public static void main(String args[]) throws IOException, PatternFormatException {
  if (args.length!=1) {
    System.out.println("Usage: java GameOfLife <path/url to store>");
    return;
  }
  
  try {
    PatternStore ps = new PatternStore(args[0]);
    GameOfLife gol = new GameOfLife(ps);    
    gol.play();
  }
  catch (IOException ioe) {
    System.out.println("Failed to load pattern store");
  }
}
}

