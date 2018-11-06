SELECT title, count(*) AS total
FROM movies
JOIN (
	SELECT  *
	FROM keywords
	WHERE keyword IN (
		SELECT keyword
		FROM keywords
		JOIN movies ON movie_id = id
		WHERE title = 'Skyfall (2012)'
	)
) ON movie_id = id
WHERE title <> 'Skyfall (2012)'
GROUP BY title
ORDER BY total DESC, title
LIMIT 10;