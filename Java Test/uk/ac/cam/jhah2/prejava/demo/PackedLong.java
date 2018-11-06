package uk.ac.cam.jhah2.prejava.demo;

class PackedLong {
   private long bools = 0;

   public void set(int idx, boolean val)
   {
   		bools = val
   			? bools | (1 << idx)
   			: bools & ~(1 << idx);
   }

   public boolean get(int idx)
   {
   		return bools & (1 << idx) != 0;
   }
}