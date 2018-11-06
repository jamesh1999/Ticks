package uk.ac.cam.jhah2.prejava.ex2;

public class TinyLife
{
	public static boolean getCell(long world, int col, int row)
	{
		//Cells outside world are dead
		if(col < 0 || col > 7 || row < 0 || row > 7) return false;

		return PackedLong.get(world, row * 8 + col);
	}

	public static long setCell(long world, int col, int row, boolean value)
	{
		//Do nothing to cells outside world
		if(col < 0 || col > 7 || row < 0 || row > 7) return world;

		return PackedLong.set(world, row * 8 + col, value);
	}

	public static int countNeighbours(long world, int col, int row)
	{
		int[][] deltas = {{-1, -1}, {0, -1}, {1, -1},
							{-1, 0}, {1, 0},
							{-1, 1}, {0, 1}, {1, 1}};

		int cnt = 0;
		for(int i = 0; i < 8; ++i)
			if(getCell(world, col + deltas[i][0], row + deltas[i][1]))
				cnt++;

		return cnt;
	}

	public static boolean computeCell(long world, int col, int row)
	{
		boolean alive = getCell(world, col, row);
		boolean stillAlive = false;

		int neighbours = countNeighbours(world, col, row);

		if(neighbours == 3) stillAlive = true;
		if(alive && neighbours == 2) stillAlive = true;



		return stillAlive;
	}

	public static long nextGeneration(long world)
	{
		long newWorld = 0L;
		for(int x = 0; x < 8; ++x)
			for(int y = 0; y < 8; ++y)
			{
				boolean alive = computeCell(world, x, y);
				newWorld = setCell(newWorld, x, y, alive);
			}

		return newWorld;
	}

	public static void print(long world) { 
	   System.out.println("-"); 
	   for (int row = 0; row < 8; row++) { 
	      for (int col = 0; col < 8; col++) {
	         System.out.print(getCell(world, col, row) ? "#" : "_"); 
	      }
	      System.out.println(); 
	   } 
	}

	public static void play(long world) throws java.io.IOException {
	   int userResponse = 0;
	   while (userResponse != 'q') {
	      print(world);
	      userResponse = System.in.read();
	      world = nextGeneration(world);
	   }
	}

	public static void main(String[] args) throws java.io.IOException {
	   play(Long.decode(args[0]));
	}
}