package uk.ac.cam.jhah2.prejava.ex1; //TODO: replace your-crsid

public class SetBit {
   public static void main(String [] args) {
      long currentValue = Long.decode(args[0]);
      int position = Integer.parseInt(args[1]);
      boolean value = Boolean.parseBoolean(args[2]);
      currentValue = PackedLong.set(currentValue,position,value);       
      System.out.println(currentValue);
   }
}