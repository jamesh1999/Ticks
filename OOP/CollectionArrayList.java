import java.util.AbstractList;

public class CollectionArrayList<T> extends AbstractList<T>
{
	private Object[] m_data = new Object[1];
	private int m_size = 0;

	@SuppressWarnings("unchecked")
	public T get(int idx)
	{
		if (idx >= m_size || idx < 0) throw new IndexOutOfBoundsException("");
		return (T) m_data[idx];
	}

	public int size()
	{
		return m_size;
	}

	@Override
	public T set(int idx, T val)
	{
		if (idx >= m_size || idx < 0) throw new IndexOutOfBoundsException("");

		@SuppressWarnings("unchecked")
		T prev = (T) m_data[idx];
		m_data[idx] = val;
		return prev;
	}

	@Override
	public void add(int idx, T val)
	{
		if (idx > m_size || idx < 0) throw new IndexOutOfBoundsException("");

		Object[] source = m_data;
		int i = idx;

		// Reallocate if necessary
		if (m_size == m_data.length)
		{
			m_data = new Object[2 * m_size];
			i = 0;
		}

		// Copy data into new location
		for (; i <= m_size; ++i)
		{
			if (idx < i)
				m_data[i] = source[i - 1];
			else if (idx == i)
				m_data[i] = val;
			else // idx > i
				m_data[i] = source[i];
		}

		++m_size;
	}

	@Override
	public T remove(int idx)
	{
		@SuppressWarnings("unchecked")
		T val = (T) m_data[idx];

		// Copy data into new location
		for (int i = idx + 1; i < m_size; ++i)
			m_data[i - 1] = m_data[i];
		
		// Enable garbage collection if other references to this object are removed
		m_data[m_size - 1] = null;

		--m_size;

		return val;
	}

	public static void main(String[] args)
	{
		CollectionArrayList<Integer> l = new CollectionArrayList<>();
		l.add(1);
		l.add(2);
		l.add(3);
		l.add(4);
		System.out.println(l.get(2));
		System.out.println(l.remove(2));
		l.add(5);
		System.out.println(l.get(3));
		System.out.println(l.set(0, 0));
		System.out.println(l.get(0));
		System.out.println(l.size());
	}
}