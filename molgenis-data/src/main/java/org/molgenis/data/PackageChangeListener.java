package org.molgenis.data;

/**
 * Listener that when added to a {@link Package} fires when a package property changes.
 */
public interface PackageChangeListener
{
	/**
	 * Gets the listener id
	 * 
	 * @return listener id
	 */
	String getId();

	/**
	 * Callback that fires when a package property changed
	 * 
	 * @param package_
	 *            updated package
	 */
	void onChange(Package package_);
}
