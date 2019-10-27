package uk.ac.cam.jhah2.Algorithms.Tick1;

public interface MaxCharHeapInterface {
       // get and remove the maximum value (or exception if empty)	
       public char getMax() throws EmptyHeapException;
       // insert a new value into the heap
       public void insert(char i);
       // Get the number of items in the heap
       public int getLength();
}