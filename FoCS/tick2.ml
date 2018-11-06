exception EmptyList;
fun last [] = raise EmptyList
	| last [x] = x
	| last (_::xs) = last xs;

fun butlast (x1::x2::xs) = x1 :: butlast (x2::xs)
	| butlast _ = [];

exception IndexError;
fun nth (x::xs, 0) = x
	| nth ([], _) = raise IndexError
	| nth (x::xs, n) = nth (xs, n-1);