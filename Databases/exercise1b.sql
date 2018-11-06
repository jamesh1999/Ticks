SELECT title, G3.genre
FROM movies
JOIN genres AS G1 ON id = G1.movie_id
JOIN genres AS G2 ON id = G2.movie_id
JOIN genres AS G3 ON id = G3.movie_id
WHERE G1.genre = 'Romance' AND G2.genre = 'Comedy' AND G3.genre <> 'Romance' AND G3.genre <> 'Comedy'
ORDER BY title, genre;