package uk.ac.cam.jhah2.prejava.demo;

public class ArrayTest
{
	public static void main(String[] args)
	{
		// Create a 2-by-2 array with all values initialised to zero
		int[][] i = new int[2][2]; 

		// 2D array of values with each 1D value of a different length
		int [][] j = {i[1],{1,2,3},{4,5,6,7}}; 

		// Create a 3D array using two 2D array references
		int [][][] k = {i,j};

		System.out.print(k[0][1][0]++);
		System.out.print("-");
		System.out.print(++k[1][0][0]);
		System.out.print("-");
		System.out.print(i[1][0]);
		System.out.print("-");
		System.out.print(--j[0][0]);
	}
}