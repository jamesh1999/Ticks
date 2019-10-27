package uk.ac.cam.jhah2.oop.tick5;

import java.lang.StringBuilder;

public class Pattern implements Comparable<Pattern>
{
	private String name;
	private String author;
	private int width;
	private int height;
	private int startCol;
	private int startRow;
	private String cells;

	private int parseInt(String field, String val) throws PatternFormatException
	{
		try
			{ return Integer.parseInt(val); }
		catch (NumberFormatException e)
		{
			throw new PatternFormatException("Invalid pattern format: Could not interpret the " + field + " field as a number ('" + val + "' given).");
		}
	}

	public Pattern(String format) throws PatternFormatException
	{
		// Empty input
		if (format.equals("")) throw new PatternFormatException("Please specify a pattern.");

		String parts[] = format.split(":");

		// Incorrect field number
		if (parts.length != 7) throw new PatternFormatException("Invalid pattern format: Incorrect number of fields in pattern (found " + parts.length + ").");

		name = parts[0];
		author = parts[1];
		width = parseInt("width", parts[2]);
		height = parseInt("height", parts[3]);
		startCol = parseInt("startX", parts[4]);
		startRow = parseInt("startY", parts[5]);
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
	public void initialise(World world) throws PatternFormatException
	{
		String[] givenRows = cells.split(" ");
		for(int y = 0; y < givenRows.length; ++y)
			for(int x = 0; x < givenRows[y].length(); ++x)
			{
				if (givenRows[y].charAt(x) != '1' 
					&& givenRows[y].charAt(x) != '0')
					throw new PatternFormatException("Invalid pattern format: Malformed pattern '" + cells + "'.");
				
				world.setCell(startRow + y, startCol + x, givenRows[y].charAt(x) == '1');
			}
	}

	@Override
	public int compareTo(Pattern other)
	{
		return name.compareTo(other.name);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(" (");
		sb.append(author);
		sb.append(')');

		return sb.toString();
	}
}
