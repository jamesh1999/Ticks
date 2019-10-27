package uk.ac.cam.jhah2.Algorithms.Tick1;

import uk.ac.cam.rkh23.Algorithms.Tick1.*;

public class MaxCharHeap implements MaxCharHeapInterface
{
	private int m_length;
	private char[] m_arr;

	private void siftDown(int idx)
	{
		// Continue while greater than all existing children
		while ((m_length > idx * 2 + 1 && m_arr[idx] < m_arr[idx * 2 + 1])
			|| (m_length > idx * 2 + 2 && m_arr[idx] < m_arr[idx * 2 + 2]))
		{
			// Move to next node
			if (m_arr[idx * 2 + 1] > m_arr[idx * 2 + 2])
				idx = idx * 2 + 1;
			else
				idx = idx * 2 + 2;

			// Swap value with parent
			char tmp = m_arr[idx];
			m_arr[idx] = m_arr[(idx - 1) >> 1];
			m_arr[(idx - 1) >> 1] = tmp;
		}
	}

	private void siftUp(int idx)
	{
		// Continue while greater than all existing children & not root
		while (idx != 0 && m_arr[idx] > m_arr[(idx - 1) >> 1])
		{
			// Swap value with parent
			char tmp = m_arr[idx];
			m_arr[idx] = m_arr[(idx - 1) >> 1];
			m_arr[(idx - 1) >> 1] = tmp;

			// Move to next node
			idx = (idx - 1) >> 1;
		}
	}

	// Constructs a heap from the data in m_arr
	private void makeHeap()
	{
		for (int i = m_length - 1; i >= 0; --i)
			siftDown(i);
	}

	public MaxCharHeap(String s)
	{
		if (s.length() != 0)
			m_arr = s.toLowerCase().toCharArray();
		else
			m_arr = new char[1];

		m_length = s.length();

		makeHeap();
	}

	// Get and remove the maximum value (or exception if empty)	
	public char getMax() throws EmptyHeapException
	{
		if (m_length == 0) throw new EmptyHeapException();

		char ret = m_arr[0];

		// Construct smaller heap
		m_arr[0] = m_arr[m_length - 1];
		--m_length;
		siftDown(0);

		return ret;
	}

	// Insert a new value into the heap
	public void insert(char i)
	{
		i = Character.toLowerCase(i);

		// Copy array to double sized
		if (m_length == m_arr.length)
		{
			char[] nArr = new char[m_arr.length * 2];
			for (int j = 0; j < m_length; ++j)
				nArr[j] = m_arr[j];
			m_arr = nArr;
		}

		m_arr[m_length] = i;
		siftUp(m_length);
		++m_length;
	}

	// Get the number of items in the heap
	public int getLength() { return m_length; }
}