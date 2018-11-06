4.4
No negative or zero value coins in till => Will lead to infinite recursion & stack overflow
Till is sorted in decreasing order => Might lead to missing some combinations

A.2
(*Applies a function to every element and returns a list
	(a' -> b') -> a' list -> b' list *)
fun map f [] = []
	| map f (h::t) = (f h)::(map f t);

(* a' list list -> a' list *)
fun flatten [] = []
	| flatten (h::t) = h @ (flatten t);

(*Returns a list of possible lists after inserting x into the list
	a' -> a' list -> a' list list *)
fun distribute x [] = [[x]]
	| distribute x (h::t) = (x::h::t) :: (map (fn l => h::l) (distribute x t));

(*Generates permutations by distributing the head into all the permutations of the tail
	a' list -> a' list list *)
fun perms [] = [[]]
	| perms (h::t) = flatten (map (distribute h) (perms t));

(*Removes the first element matching x from list l
	a' -> a' list -> a' list *)
fun remove x [] = []
	| remove x (h::t) =
		if x = h then t
		else h :: (remove x t);

(*Generates permutations by removing each element in turn then placing it at the head of the permutations of the remaining elements
	a' list -> a' list list *)
fun permsLexicographic [] = [[]]
	| permsLexicographic xs =
		flatten (map (fn x => map (fn ys => x::ys) (permsLexicographic (remove x xs))) xs);

5.1
O(n^2):
For a list of length 1, no comparison is required => T(1) = 0
For a list of length n, finding the smallest element needs (n-1) comparisons. Then this process must be repeated for the remaining list of size (n-1) => T(n) = T(n-1) + (n-1)
=> T(n) = (n-1) + (n-2) + ... + 2 + 1
=> T(n) = (n-1) + 1 + (n-2) + 2 + ...
=> T(n) = 0.5 * n * (n-1)
=> T(n) = O(n^2)

5.2
(* Brings lowest element to the start of the list
	'a list -> 'a list *)
fun select [] = []
	| select [x] = [x]
	| select (min::x::xs) =
		if min <= x then select (min::xs) @ [x]
		else select (x::xs) @ [min];

(* As before but with the append removed *)
val select =
	let
		(* Internal function hides accumulator *)
		fun internal acc [] = acc
			| internal acc [x] = x :: acc
			| internal acc (min::x::xs) =
				if min <= x then internal (x::acc) (min::xs)
				else internal (min::acc) (x::xs)
	in
		internal []
	end;

(* Selection sort: Recursively calls select to get each smallest element in turn
	'a list -> 'a list *)
fun ssort [] = []
	| ssort [x] = [x]
	| ssort xs =
		let
			val (min::ys) = select xs
		in
			min :: ssort ys
		end;

A.3
(* Calls select n times to retrieve the n smallest elements in xs 
	'a list -> 'a list *)
fun least (0, xs) = []
	| least (n, xs) =
		let
			val (min::ys) = select xs
		in
			min :: least (n-1, ys)
		end;

6.1
datatype WeekDay = Monday
	| Tuesday
	| Wednesday
	| Thursday
	| Friday
	| Saturday
	| Sunday;

Pros:
- Little confusion about what Monday, Tuesday etc will represent within the code
- Helps determine errors within the program during type checking: An integer could represent anything whereas a WeekDay is specific
- Easy to change order / Add or remove days (Not that this is ever likely to happen but maybe the calendar app would need to separate working days from weekends?)
- Helps with domain driven design so you can write e.g. makeBooking Tuesday Afternoon Supervision
Cons:
- (Not that this is a concern in modern computers) Increases the size of the program vs using 0,1,2,3...

6.3
Constructs a tree of depth n with values at each vertex that correspond to the indices of that vertex if the tree represented a functional array

6.4
datatype Expr = Real of real
	| Var of string
	| Neg of Expr
	| Add of Expr * Expr
	| Mul of Expr * Expr;

6.5
exception UndefinedVariable of string;
fun eval (Real x) = x
	| eval (Add (e1, e2)) = eval e1 + eval e2
	| eval (Neg (e)) = ~ (eval e)
	| eval (Mul (e1, e2)) = eval e1 * eval e2
	| eval (Var s) = raise (UndefinedVariable s);

7.1
Alice
    \
    Tobias
   /
Gerald
	\
	Lucy

	Gerald
	/    \
Alice	  Lucy
			\
			Tobias

7.2
exception Collision;
fun insertNew k Lf = Br (k, Lf, Lf)
	| insertNew k (Br (x, left, right)) = 
	if x = k then raise Collision
	else
		if k < x then Br (x, insertNew k left, right)
		else Br (x, left, insertNew k right);

7.3
The dictionary is polymorphic so the value stored could be of any type. Therefore, the exception Collision would also have to be polymorphic to allow it to return any type

7.4
Navigate down the tree to the node to be deleted
Check the depth of each of its sub branches
If the longest branch is the right then:
	Take the left branch and "graft" it instead of the leftmost leaf on the right branch
	Then return this new branch in place of the node to be deleted
Otherwise do the mirror image

If the tree needs to be kept balanced, this will not always return the optimal tree so after several operations it may be necessary to "rebalance" the tree.

7.5
exception CannotGraft;
(* Attaches an entire branch to a tree in place of a leaf if there is space 
	'a tree -> 'a tree -> 'a tree *)
fun graft br Lf = br
	| graft (br as Br (k,_,_)) (Br (x, left, right)) =
		if k=x then raise CannotGraft
		else
			if k<x then Br (x, graft br left, right)
			else Br (x, left, graft br right);

exception KeyNotFound;
(* Removes an entry from a binary search tree 
	'a -> 'a tree -> 'a tree *)
fun delete k Lf = raise KeyNotFound
	| delete k (Br (x, left, right)) =
		if x=k then
			if (depth right) > (depth left) then graft left right
			else graft right left
		else
			(* Continue search for node *)
			if k<x then Br (x, delete k left, right)
			else Br (x, left, delete k right);

7.6
The @ operator performs n+m :: operations where the lists involved are of length n and m
Consider the tree where only the right branches of each node are used and all the left branches are leaves
When the tree height is n, all functions return a list of length n
Therefore concatenating the three lists at height n+1 gives (1 + n) + (0 + n) = 2n+1 :: operations
This leads to the recurrance relation: T(n+1) = T(n) + 2n + 1
Which means T(n) = O(n^2)

7.7
Assuming the :: operation takes 1 unit of time
There is 1 :: operation performed for each function call
Also from the recursive calls: T(n+m) = T(n) + T(m) + 1; T(1) = 1
Where n+m is the tree size
Therefore T(n+1) = T(n) + T(1) + 1
= T(n) + 2
Which leads to linear time: T(n) = O(n)

7.8
(* Removes the root element from the tree
	then rearranges so the "next" element is the root
	Functionally an inverse to tcons although returns 'a tree
	'a tree -> 'a tree *)
fun tpop (Br (x, left as Br (y,_,_), right)) = Br (y, right, tpop left)
	| tpop _ = Lf;