1.1 
Pros:
	- It is a concise representation using only 2 bytes of memory. This may be important where memory usage is a concern (embedded systems) or where large numbers of dates are stored in records.
	- It is easy to convert from this representation to a string representation of the date.
Cons:
	- It introduces a "2050" bug where the same issue will occur because dates will wrap around to 1950.
	- It doesn't make efficient usage of the 2 bytes it uses: 2^16 combinations allows more than 100 dates to be stored.

1.2
type date = int * int;
fun comp (d1:date, d2:date) =
	let
		val (h1,t1) = d1 (*Split date tuples into their 'head'/'tail' digits*)
		val (h2,t2) = d2
	in
		if h1 div 5 = h2 div 5 then h1*10+t1 > h2*10+t2 (*If can be compared directly...*)
		else h2>h1 (*If d2 is in the range 1950-1999*)
	end;

(*Simpler solution by changing the date representation internally...*)
fun twoDigitToInt (h,t) =
	if h >= 5 then 1900 + 10*h + t
	else 2000 + 10*h + t;

fun intToTwoDigit d =
	if d < 1950 orelse d > 2049 then (~1, ~1) (*Represents a date out of range*)
	else ((d div 10) mod 10, d mod 10);

fun comp (d1:date, d2:date) =
	(twoDigitToInt d1) > (twoDigitToInt d2);

fun add (d1:date, i) =
	intToTwoDigit (twoDigitToInt d1 + i);

1.4
Only power needs its type constraint: npower's return type is known from the true branch of its if clause returning 1.0 (a real)

1.6
val phi = (1.0 + Math.sqrt 5.0) / 2.0;
fun test 0 = phi
	| test n = 1.0 / (test (n-1) - 1.0);

Gives the negative root: ~0.618...

2.1
fun power (x, n) =
	let
		(*Internal function hides accumulator to user*)
		fun internal (x, 0, acc) = acc
			| internal (x, n, acc) =
				if n mod 2 = 0 then internal (x*x, n div 2, acc)
				else internal (x*x, n div 2, acc * x)
	in
		internal (x, n, 1.0)
	end;

A.1
fun compose a b x = a (b x);
val epsilon = 0.000000001;
fun fpow (x, n) =
	(*Handle -ve powers*)
	if n < 0.0 then 1.0 / fpow (x, ~n)
	else
		let
			(*Split parts because we treat them separately*)
			val intPart = floor
			fun realPart x = x - real (intPart x)

			fun even x = x mod 2 = 0

			(*Split into internal & 1 to allow currying of term/mut*)
			fun internal branch term mut =
			    let
    				fun internal1 (x, n) acc =
    					if term (x, n) then acc
    					else
    						if branch n then internal1 (mut (x,n)) acc
    						else internal1 (mut (x,n)) (acc * x)
    			in
    			    internal1
    			end

		in
			(*Int part*)
			internal
			    even
				(fn (x,n) => n = 0) (*Terminate: int part exact*)
				(fn (x,n) => (x*x, n div 2)) (*Same mutation as from power (x, n)*)
				(x, intPart n) 1.0
			(*Real part*)
			* internal
			    (compose even floor)
				(fn (x,n) => x - 1.0 < epsilon) (*Terminate: x is small*)
				(fn (x,n) => (Math.sqrt x, 2.0 * realPart n))
				(x, realPart n) 1.0
		end;


2.2
f(n) = O(a1g1(n) + a2g2(n) + ... + akgk(n))
<=> f(n) <= a1g1(n) + a2g2(n) + ... + akgk(n)

Let c = max[a]
=> f(n) <= cg1(n) + cg2(n) + ... + cgk(n)
<=> f(n) <= c(g1(n) + g2(n) + ... + gk(n))
<=> f(n) = O(g1(n) + g2(n) + ... + gk(n))

3.1
fun sumr [] = 0
	| sumr h::t = h + sumr t;

Time complexity: O(n)
Space complexity: O(n)

fun sumi l =
	let
		fun internal acc [] = acc
			| internal acc (h::t) = internal (acc + h) t
	in
		internal 0 l
	end;

Time complexity: O(n)
Space complexity: O(1)

3.2
Thinking about the list in a box/arrow representation, there is no way to locate the last element without traversing the entire list. Therefore, the lower bound on complexity is O(n)

fun last [] = raise Match
	| last (h::[]) = h
	| last (_::t) = last t;

3.3
fun even [] = []
	| even (_::[]) = []
	| even (ho::(he::t)) = he :: (even t);

4.1
fun contains [] x = false
	| contains (h::t) x =
		if h = x orelse contains t x;
fun union l [] = l
	| union l (h::t) =
		if contains l h then union l t
		else union (h::l) t;

4.2
fun split l =
	let
		fun internal [] acc = acc
			| internal (h::t) (pos, neg) =
				(*Add head to first if positive, second if negative*)
				if h >= 0 then internal t (h::pos, neg)
				else internal t (pos, h::neg)
	in
		internal l ([], [])
	end;

4.3
It will only match when lists have the same length

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