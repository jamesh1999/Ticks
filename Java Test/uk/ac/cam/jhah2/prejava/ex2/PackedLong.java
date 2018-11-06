package uk.ac.cam.jhah2.prejava.ex2; //TODO: replace your-crsid

public class PackedLong {

   /*
    * Unpack and return the nth bit from the packed number at index position;
    * position counts from zero (representing the least significant bit)
    * up to 63 (representing the most significant bit).
    */
   public static boolean get(long packed, int idx)
   {
      return (packed & (1L << idx)) != 0L;
   }

   /*
    * Set the nth bit in the packed number to the value given
    * and return the new packed number
    */
   public static long set(long packed, int idx, boolean val)
   {
      packed = val
        ? packed | (1L << idx)
        : packed & ~(1L << idx);
      return packed;
   }
}