package uk.ac.cam.jhah2.oop.tick3;

import java.io.*;
import java.net.*;
import java.util.*;

public class PatternStore
{
	private List<Pattern> patterns = new LinkedList<>();
	private Map<String,List<Pattern>> mapAuths = new HashMap<>();
	private Map<String,Pattern> mapName = new HashMap<>();

	public PatternStore(String source) throws IOException
	{
		if (source.startsWith("http://") || source.startsWith("https://"))
			loadFromURL(source);
		else
			loadFromDisk(source);
	}

	public PatternStore(Reader source) throws IOException
	{
		load(source);
	}

	private void load(Reader r) throws IOException
	{
		BufferedReader br = new BufferedReader(r);
		for (String l; (l = b.readLine()) != null; )
		{
			try
			{
				Pattern p = new Pattern(l);
				patterns.add(p);
				mapName.put(p.getName(), p);
				if (!mapAuths.containsKey(p.getAuthor()))
					mapAuths.put(p.getAuthor(), new LinkedList<Pattern>());
				mapAuths.get(p.getAuthor()).add(p);
			}
			catch (PatternFormatException)
				System.out.println("WARNING: " + l);
		}
	}

	private void loadFromURL(String url) throws IOException
	{
		URL dest = new URL(url);
		URLConnection conn = dest.openConnection();
		load(new InputStreamReader(conn.getInputStream()));
	}

	private void loadFromDisk(String filename) throws IOException
	{
		load(new FileReader(filename));
	}

	public List<Pattern> getPatternsNameSorted()
	{
		return Collections.sort(new LinkedList<Pattern>(mapName.values()),
				new Comparator<Pattern>()
				{
					public int compare(Pattern p1, Pattern p2)
					{
						return p1.getName().compareTo(p2.getName());
					}
				});
	}

	public List<Pattern> getPatternsAuthorSorted()
	{
		// TODO: Get a list of all patterns sorted by author then name
	}

	public List<Pattern> getPatternsByAuthor(String author) throws PatternNotFound
	{
		try
			return mapAuth.get(new LinkedList<Pattern>(author));
		catch
			throw new PatternNotFound("");
	}

	public Pattern getPatternByName(String name) throws PatternNotFound
	{
		try
			return mapName.get(name);
		catch
			throw new PatternNotFound("");
	}

	public List<String> getPatternAuthors()
	{
		return Collections.sort(mapAuth.keySet());
	}

	public List<String> getPatternNames()
	{
		return Collections.sort(mapName.keySet());
	}

	public static void main(String args[])
	{
		PatternStore p = new PatternStore(args[0]);
	}
}
