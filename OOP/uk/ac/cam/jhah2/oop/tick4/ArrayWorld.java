package uk.ac.cam.jhah2.oop.tick4;

public class ArrayWorld extends World implements Cloneable
{
	private boolean world[][];
	private boolean deadRow[];

	public ArrayWorld(Pattern p) throws PatternFormatException
	{
		super(p);
		world = new boolean[getHeight()][getWidth()];
		deadRow = new boolean[getWidth()];

		// Populate world cells
		getPattern().initialise(this);

		for (int i = 0; i < world.length; ++i)
		{
			boolean dead = true;
			for (int j = 0; j < world[i].length; ++j)
				if (world[i][j]) dead = false;
			if (dead) world[i] = deadRow;
		}
	}

	public ArrayWorld(String s) throws PatternFormatException
	{
		super(s);
		world = new boolean[getHeight()][getWidth()];
		deadRow = new boolean[getWidth()];

		// Populate world cells
		getPattern().initialise(this);

		for (int i = 0; i < world.length; ++i)
		{
			boolean dead = true;
			for (int j = 0; j < world[i].length; ++j)
				if (world[i][j]) dead = false;
			if (dead) world[i] = deadRow;
		}
	}

	public ArrayWorld(ArrayWorld w)
	{
		super(w);
		world = new boolean[getHeight()][getWidth()];
		deadRow = w.deadRow;

		for (int i = 0; i < world.length; ++i)
		{
			if (w.world[i] == deadRow) world[i] = deadRow;
			for (int j = 0; j < world[i].length; ++j)
				world[i][j] = w.world[i][j];
		}
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

		for(int y = 0; y < getHeight(); ++y)
			for(int x = 0; x < getWidth(); ++x)
				newWorld[y][x] = computeCell(y, x);

		world = newWorld;
	}

	@Override
	public ArrayWorld clone() throws CloneNotSupportedException
	{
		ArrayWorld c = (ArrayWorld)super.clone();
		c.world = new boolean[getHeight()][getWidth()];
		c.deadRow = deadRow;

		for (int i = 0; i < c.world.length; ++i)
		{
			if (world[i] == deadRow) c.world[i] = deadRow;
			for (int j = 0; j < c.world[i].length; ++j)
				c.world[i][j] = world[i][j];
		}

		return c;
	}
}
