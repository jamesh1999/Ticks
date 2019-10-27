package uk.ac.cam.jhah2.oop.tick5;

public abstract class World implements Cloneable
{
	private int m_generation = 0;
	private Pattern m_pattern;

	public World(Pattern patt)
	{
		m_pattern = patt;
	}

	public World(String s) throws PatternFormatException
	{
		m_pattern = new Pattern(s);
	}

	public World(World w)
	{
		m_generation = w.m_generation;
		m_pattern = w.m_pattern;
	}

	// Getters
	public int getWidth() { return m_pattern.getWidth(); }
	public int getHeight() { return m_pattern.getHeight(); }
	public int getGenerationCount() { return m_generation; }
	protected Pattern getPattern() { return m_pattern; }

	protected abstract boolean getCellImpl(int row, int col);
	public boolean getCell(int row, int col)
	{
		//Cells outside world are dead
		if(col < 0 || col >= getWidth() || row < 0 || row >= getHeight()) return false;

		return getCellImpl(row, col);
	}

	protected abstract void setCellImpl(int row, int col, boolean val);
	public void setCell(int row, int col, boolean val)
	{
		//Do nothing to cells outside world
		if(col < 0 || col >= getWidth() || row < 0 || row >= getHeight()) return;

		setCellImpl(row, col, val);
	}

	protected int countNeighbours(int row, int col)
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

	// Using # alive neighbours, computes whether the cell is alive in the next generation
	// By standard GoL rules
	protected boolean computeCell(int row, int col)
	{
		boolean alive = getCell(row, col);
		int neighbours = countNeighbours(row, col);

		return neighbours == 3 || (alive && neighbours == 2);
	}

	protected abstract void nextGenerationImpl();
	public void nextGeneration()
	{
		++m_generation;
		nextGenerationImpl();
	}

	@Override
	public World clone()
	{
		try
		{
			World c = (World)super.clone();
			c.m_generation = m_generation;
			c.m_pattern = m_pattern;

			return c;
		}
		catch (CloneNotSupportedException e)
		{
			System.out.println("World cannot be cloned.");
			return null;
		}
	}
}
