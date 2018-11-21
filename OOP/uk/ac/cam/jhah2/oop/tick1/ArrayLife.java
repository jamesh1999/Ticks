package uk.ac.cam.jhah2.oop.tick1;

import java.util.Scanner;

public class ArrayLife
{
	private boolean world[][];
	private Pattern pattern;

	public ArrayLife(String format)
	{
		pattern = new Pattern(format);
		world = new boolean[pattern.getHeight()][pattern.getWidth()];

		// Populate world cells
		pattern.initialise(this);
	}

	public boolean getCell(int row, int col)
	{
		//Cells outside world are dead
		if(col < 0 || col >= pattern.getWidth() || row < 0 || row >= pattern.getHeight()) return false;

		return world[row][col];
	}

	public void setCell(int row, int col, boolean value)
	{
		//Do nothing to cells outside world
		if(col < 0 || col >= pattern.getWidth() || row < 0 || row >= world.length) return;

		world[row][col] = value;
	}

	private int countNeighbours(int row, int col)
	{
		int cnt = 0;
		for(int x = -1; x <= 1; ++x)
			for(int y = -1; y <= 1; ++y)
			{
				if(y==0 && x==0) continue; // Skip this cell
				if(getCell(row + y, col + x)) ++cnt;
			}

		return cnt;
	}

	private boolean computeCell(int row, int col)
	{
		boolean alive = getCell(row, col);
		int neighbours = countNeighbours(row, col);

		return neighbours == 3 || (alive && neighbours == 2);
	}

	public void nextGeneration()
	{
		boolean newWorld[][] = new boolean[pattern.getHeight()][pattern.getWidth()];

		for(int x = 0; x < pattern.getWidth(); ++x)
			for(int y = 0; y < pattern.getHeight(); ++y)
				newWorld[y][x] = computeCell(y, x);

		world = newWorld;
	}

	public void print()
	{ 
		System.out.println("-"); 
		for (int row = 0; row < pattern.getHeight(); row++)
		{ 
			for (int col = 0; col < pattern.getWidth(); col++)
				System.out.print(world[row][col] ? "#" : "_"); 
			System.out.println(); 
		} 
	}

	public void play() throws java.io.IOException
	{
		Scanner sc = new Scanner(System.in);
		String userResponse = "";
		while (!userResponse.equals("q"))
		{
			print();
			userResponse = sc.nextLine();
			nextGeneration();
		}
	}

	public static void main(String[] args) throws java.io.IOException
	{
		ArrayLife al = new ArrayLife(args[0]);
		al.play();
	}
}
