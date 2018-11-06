package uk.ac.cam.jhah2.prejava.ex3;

public class ArrayLife
{
	public static boolean getCell(boolean[][] world, int col, int row)
	{
		//Cells outside world are dead
		if(col < 0 || col >= world[0].length || row < 0 || row >= world.length) return false;

		return world[row][col];
	}

	public static void setCell(boolean[][] world, int col, int row, boolean value)
	{
		//Do nothing to cells outside world
		if(col < 0 || col >= world[0].length || row < 0 || row >= world.length) return;

		world[row][col] = value;
	}

	public static int countNeighbours(boolean[][] world, int col, int row)
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

	public static boolean computeCell(boolean[][] world, int col, int row)
	{
		boolean alive = getCell(world, col, row);
		boolean stillAlive = false;

		int neighbours = countNeighbours(world, col, row);

		if(neighbours == 3) stillAlive = true;
		if(alive && neighbours == 2) stillAlive = true;



		return stillAlive;
	}

	public static boolean[][] nextGeneration(boolean[][] world)
	{
		boolean[][] newWorld = new boolean[world.length][world[0].length];
		for(int x = 0; x < world[0].length; ++x)
			for(int y = 0; y < world.length; ++y)
			{
				boolean alive = computeCell(world, x, y);
				setCell(newWorld, x, y, alive);
			}

		return newWorld;
	}

	public static void print(boolean[][] world) { 
	   System.out.println("-"); 
	   for (int row = 0; row < world.length; row++) { 
	      for (int col = 0; col < world[0].length; col++) {
	         System.out.print(world[row][col] ? "#" : "_"); 
	      }
	      System.out.println(); 
	   } 
	}

	public static void play(boolean[][] world) throws java.io.IOException {
	   int userResponse = 0;
	   while (userResponse != 'q') {
	      print(world);
	      userResponse = System.in.read();
	      world = nextGeneration(world);
	   }
	}

	public static boolean getFromPackedLong(long packed, int position) {
        return ((packed >>> position) & 1) == 1;
	}

	public static void main(String[] args) throws java.io.IOException {
	   int size = Integer.parseInt(args[0]);
	   long initial = Long.decode(args[1]);
	   boolean[][] world = new boolean[size][size];
	   //place the long representation of the game board in the centre of "world"
	   for(int i = 0; i < 8; i++) {
	      for(int j = 0; j < 8; j++) {
	         world[i+size/2-4][j+size/2-4] = getFromPackedLong(initial,i*8+j);
	      }
	   }
	   play(world);
	}
}