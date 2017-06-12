package org.molgenis.util;

/**
 * Generic pair to link two objects together. A Pair holds one object of type T
 * and one object of type T2. The pair is the owner of these objects. The basic
 * use of this type is to link two object belonging together.
 *
 * @param <T>  The type of the first element
 * @param <T2> The type of the second element
 */
public class Pair<T, T2>
{
	// constructor(s)

	/**
	 * Standard constructor, which initializes the two values to null.
	 */
	public Pair()
	{
	}

	/**
	 * Specific constructor, which initializes the two values to the two values
	 * passed as parameters. After this call the pair is owner of the two
	 * objects.
	 *
	 * @param a Pointer to the first object.
	 * @param b Pointer to the second object.
	 */
	public Pair(T a, T2 b)
	{
		this.a = a;
		this.b = b;
	}

	// access methods

	/**
	 * Returns the pointer to the first object.
	 *
	 * @return Pointer to the first object.
	 */
	public T getA()
	{
		return this.a;
	}

	/**
	 * Sets the first object
	 *
	 * @param a New pointer for the first object
	 */
	public void setA(T a)
	{
		this.a = a;
	}

	/**
	 * Returns the pointer to the second object.
	 *
	 * @return Pointer to the second object.
	 */
	public T2 getB()
	{
		return this.b;
	}

	/**
	 * Sets the second object
	 *
	 * @param b New pointer for the second object
	 */
	public void setB(T2 b)
	{
		this.b = b;
	}

	// Object overloads
	@Override
	public String toString()
	{
		return "Pair(" + a.toString() + ", " + b.toString() + ")";
	}

	// member variables
	/**
	 * Pointer to the first object
	 */
	private T a = null;
	/**
	 * Pointer to the second object
	 */
	private T2 b = null;

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Pair)
		{
			@SuppressWarnings("unchecked")
			Pair<T, T2> that = ((Pair<T, T2>) o);

			return (this.a.equals(that.a) && this.b.equals(that.b));
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return a.hashCode() + b.hashCode();
	}

}
