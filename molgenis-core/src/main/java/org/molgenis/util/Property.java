/**
 * File: invengine.tdg.TDG <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2006-01-02; 1.0.0; RA Scheltema; Creation.
 * </ul>
 */

package org.molgenis.util;

import java.util.Observable;

import java.lang.IllegalAccessException;

/**
 * Implementation of a property. This class is a generic, which can be used for
 * adding properties to your class. It has the get and set methods, so there is
 * no need to specify them for each property, resulting in cleaner code.
 * Furthermore the class has some extended functionality with which the access
 * to the value of the property can be regulated and the class extends the
 * Observable class, so other classes cam listen in on a change of the value.
 * <p>
 * A property should be used in the following manner:
 * 
 * <pre>
 *    class Example
 *    {
 *       public final Property&lt;int&gt; property = new Property&lt;int&gt;(0, true);
 *    }
 *    
 *    Example example = new Example();
 *    try
 *    {
 *       example.property.set(1);
 *    }
 *    catch (IllegalAccessException e)
 *    {
 *       System.out.println(e.getMessage());
 *    }
 * </pre>
 */
public class Property<GaType> extends Observable
{
	// constructor(s)
	/**
	 * Default constructor which sets the property-value to the given value and
	 * the editable-flag to true.
	 * 
	 * @param value
	 *            The value for the property.
	 */
	public Property(GaType value)
	{
		this.value = value;
		this.editable = true;
	}

	/**
	 * Default constructor which sets the property-value to the given value and
	 * the editable-flag to given value.
	 * 
	 * @param value
	 *            The value for the property.
	 * @param editable
	 *            The editable-flag.
	 */
	public Property(GaType value, boolean editable)
	{
		this.value = value;
		this.editable = editable;
	}

	// access methods
	/**
	 * Sets the given value as the new value for the property. When the
	 * editable-flag is set to false, this method throws an exception. Otherwise
	 * the value is changed and all the registered observers are notied of the
	 * change.
	 * 
	 * @param value
	 *            The new value for the property
	 * @throws IllegalAccessException
	 *             Thrown when the editable-flag has been set to false.
	 */
	public synchronized void set(GaType value) throws IllegalAccessException
	{
		if (editable)
		{
			this.value = value;

			this.setChanged();
			this.notifyObservers();
		}
		else
		{
			throw new IllegalAccessException("Property is not set to editable.");
		}
	}

	/**
	 * Returns the value of the property.
	 * 
	 * @return The value of the property.
	 */
	public synchronized GaType get()
	{
		return this.value;
	}

	/**
	 * Returns true when the set-method of this property can be called and false
	 * otherwise.
	 * 
	 * @return True when the property can be set.
	 */
	public boolean isEditable()
	{
		return this.editable;
	}

	// member variables
	/** The actual value of this property */
	private GaType value;

	/** Indicates whether this property can be changed with the set-method */
	private boolean editable;
}
