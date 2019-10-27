10.1
Generics are implemented in java using type erasure. This is possible because every class in java inherits from Object. Therefore, the generics can just operate on Object references with the appropriate type casting inserted by the compiler. Because primitives do not inherit Object, they cannot be stored in Object references and thus are unsupported by generics.
"new T()" is invalid for the same reason. Because T is erased, the actual object created will be an instance of Object and thus will not behave as a T.

10.2
public interface OOPList<T>
{
	public T nth();
	public void insert(T val);
	public void remove();
	public int length();
}

public class OOPLinkedList<T> implements OOPList<T>
{
	private class OOPLinkedListElement
	{
		public OOPLinkedListElement next;
		public T value;

		public OOPLinkedListElement (T val, OOPLinkedListElement next)
		{
			value = val;
			this.next = next;
		}
	}
	private OOPLinkedListElement head;

	public T head()
	{
		if (head == null) throw new Exception("Index out of range");
		return head.value;
	}

	public T nth(int idx)
	{
		OOPLinkedListElement current = head;
		for (; idx > 0; --idx)
		{
			if (current == null) throw new Exception("Index out of range");
			current = current.next;
		}

		if (current == null) throw new Exception("Index out of range");
		return current.value;
	}

	public void insert (T val)
	{
		head = new OOPLinkedListElement(val, head);
	}

	public void remove ()
	{
		if (head == null) throw new Exception("Index out of range");
		head = head.next;
	}

	public int length ()
	{
		OOPLinkedListElement current = head;
		int len = 0;
		while (current != null)
		{
			current = current.next;
			++len;
		}
		return len;
	}
}

10.3
public class PatternStore
{
	private List<Pattern> patterns = new ArrayList<>();
	private Map<String,List<Pattern>> mapAuths = new HashMap<>();
	private Map<String,Pattern> mapName = new HashMap<>();

	private void load(Reader r) throws IOException
	{
		BufferedReader br = new BufferedReader(r);
		for (String l; (l = br.readLine()) != null; )
		{
			try
			{
				Pattern p = new Pattern(l);
				patterns.add(p);
				mapName.put(p.getName(), p);
				if (!mapAuths.containsKey(p.getAuthor()))
					mapAuths.put(p.getAuthor(), new ArrayList<Pattern>());
				mapAuths.get(p.getAuthor()).add(p);
			}
			catch (PatternFormatException e)
			{
				System.out.println("WARNING: " + l);
			}
		}
	}

	private void loadFromURL(String url) throws IOException
	{
		URL dest = new URL(url);
		URLConnection conn = dest.openConnection();
		load(new InputStreamReader(conn.getInputStream()));
	}

	private void loadFromDisk(String filename) throws IOException
	{
		load(new FileReader(filename));
	}

	public PatternStore(String source) throws IOException
	{
		if (source.startsWith("http://") || source.startsWith("https://"))
			loadFromURL(source);
		else
			loadFromDisk(source);
	}

	public PatternStore(Reader source) throws IOException
	{
		load(source);
	}

	public List<Pattern> getPatternsNameSorted()
	{
		List<Pattern> copy = new ArrayList<>(patterns);
		copy.sort((Pattern p1, Pattern p2) ->
					p1.getName().compareTo(p2.getName()));
		return copy;
	}

	public List<Pattern> getPatternsAuthorSorted()
	{
		List<Pattern> copy = new ArrayList<>(patterns);
		copy.sort(
			((Pattern p1, Pattern p2) ->
				p1.getAuthor().compareTo(p2.getAuthor()))
			.thenComparing(
				(Pattern p1, Pattern p2) ->
					p1.getName().compareTo(p2.getName())));
		return copy;
	}

	public List<Pattern> getPatternsByAuthor(String author) throws PatternNotFound
	{
		List<Pattern> result = mapAuths.get(author);
		if (result == null) throw new PatternNotFound("");
		List<Pattern> copy = new ArrayList<>(result);
		copy.sort((Pattern p1, Pattern p2) ->
			p1.getName().compareTo(p2.getName()));
		return copy;
	}

	public Pattern getPatternByName(String name) throws PatternNotFound
	{
		Pattern p = mapName.get(name);
		if (p == null) throw new PatternNotFound("");
		return p;
	}

	public List<String> getPatternAuthors()
	{
		List<String> keys = new ArrayList<>(mapAuths.keySet());
		keys.sort((String s1, String s2) ->
			s1.compareTo(s2));
		return keys;
	}

	public List<String> getPatternNames()
	{
		List<String> keys = new ArrayList<>(mapName.keySet());
		keys.sort((String s1, String s2) ->
			s1.compareTo(s2));
		return keys;
	}
}

10.5
Wildcards are useful because a generic class of a supertype is not a supertype of a generic class of the subtype.
E.g. List<Integer> is not a superclass of List<Number>
However, in some applications, it may be desirable for a method to accept Lists of any class extending Number (e.g. to sum them). In this case a wildcard can be used: List<?> (or list of unknown) is considered a supertype of List<Integer> and list of anything other object. This can be bounded so List<? extends Number> resolving the issue. However, this will not support adding elements to the list because the type of the list might be more specified.
In the previous example, the wildcard is "upper bounded" because it is a superclass which has been specified and only derived classes can be substituted. We can also lower bound the wildcard with the super keyword. This behaves differently to before because we can add elements to the list. (List<Object>, List<Number>, etc can all accept Integers) However this time getting elements from the list is harder because any object subclassing Object could theoretically be in the list.

10.6
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
}

The main difficulty here is that we cannot create the underlying array with "new T[]". The array must be implemented as an array of Objects which introduces unchecked type conversions. Since these are all internal, we can be sure they will not fail and supress the warnings.

11.1
The state pattern suggests that the context class will frequently change its own state.
The strategy pattern suggests the strategy will be set once and probably from outside the context class.

11.2
a) A Lecturer can become a Professor but they are still the same Academic. In the first solution, a new Academic (Professor) would have to be constructed if this happened and it would have to replace the original Academic (Lecturer). Thus all references would have to point to the new Academic and all state would have to be copied over. This may be a difficult operation and the construction of a new Academic is counter intuitive. Therefore, the State pattern is useful because the same Academic can switch from Lecturer to Professor avoiding the issue.
b) The call to getRank() simply wishes to determine the rank of the Academic. However, returning the value of mRank (an AcademicRank instance) gives access to the functionality that state provides as well as its own internal state which the Academic might depend on. This should not be made accessible.
c) Decorator pattern
public class AcademicRank
{
	public abstract void doJob();
	public abstract int getPay();
}

public class Professor extends AcademicRank { ... }
public class Lecturer extends AcademicRank { ... }

public class ImmutableRank extends AcademicRank
{
	private AcademicRank m_rank;

	public ImmutableRank(AcademicRank r) { m_rank = r; }

	public void doJob() { throw new Exception(...); } // Hide this because it will modify internal state (e.g. currentLecture, class etc)

	public int getPay() { return m_rank.getPay(); } // Doesn't modify internal state => Allow access
}
public class Academic
{
	private AcademicRank mRank;
	public ImmutableRank getRank()
	{
		return new ImmutableRank(mRank);
	}
}

Problems:
- The type of the rank being returned is now hidden. Therefore the caller cannot use instanceof to determine the rank of the Academic. This requires AcademicRank to contain an abstract method that will return a rank id or a similar workaround.
- It is easy to define ImmutableRank incorrectly: either Professor or Lecturer may be modified so that a call to getPay() affects some internal state. When this happens, the implementation in ImmutableRank must be changed so the actual method is not called. This is likely to be forgotten.
11.3
a) Yes. There is no sensible default implementation and every subclass will define it.
b)
public void drawAll(List<Shape> shapes)
{
	for (Shape s : shapes)
		s.draw();
}

c)
public class Shape
{
	public abstract void draw();
}
public class Circle extends Shape { ... }
public class Rectangle extends Shape { ... }
public class CompositeShape
{
	List<Shape> m_components;
	public void draw()
	{
		for (Shape c : m_components)
			c.draw();
	}
}

d) Decorator pattern
public class FramedShape extends Shape
{
	private Shape m_shape;

	public FramedShape(Shape s) { m_shape = s; }
	public void draw()
	{
		m_shape.draw();

		// Draw frame surrounding shape
		...
	}
}

11.4
public class Component
{
	public abstract void doSomething();
}

class DecoratableComponent extends Component {}

public class Foo extends DecoratableComponent
{
	public void doSomething() {...};
}
public class Bar extends DecoratableComponent
{
	public void doSomething() {...};
}

public class ComponentDecorator<TComponent extends DecoratableComponent> extends Component
{
	TComponent m_component;

	public ComponentDecorator(TComponent c) { m_component = c; }
	public void doSomething()
	{
		m_component.doSomething();
		doSomethingElse();
	}
}

2015P1Q4
a) Source code is the raw human-readable code written by the programmer. Bytecode is an intermediate language which is not human-readable and that Java is compiled into. This is interpreted by the Java Virtual Machine. Machine code is the actual instructions stored in memory which will be executed by the machine. Unlike bytecode, machine code is platform specific.
Advantages:
- Smaller filesize
- Makes modification, reading of code difficult. (May be important if this is a product / Harder to find security vulnerabilities if this is relevant)

b) The algorithm itself has no state that needs to be kept between marking operations. Therefore markFaces(Image) does not depend on a specific instance and can be made static.

c)
public class FileImage extends Image
{
	private String filename;
	public String getFilename() { return filename; }
}

d)
public class LoggedImage extends Image
{
	private Image m_img;

	public LoggedImage(Image img) { m_img = img; }
	public int getWidth() { return m_img.getWidth(); }
	public int getHeight() { return m_img.getHeight(); }
	public void setPixel(int x, int y, byte val) { return m_img.setPixel(x, y, val); }

	public byte getPixel(int x, int y)
	{
		System.out.print("Pixel accessed at (");
		System.out.print(x);
		System.out.print(", ");
		System.out.print(y);
		System.out.println(")");
		return m_img.getPixel(x, y);
	}
}