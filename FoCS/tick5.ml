type color = int*int*int;
type xy = int*int;
datatype image =
    Image of xy * color array array;

fun image ((x, y):xy) (col:color) =
    let val data = Array.array (y, Array.array (0, col))
        fun populateRows dat 0 = ()
            | populateRows dat n =
                (Array.update (dat, n-1, Array.array(x, col));
                populateRows dat (n-1))
    in
        populateRows data y;
        Image ((x, y), data)
    end;

fun size (Image (dim, _)) = dim;

fun drawPixel (Image (dim, data)) col ((x, y):xy) =
    Array.update(Array.sub(data, y), x, col);

fun drawHoriz _ _ _ 0 = () 
    | drawHoriz img col ((x,y):xy) len =
        (drawPixel img col (x,y);
        drawHoriz img col (x+1, y) (len-1));

fun drawVert _ _ _ 0 = ()
    | drawVert img col ((x,y):xy) len =
        (drawPixel img col (x,y);
        drawVert img col (x, y+1) (len-1));

fun drawDiag _ _ _ 0 = ()
    | drawDiag img col ((x,y):xy) len =
        (drawPixel img col (x,y);
        drawDiag img col (x+1, y+1) (len-1));

fun drawLine img col ((x1,y1):xy) ((x2,y2):xy) =
    let val dx = abs(x2-x1)
        val dy = abs(y2-y1)
        val sx = if x1 < x2 then 1 else ~1
        val sy = if y1 < y2 then 1 else ~1
        fun nextPixel (x,y) err =
            (drawPixel img col (x,y);
            if x = x2 andalso y = y2 then ()
            else
                nextPixel (if 2*err > ~dy then x+sx else x,
                        if 2*err < dx then y+sy else y)
                        (err 
                            + (if 2*err > ~dy then ~dy else 0) 
                            + (if 2*err < dx then dx else 0)));

    in nextPixel (x1,y1) (dx-dy) end;

fun toPPM (Image ((w,h), data)) filename =
        let val oc = TextIO.openOut filename
            fun padString n s =
                if n > String.size s then
                    " " ^ padString (n-1) s
                else s;
            fun pixelToString ((r,g,b):color) =
                (padString 4 (Int.toString r)) ^ (padString 4 (Int.toString g))
                ^ (padString 4 (Int.toString b));
            fun outputRow row =
                TextIO.output(oc, Array.foldr (fn (pix, acc) => (pixelToString
                pix) ^ acc) "\n" row);
        in
            TextIO.output(oc,"P3\n" ^ Int.toString w ^ " " ^ Int.toString h ^
            "\n255\n");

            Array.app outputRow data;

            TextIO.closeOut oc
        end;
