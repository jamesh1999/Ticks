package uk.ac.cam.jhah2.oop.tick4;

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
		for (String l; (l = br.readLine()) != null; )
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
			catch (PatternFormatException e)
			{
				System.out.println("WARNING: " + l);
			}
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
		List<Pattern> copy = new LinkedList<>(patterns);
		Collections.sort(copy);
		return copy;
	}

	public List<Pattern> getPatternsAuthorSorted()
	{
		List<Pattern> copy = new LinkedList<>(patterns);
		Collections.sort(copy,
			(Pattern p1, Pattern p2)->
				p1.getAuthor().compareTo(p2.getAuthor()) != 0
				? p1.getAuthor().compareTo(p2.getAuthor())
				: p1.getName().compareTo(p2.getName()));
		return copy;
	}

	public List<Pattern> getPatternsByAuthor(String author) throws PatternNotFound
	{
		List<Pattern> result = mapAuths.get(author);
		if (result == null) throw new PatternNotFound("");
		List<Pattern> copy = new LinkedList<>(result);
		Collections.sort(copy);
		return copy;
	}

	public Pattern getPatternByName(String name) throws PatternNotFound
	{
		Pattern p = mapName.get(name);
		if (p == null) throw new PatternNotFound("");
		return p;
	}

	public List<String> getPatternAuthors()
	{
		List<String> keys = new LinkedList<>(mapAuths.keySet());
		Collections.sort(keys);
		return keys;
	}

	public List<String> getPatternNames()
	{
		List<String> keys = new LinkedList<>(mapName.keySet());
		Collections.sort(keys);
		return keys;
	}
}
