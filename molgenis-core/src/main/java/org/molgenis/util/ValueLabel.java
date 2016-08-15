package org.molgenis.util;

import java.io.Serializable;

/**
 * Specific pair to link String label to Object.
 */
public class ValueLabel implements Serializable
{
	/* The serial version UID of this class. Needed for serialization. */
	private static final long serialVersionUID = -6539081630192819896L;
	/**
	 * The label
	 */
	private String label;
	/**
	 * The value
	 */
	private Object value;

	/**
	 * Construct a new ValueLabel
	 */
	public ValueLabel()
	{

	}

	/**
	 * Construct a new ValueLabel
	 *
	 * @param value the value
	 * @param label the label
	 */
	public ValueLabel(Object value, String label)
	{
		this.label = label;
		this.value = value;
	}

	/**
	 * Retrieve the label
	 *
	 * @return label
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * Set the label
	 *
	 * @param label new label
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * Retrieve the value
	 *
	 * @return value
	 */
	public Object getValue()
	{
		return value;
	}

	/**
	 * Set the value
	 *
	 * @param value
	 */
	public void setValue(Object value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return "value=" + value + " label=" + label;
	}

}
