package org.molgenis.framework.ui.html;

/**
 * Superclass for non-interactive elements.
 */
public abstract class HtmlWidget extends HtmlInput<String>
{

	public HtmlWidget(String name, String label)
	{
		super(name, label, null);
	}

	public HtmlWidget(String name)
	{
		super(name, name);
	}

}
