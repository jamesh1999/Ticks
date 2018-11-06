(* Part 1 *)
fun nfold f 0 x = x
	| nfold f n x = nfold f (n-1) (f x);

(* Successor function *)
fun succ x = x + 1;

(* Sum two ints *)
(* int -> int -> int *)
(* Equivalent to: fun sum x y = nfold succ x y *)
val sum = nfold succ;

(* Add x y times to the additive identity *)
(* int -> int -> int *)
fun product x y = nfold (sum x) y 0;

(* Multiply the identity (1) y times by x *)
fun power x y = nfold (product x) y 1;

datatype 'a stream = Nil | Cons of 'a * (unit -> 'a stream);
fun from k = Cons(k, fn()=> from(k+1));

(* Part 2 *)
(* Takes the nth element from a stream starting at 1!! *)
(* 'a stream * int -> 'a *)
exception EndOfStream;
fun nth (Nil, _) = raise EndOfStream
	| nth (Cons (x, xf), 1) = x
	| nth (Cons (x, xf), n) = nth ((xf()), (n-1));

(* Part 3 *)
fun mapq f Nil = Nil
	| mapq f (Cons (x, xf)) = Cons (f x, fn () => (mapq f (xf())));
fun square x = x*x;

val squares = mapq square (from 1);

(* Part 4 *)
fun map2 f Nil _ = Nil
	| map2 f _ Nil = Nil
	| map2 f (Cons (x, xf)) (Cons (y, yf)) =
		Cons (f x y, fn () => map2 f (xf()) (yf()));