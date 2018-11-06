match (p:Person)-[:ACTS_IN]->(m:Movie),
(p:Person)-[:DIRECTED]->(m:Movie),
(p:Person)-[:PRODUCED]->(m:Movie),
(p:Person)-[:EDITED]->(m:Movie)
return p.name as name, m.title as title
order by name,title;