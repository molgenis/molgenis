package org.molgenis.util;

public class TemplateResourceUtils
{
	/**
	 * Check if given resource exists
	 *
	 * @param resourceName resource name ( including path from this Class)
	 * @return true is resource exists otherwise false
	 */
	public boolean resourceExists(String resourceName)
	{
		return this.getClass().getResource(resourceName) != null;
	}
}
