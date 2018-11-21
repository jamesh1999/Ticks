1.1
Functional programming is an example of declarative programming. The program will specify what the computer should do but not how to do it. It is also typical to minimise the amount of mutable state in functional programming.
In an imperative programming, the program also specifies how to perform an action. Typically, an imperative programming language does not discourage the use of mutable state or procedures.
FP - Encourage recursion
IP - Encourage iteration

1.2
double, int, float are primitive TYPES
d (double), f (float) are all primitives
i (int[]) is a reference
LinkedList, Double, Tree, Computer are all classes
l (LinkedList<Double>), k (Double) are both references to objects
t (Tree), c (Computer) are both references that do not point to objects

1.3
public class TailRecursionTest
{
	private void recurse (int times)
	{
		if (times == 0) return;	
		int memory[] = new int[1000000]; // Allocate a megabyte
		recurse(times - 1);
	}

	public static void main (String[] args)
	{
		recurse(500);
	}
}

To test, run the above program and monitor its memory usage. 
~500MB => No tail recursion optimisation
Less => Tail recursion optimisation

1.4
public class LowestCommon
{
	public static int lowestCommon (long a, long b)
	{
		for (int i = 0; i < 64; ++i)
		{
			if (a & 0x01 == b & 0x01) return i; // Bits are same
			a >>= 1;
			b >>= 1;
		}
		return -1;
	}

	// Using a break statement?
	public static int lowestCommon (long a, long b)
	{
		int i = 0;
		for (; i < 64; ++i)
		{
			if (a & 0x01 == b & 0x01) break;
			a >>= 1;
			b >>= 1;
		}

		return i == 64 ? -1 : i;
	}
}

1.5
public class MatrixUtils
{
	public static float[][] construct (int n)
	{
		return new float[n][n];
	}

	public static float[][] transposeSquare (float[][] mat)
	{
		for (int i = 0; i < mat.length; ++i)
		{
			if (mat[i].length != mat.length)
				throw new IllegalArgumentException("Matrix must be square");

			for (int j = i + 1; j < mat[i].length; ++j)
			{
				float buffer = mat[i][j];
				mat[i][j] = mat[j][i];
				mat[j][i] = buffer;
			}
		}
	}
}

2.1
By using private state w/ public getters/setters, we provide an interface that will not change even if the internal representation of the state changes. This reduces coupling since it is easier to modify the internal representation without making changes elsewhere in the program.
It also allows for sanitisation when setting the state: take for example ArrayLife in the prearrival course. The setCell method was able to check whether the cell being set was within the boundaries of the board.

2.2
Advantages:
- It still allows accessing private state: This can be permissable when two classes are supposed to be closely coupled (e.g. friends in C++)
- It makes a clear distinction of what is public vs private inside the class: By sticking to the naming convention, it is clear from the variable names within the class what is public/private. The variable declaration does not need to be searched for

Disadvantages:
- It is possible for someone using the class to completely ignore the public interface and access "private" state directly thus leading to high coupling where it should be avoided

2.3a
public class Vector2D
{
	private float x;
	private float y;

	public Vector2D (float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public void add (Vector2D v)
	{
		x += v.x;
		y += v.y;
	}

	public float dot (Vector2D v)
	{
		return x * v.x + y * v.y;
	}

	public float mag ()
	{
		return Math.sqrt(x*x + y*y);
	}

	public void norm ()
	{
		float magnitude = mag();
		x /= magnitude;
		y /= magnitude;
	}
}

2.3b
To make it immutable, none of the methods should be able to modify the internal state. Add() and Norm() should both return newly constructed Vector2D objects.

Label final?

2.3c
public void add(Vector2D v)
i) This makes the most sense for a mutable Vector2D since it updates the value of the object to reflect that of the two Vector2Ds added together.
Could add current vector to v? 
ii) This is unsuitable for an immutable Vector2D since the object cannot be modified (it is immutable) and the method does not return a newly constructed Vector2D

public Vector2D add(Vector2D v)
i) It does not make sense to use this for a mutable Vector2D since it returns a new Vector2D. (It could conceivably return "this" but that seems a bit pointless.
ii) For an immutable Vector2D this prototype would make sense: It adds another vector to the vector this method is called on and returns a new vector containing the result

public Vector2D add (Vector2D v1, Vector2D v2)
i) As before this does not make sense for a mutable Vector2D
ii) This does not make sense for an immutable Vector2D either since both the required vectors are passed as arguments. Therefore, there is no need for this function to depend on an instance and it should be made static

public static Vector2D add (Vector2D v1, Vector2D v2)
i) As before this does not make sense for a mutable Vector2D
ii) This is the preferred method for an immutable Vector2D. The method no longer depends on an instance of Vector2D because it is static.

2.3d
- Class naming & Documentation
- In the interface provided: Do not provide access to internal state, all methods return newly constructed instances

2.4a
public class OOPLinkedList
{
	private class OOPLinkedListElement
	{
		public OOPLinkedListElement next;
		public int value;

		public OOPLinkedListElement (int val, OOPLinkedListElement next)
		{
			value = val;
			this.next = next;
		}
	}
	private OOPLinkedListElement head;

	public int head()
	{
		if (head == null) throw new Exception("Index out of range");
		return head.value;
	}

	public int index(int idx)
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

	public void insert (int val)
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

2.5a
public class BinaryTreeNode
{
	private int mValue;
	private BinaryTreeNode mLeft;
	private BinaryTreeNode mRight;

	public BinaryTreeNode(int val)
	{
		mValue = val;
	}

	public int getValue()
	{
		return mValue;
	}

	public void setValue(int val)
	{
		mValue = val;
	}

	public BinaryTreeNode getLeft()
	{
		return mLeft;
	}

	public BinaryTreeNode getRight()
	{
		return mRight;
	}

	public void setLeft(BinaryTreeNode left)
	{
		mLeft = left;
	}

	public void setRight(BinaryTreeNode right)
	{
		mRight = right;
	}
}

public class SearchSet
{
	private int mElements;
	private BinaryTreeNode mHead;

	public void insert(int val)
	{
		// Insert at head
		if (mHead == null)
		{
			mHead = new BinaryTreeNode(val);
			return;
		}

		BinaryTreeNode cur = mHead;
		while (cur.getValue() != val)
		{
			if (cur.getValue() > val)
			{
				// Add new node
				if (cur.getLeft() == null)
				{
					cur.setLeft(new BinaryTreeNode(val));
					++mElements;
				}
				cur = cur.getLeft();
			}
			else
			{
				// Add new node
				if (cur.getRight() == null)
				{
					cur.setRight(new BinaryTreeNode(val));
					++mElements;
				}
				cur = cur.getRight();
			}
		}
	}

	public int getNumberElements()
	{
		return mElements;
	}

	public boolean contains(int val)
	{
		BinaryTreeNode cur = mHead;
		while (cur != null)
		{
			if (cur.getValue() == val) return true;

			if (cur.getValue() > val) cur = cur.getLeft();
			else cur = cur.getRight();
		}
		return false;
	}
}

3.5
Convenience passing references to functions?
