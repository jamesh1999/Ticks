datatype 'a stream = Nil
	| Cons of 'a * (unit -> 'a stream);

(* Part 1 *)
(* 'a stream * 'a stream -> 'a stream *)
fun merge (Nil, yq) = yq
	| merge (xq, Nil) = xq
	| merge (xq as Cons (x, xf), yq as Cons (y, yf)) =
		if x < y then Cons (x, fn () => (merge (xf(), yq)))
		else if y < x then Cons (y, fn () => (merge (xq, yf())))
		else Cons (x, fn () => (merge (xf(), yf()))) (* If equal => Force both qs *)

(* Part 2 *)
fun mul x y = x*y;

(* Map for streams *)
(* ('a -> 'b) -> 'a stream -> 'b stream *)
fun mapq f Nil = Nil
	| mapq f (Cons (x, xf)) = Cons (f x, fn () => (mapq f (xf())));
fun tl (Cons (_, xf)) = xf();

val powq = 
	let 
		fun internal acc x =
			Cons (acc, fn () => (internal (acc*x) x))
	in internal 1 end;

val pows2 = powq 2;

fun fn_pows23() = Cons (1, fn () =>
	merge (tl pows2, mapq (mul 3) (fn_pows23())));
val pows23 = fn_pows23();

(* Part 3 *)

fun fn_pows235() = Cons (1, fn () =>
	merge (tl pows23, mapq (mul 5) (fn_pows235())));
val pows235 = fn_pows235();