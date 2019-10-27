import queue



def breadth_first_search(connections, src):
	previous = {}
	for node in connections:
		previous[node] = False

	# Expand source node
	q = queue.Queue()
	q.put(src)
	previous[src] = src

	while not q.empty():
		node = q.get()

		for nxt in connections:
			# If connected & not visited
			if not connections[node][nxt]: continue;
			if previous[nxt]: continue;

			previous[nxt] = node
			q.put(nxt)

	return previous



def reduced_network(capacity, flow):
	# Network only contains edges with spare capacity
	connections = {}
	for n1 in capacity:
		connections[n1] = {}
		for n2 in capacity:
			connections[n1][n2] = capacity[n1][n2] - flow[n1][n2] > 0

	return connections



def find_augmenting_path(capacity, flow, src, dst):
	previous = breadth_first_search(reduced_network(capacity, flow), src)

	# No path
	if not previous[dst]: return 0, None

	# Follow backtracking pointers & store bottlenecked amount
	min_spare = None
	path = [dst]
	while path[-1] != src:
		path.append(previous[path[-1]])
		spare = capacity[path[-1]][path[-2]] - flow[path[-1]][path[-2]]

		if min_spare == None or spare < min_spare:
			min_spare = spare

	return min_spare, path



def compute_max_flow(capacity, s, t):

	# Set of nodes in network
	nodes = {}
	for (n1, n2) in capacity:
		nodes[n1] = None
		nodes[n2] = None

	# 0 filled capacity/flow matrices
	capacities = {}
	flows = {}
	for start in nodes:
		capacities[start] = {}
		flows[start] = {}

		for end in nodes:
			capacities[start][end] = 0
			flows[start][end] = 0

	# Fill capacity matrix
	for (start, end) in capacity:
		capacities[start][end] = capacity[(start, end)]

	tot_flow = 0

	new_flow, aug = find_augmenting_path(capacities, flows, s, t)
	while new_flow:
		for i in range(len(aug) - 1):
			flows[aug[i + 1]][aug[i]] += new_flow
			flows[aug[i]][aug[i + 1]] -= new_flow

		tot_flow += new_flow
		new_flow, aug = find_augmenting_path(capacities, flows, s, t)

	# Convert the flows in to the given format
	edge_flows = {}
	for start in nodes:
		for end in nodes:
			if flows[start][end] <= 0: continue;

			edge_flows[(start, end)] = flows[start][end]

	# Compute the minimum cut
	min_cut_prev = breadth_first_search(reduced_network(capacities, flows), s)
	min_cut = [node for node in min_cut_prev if min_cut_prev[node]]

	return (tot_flow, edge_flows, min_cut)

import csv
with open('flownetwork_02.csv') as f:
    rows = [row for row in csv.reader(f)][1:]
capacity = {(u, v): int(c) for u,v,c in rows}

print(compute_max_flow(capacity, '0', '5'))