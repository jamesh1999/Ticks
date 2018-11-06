datatype 'a tree = Lf
	| Br of 'a * 'a tree * 'a tree;

(* Swaps the right branch to the left at each position
	then "pushes" each value down one level inserting v at the root *)
fun tcons v Lf = Br (v, Lf, Lf)
	| tcons v (Br (w, t1, t2)) = Br (v, tcons w t2, t1);
(* Creates functional array by calling tcons on the list in reverse order *)
fun arrayoflist [] = Lf
	| arrayoflist (x::xs) = tcons x (arrayoflist xs);

(* Removes the root element from the tree
	then rearranges so the "next" element is the root
	Functionally an inverse to tcons although returns 'a tree
	'a tree -> 'a tree *)
fun tpop (Br (x, left as Br (y,_,_), right)) = Br (y, right, tpop left)
	| tpop _ = Lf;
(* Creates a list from a functional array by popping the root off repeatedly *)
fun listofarray Lf = []
	| listofarray (tree as Br (x,_,_)) = x :: (listofarray (tpop tree));

(* Alternative method: *)
(* Takes elements alternately from each list and constructs a new list from them 
	'a list -> 'a list -> 'a list *)
fun splice (x::xs) ys = x :: splice ys xs
	| splice _ _ = [];
(* Recursively splices the list representation of the left and the right branches *)
fun listofarray (Br(x, left, right)) = x :: splice (listofarray left) (listofarray right)
	| listofarray Lf = [];

(*Code golf!!*)
fun p(Br(x,l as Br(y,_,_),r))=Br(y,r,p l)|p _=Lf;fun q(t as Br(x,_,_))=x::q(p t)|q _=[];val listofarray=q;
fun s(h::t,l)=h::s(l,t)|s _=[];fun f(Br(x,l,r))=x::s(f l,f r)|f _=[];val listofarray=f;

val getEvenPositions =
	let
		(* Number is even -> true, Number is odd -> false*)
		fun even x = (x mod 2) = 0
		(* Encapsulate the internal function to hide the accumulator to the user *)
		fun internal n [] = []
			| internal n (x::xs) = 
				if even x then n :: (internal (n+1) xs)
				else (internal (n+1) xs)

	in
		internal 1
	end;
(* Takes a tree, converts it to a list, then gets the indices of the even elements *)
val getSubsOfEvens = getEvenPositions o listofarray;