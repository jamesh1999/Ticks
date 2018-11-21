package uk.ac.cam.jhah2.oop.tick1;

public class Pattern
{
	private String name;
	private String author;
	private int width;
	private int height;
	private int startCol;
	private int startRow;
	private String cells;

	public Pattern(String format)
	{
		String parts[] = format.split(":");

		name = parts[0];
		author = parts[1];
		width = Integer.parseInt(parts[2]);
		height = Integer.parseInt(parts[3]);
		startCol = Integer.parseInt(parts[4]);
		startRow = Integer.parseInt(parts[5]);
		cells = parts[6];
	}

	// Getters for member variables
	public String getName() { return name; }
	public String getAuthor() { return author; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public int getStartCol() { return startCol; }
	public int getStartRow() { return startRow; }
	public String getCells() { return cells; }

	// Fills a world with the cells described in the pattern
	public void initialise(ArrayLife world)
	{
		String[] givenRows = cells.split(" ");
		for(int y = 0; y < givenRows.length; ++y)
			for(int x = 0; x < givenRows[y].length(); ++x)
				world.setCell(startRow + y, startCol + x, givenRows[y].charAt(x) == '1');
	}
}
