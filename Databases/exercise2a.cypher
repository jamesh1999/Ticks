match (p:Person)-[:WROTE]->(m:Movie)-[:HAS_GENRE]->(g:Genre)
 where g.genre="Drama"
 return p.name as name, count(*) as total
 order by total desc, name
 limit 10;