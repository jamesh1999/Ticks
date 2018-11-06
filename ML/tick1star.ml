fun eapprox n =
	let
		fun internal i =
			if i > n then 0.0
			else 1.0 + (internal (i+1)) / (real i)
	in
		internal 1
	end;

fun exp (z,n) =
	let
		fun internal i =
			if i > n then 0.0
			else 1.0 + (internal (i+1)) * z / (real i)
	in
		internal 1
	end;

fun gcd (a,b) =
	if a = b then a
	else
		case (a mod 2, b mod 2) of (0, 0) => 2 * gcd (a div 2, b div 2)
		| (1, 0) => gcd (a, b div 2)
		| (0, 1) => gcd (a div 2, b)
		| (_, _) => 
			if a > b then gcd (b, a - b)
			else gcd (a, b - a);