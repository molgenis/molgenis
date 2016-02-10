package org.molgenis.data;

/**
 * Listener that when added to a {@link AttributeMetaData} fires when an attribute property changes.
 */
public interface AttributeChangeListener
{
	/**
	 * Gets the listener id
	 * 
	 * @return listener id
	 */
	String getId();

	/**
	 * Callback that fires when an attribute property changed
	 * 
	 * @param attrName
	 *            attribute of the the given attribute that changed
	 * @param attr
	 *            changed attribute
	 */
	void onChange(String attrName, AttributeMetaData attr);
}
