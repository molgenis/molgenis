package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;

/**
 * Listener that when added to a {@link Attribute} fires when an attribute property changes.
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
	 * @param attrName attribute of the the given attribute that changed
	 * @param attr     changed attribute
	 */
	void onChange(String attrName, Attribute attr);
}
