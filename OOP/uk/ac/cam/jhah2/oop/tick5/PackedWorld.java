package uk.ac.cam.jhah2.oop.tick5;

public class PackedWorld extends World implements Cloneable
{
	private long world;

	private void initialise() throws Exception
	{
		int bits = getWidth() * getHeight();
		if (bits > 64) throw new Exception("The given world is too large to be packed");

		// Populate world cells
		getPattern().initialise(this);
	}

	protected boolean getCellImpl(int row, int col)
	{
		int packedIdx = row * getWidth() + col;
		return (world & (1L << packedIdx)) != 0L;
	}

	protected void setCellImpl(int row, int col, boolean val)
	{
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

	public PackedWorld(Pattern p) throws Exception
	{
		super(p);
		initialise();
	}

	public PackedWorld(String s) throws Exception
	{
		super(s);
		initialise();
	}

	public PackedWorld(PackedWorld w)
	{
		super(w);
		world = w.world;
	}

	@Override
	public PackedWorld clone()
	{
		PackedWorld c = (PackedWorld)super.clone();
		c.world = world;
		return c;
	}
}
