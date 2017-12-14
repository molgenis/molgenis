package org.molgenis.i18n;

/**
 * If you want localization messages for a namespace to be picked up by the populator,
 * you need to create a {@link PropertiesMessageSource} bean for that namespace and add it to the context.
 */
public class PropertiesMessageSource
{
	private final String namespace;

	public PropertiesMessageSource(String namespace)
	{
		this.namespace = namespace.trim().toLowerCase();
	}

	/**
	 * Returns the namespace of this {@link PropertiesMessageSource}
	 */
	public String getNamespace()
	{
		return namespace;
	}
}
