package org.molgenis.framework.ui;

import org.molgenis.framework.ui.html.HtmlInputException;

/**
 * A ScreenView contains the <i>layout</i> of a part of the user-interface.
 */
public interface ScreenView
{
	/**
	 * This methods produces an html representation of the view
	 * 
	 * @throws HtmlInputException
	 */
	public String render() throws HtmlInputException;

	/** Produces any custom html headers needed, e.g. to load css or javascript */
	public String getCustomHtmlHeaders();
}
