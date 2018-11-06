SELECT title
FROM movies
JOIN genres AS G1 ON id = G1.movie_id
JOIN genres AS G2 ON id = G2.movie_id
WHERE G1.genre = 'Romance' AND G2.genre = 'Comedy'
ORDER BY title;