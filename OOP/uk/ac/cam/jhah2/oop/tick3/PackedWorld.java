package uk.ac.cam.jhah2.oop.tick3;

public class PackedWorld extends World
{
	private long world;

	public PackedWorld(String s) throws Exception
	{
		super(s);

		int bits = getWidth() * getHeight();
		if (bits > 64) throw new Exception("The given world is too large to be packed");

		// Populate world cells
		getPattern().initialise(this);
	}

	public boolean getCell(int row, int col)
	{
		//Cells outside world are dead
		if(col < 0 || col >= getWidth() || row < 0 || row >= getHeight()) return false;

		int packedIdx = row * getWidth() + col;
		return (world & (1L << packedIdx)) != 0L;
	}

	public void setCell(int row, int col, boolean val)
	{
		//Do nothing to cells outside world
		if(col < 0 || col >= getWidth() || row < 0 || row >= getHeight()) return;

		int packedIdx = row * getWidth() + col;
		if (val)
			world |= (1L << packedIdx);
		else
			world &= ~(1L << packedIdx);
	}

	protected void nextGenerationImpl()
	{
		long newWorld = 0;

		for(int x = 0; x < getWidth(); ++x)
			for(int y = 0; y < getHeight(); ++y)
			{
				if(!computeCell(y, x)) continue;

				// Set corresponding bit
				int packedIdx = y * getWidth() + x;
				newWorld |= 1L << packedIdx;
			}

		world = newWorld;
	}
}
