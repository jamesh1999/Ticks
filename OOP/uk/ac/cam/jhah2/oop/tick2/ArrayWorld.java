package uk.ac.cam.jhah2.oop.tick2;

public class ArrayWorld extends World
{
	private boolean world[][];

	public ArrayWorld(String format)
	{
		super(format);
		world = new boolean[getHeight()][getWidth()];

		// Populate world cells
		getPattern().initialise(this);
	}

	public boolean getCell(int row, int col)
	{
		//Cells outside world are dead
		if(col < 0 || col >= getWidth() || row < 0 || row >= getHeight()) return false;

		return world[row][col];
	}

	public void setCell(int row, int col, boolean val)
	{
		//Do nothing to cells outside world
		if(col < 0 || col >= getWidth() || row < 0 || row >= getHeight()) return;

		world[row][col] = val;
	}

	protected void nextGenerationImpl()
	{
		boolean newWorld[][] = new boolean[getHeight()][getWidth()];

		for(int x = 0; x < getWidth(); ++x)
			for(int y = 0; y < getHeight(); ++y)
				newWorld[y][x] = computeCell(y, x);

		world = newWorld;
	}
}
