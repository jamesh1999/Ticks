package uk.ac.cam.cl.jhah2.exercises;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.*;
import uk.ac.cam.cl.mlrd.utils.*;
import uk.ac.cam.cl.mlrd.utils.BestFit.Point;
import uk.ac.cam.cl.mlrd.utils.BestFit.Line;

public class Exercise3
{
	private Map<String, Integer> m_wordCounts;
	private List<Integer> m_unique;

	private long totwords;

	private void importReviews(Path reviewsDir) throws IOException
	{
		DirectoryStream<Path> ds = Files.newDirectoryStream(reviewsDir);

		long wordCount = 0;

		for (Path child : ds)
		{
			List<String> words = Tokenizer.tokenize(child);

			for (String word : words)
			{
				if (!m_wordCounts.containsKey(word)) m_wordCounts.put(word, 0);
				m_wordCounts.put(word, m_wordCounts.get(word) + 1);

				++wordCount;

				if (Math.pow(2, m_unique.size()) <= wordCount)
					m_unique.add(m_wordCounts.size());
			}
		}

		totwords = wordCount;
	}

	public Exercise3()
	{
		m_wordCounts = new HashMap<>();
		m_unique = new ArrayList<>();
	}

	private double k = 0.0;
	private double a = 0.0;
	public void plotZipf()
	{
		List<Point> points = new ArrayList<>();
		Map<Point, Double> pointWeights = new HashMap<>();

		List<Map.Entry<String, Integer>> kvps = new ArrayList<>(m_wordCounts.entrySet());
		Collections.sort(kvps, (a, b) -> b.getValue().compareTo(a.getValue()));

		Map<String, Integer> ranks = new HashMap<>();

		int i = 0;
		for (Map.Entry<String, Integer> kvp : kvps)
		{
			++i;
			
			ranks.put(kvp.getKey(), i);

			if (i >= 10000) continue;
			Point p = new Point(Math.log(i), Math.log(kvp.getValue()));
			points.add(p);
			pointWeights.put(p, (double)kvp.getValue());
		}

		Line best = BestFit.leastSquares(pointWeights);
		List<Point> linePts = new ArrayList<>();
		linePts.add(new Point(0, best.yIntercept));
		linePts.add(new Point(-best.yIntercept / best.gradient, 0));

		k = Math.exp(best.yIntercept);
		a = -best.gradient;

		System.out.println(k);
		System.out.println(a);

		String[] s2 = {"gripping", "enthralling", "abysmal", "enjoyed", "loved", "hated", "ingenious", "shallow", "poorly", "fake"};
		for (String s : s2)
		{
			System.out.println(s);
			System.out.println(k / Math.pow(ranks.get(s), a));
			System.out.println(m_wordCounts.get(s));
		}

		//ChartPlotter.plotLines(points, linePts);

	}

	void plotHeaps()
	{
		List<Point> pts = new ArrayList<>();
		int i = 0;
		for (Integer dp : m_unique)
		{
			pts.add(new Point(Math.log(Math.pow(2,i)), Math.log(dp)));
			++i;
		}
		pts.add(new Point(Math.log(totwords), Math.log(m_wordCounts.size())));

		ChartPlotter.plotLines(pts);
	}

	static final Path dataDirectory = Paths.get("large_dataset");

	public static void main(String[] args) throws IOException {

		Exercise3 ex = new Exercise3();
		

		ex.importReviews(dataDirectory);

		//ex.plotZipf();
		ex.plotHeaps();
	}
}