package uk.ac.cam.jhah2.oop.tick2;

public abstract class World
{
	private int m_generation = 0;
	private Pattern m_pattern;

	public World(String s)
	{
		m_pattern = new Pattern(s);
	}

	// Getters
	public int getWidth() { return m_pattern.getWidth(); }
	public int getHeight() { return m_pattern.getHeight(); }
	public int getGenerationCount() { return m_generation; }
	protected Pattern getPattern() { return m_pattern; }

	// Refactoring: Input sanitisation could be performed here rather than in child classes
	public abstract boolean getCell(int row, int col);
	public abstract void setCell(int row, int col, boolean val);

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

	// Refactoring: nextGeneration() could be implemented in Game using two different Worlds as buffers
	protected abstract void nextGenerationImpl();
	public void nextGeneration()
	{
		++m_generation;
		nextGenerationImpl();
	}
}