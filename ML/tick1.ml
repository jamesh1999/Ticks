fun evalquad (a,b,c,x) : real = a*x*x + b*x + c;

fun facr n =
	if n <= 0 then 1
	else n * facr (n-1);

fun faci n =
	let 
		fun fac_internal i n tot = 
		if n >= i then fac_internal (i+1) n tot*i
		else tot
	in fac_internal 2 n 1
	end;

fun sumt 0 = 0.0
	| sumt n = 1.0 + (sumt (n-1)) / 2.0;