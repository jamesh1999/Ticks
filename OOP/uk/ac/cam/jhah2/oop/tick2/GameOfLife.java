package uk.ac.cam.jhah2.oop.tick2;

import java.lang.StringBuilder;
import java.util.Scanner;

public class GameOfLife
{
	private World world;

	public GameOfLife(World w)
	{
		world = w;
	}

	public void play() throws java.io.IOException
	{
		Scanner sc = new Scanner(System.in);
		String userResponse = "";
		while (!userResponse.equals("q"))
		{
			print();
			userResponse = sc.nextLine();
			world.nextGeneration();
		}
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

	public static void main(String[] args) throws java.io.IOException, Exception
	{
		World w = null;
		String format = args[args.length - 1];

		for (String arg : args)
		{
			switch (arg)
			{
			case "--array":
				w = new ArrayWorld(format);
				break;
			case "--packed":
				w = new PackedWorld(format);
				break;
			default:
				break;
			}
		}

		GameOfLife game = new GameOfLife(w);
		game.play();
	}
}