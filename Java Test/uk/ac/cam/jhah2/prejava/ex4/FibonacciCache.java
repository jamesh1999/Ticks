package uk.ac.cam.jhah2.prejava.ex4;

public class FibonacciCache {

   // Uninitialised array
   public static long[] fib = null;

   public static void store() throws Exception {
      if(fib == null) throw new Exception("Cache is uninitialised");
      
      for(int i = 0; i < fib.length; ++i)
      {
         if(i < 2) fib[i] = 1;
         else fib[i] = fib[i - 2] + fib[i - 1];
      }
   }

   public static void reset(int cachesize) {
      fib = new long[cachesize];
      for(int i = 0; i < cachesize; ++i)
         fib[i] = 0;
   }
 
   public static long get(int i) throws Exception {
      if(fib == null) throw new Exception("Cache is uninitialised");

      if(i < 0 || i >= fib.length) throw new Exception("Index out of range");

      return fib[i];
   }

   public static void main(String[] args) {
      try
      {
         reset(20);
         store();
         int i = Integer.decode(args[0]);
         System.out.println(get(i));
      }
      catch (Exception e)
      {
         System.out.println(e.getMessage());
      }
   }
  
}