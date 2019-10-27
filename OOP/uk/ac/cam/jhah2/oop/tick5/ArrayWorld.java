package uk.ac.cam.jhah2.oop.tick5;

public class ArrayWorld extends World implements Cloneable
{
	private boolean world[][];
	private boolean deadRow[];

	private void initialise() throws PatternFormatException
	{
		world = new boolean[getHeight()][getWidth()];
		deadRow = new boolean[getWidth()];

		// Populate world cells
		getPattern().initialise(this);
		setDeadRows();
	}

	private void setDeadRows()
	{
		// Finds rows that are entirely dead and sets them to reference deadRow[]
		for (int i = 0; i < world.length; ++i)
		{
			boolean dead = true;
			for (int j = 0; j < world[i].length; ++j)
				if (world[i][j]) dead = false;
			if (dead) world[i] = deadRow;
		}
	}

	protected boolean getCellImpl(int row, int col)
	{
		return world[row][col];
	}

	protected void setCellImpl(int row, int col, boolean val)
	{
		// Remove dead row if necessary
		if (val && world[row] == deadRow) world[row] = new boolean[getWidth()];

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

	public ArrayWorld(Pattern p) throws PatternFormatException
	{
		super(p);
		initialise();
	}

	public ArrayWorld(String s) throws PatternFormatException
	{
		super(s);
		initialise();
	}

	public ArrayWorld(ArrayWorld w)
	{
		super(w);
		world = new boolean[getHeight()][getWidth()];
		deadRow = w.deadRow;

		// Deep copy world & maintain dead rows
		for (int i = 0; i < world.length; ++i)
		{
			if (w.world[i] == deadRow) world[i] = deadRow;
			for (int j = 0; j < world[i].length; ++j)
				world[i][j] = w.world[i][j];
		}
	}

	@Override
	public ArrayWorld clone()
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
