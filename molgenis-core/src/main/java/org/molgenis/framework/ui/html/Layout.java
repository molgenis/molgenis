package org.molgenis.framework.ui.html;

import java.util.List;

/**
 * A layout is a container that renders its elements in a particular layout.
 */
public interface Layout extends HtmlElement
{
	/** Add an element to the layout */
	void add(HtmlElement element);

	/** Add a set of elements to the layout */
	void setElements(List<HtmlElement> elements);
}
