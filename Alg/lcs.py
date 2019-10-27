def tableCell(tbl, i, j):
	if i >= 0 and j >= 0: return tbl[i][j]
	return 0

def table(s1, s2):
	tbl = [[0 for j in range(len(s2))] for i in range(len(s1))]

	for i in range(len(s1)):
		for j in range(len(s2)):
			if s1[i] == s2[j]:
				tbl[i][j] = 1 + tableCell(tbl, i-1, j-1)
			else:
				tbl[i][j] = max(tableCell(tbl, i-1, j), tableCell(tbl, i, j-1))

	return tbl

def match_length(tbl):
	len1 = len(tbl)
	len2 = len(tbl[0]) if len1 else 0

	return tbl[len1-1][len2-1] if len2 else 0

def match_string(s1, s2, tbl):
	ret = []

	if  tableCell(tbl, len(s1)-1, len(s2)-1) == 0: return ""

	if tableCell(tbl, len(s1)-1, len(s2)-1) == tableCell(tbl, len(s1)-2, len(s2)-1):
		return match_string(s1[:-1], s2, tbl)

	if tableCell(tbl, len(s1)-1, len(s2)-1) == tableCell(tbl, len(s1)-1, len(s2)-2):
		return match_string(s1, s2[:-1], tbl)

	return match_string(s1[:-1], s2[:-1], tbl) + s1[-1]