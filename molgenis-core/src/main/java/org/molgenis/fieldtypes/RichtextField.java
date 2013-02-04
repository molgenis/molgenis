package org.molgenis.fieldtypes;

import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.framework.ui.html.HtmlInputException;
import org.molgenis.framework.ui.html.RichtextInput;

public class RichtextField extends TextField
{
	private static final long serialVersionUID = 1L;

	@Override
	public HtmlInput<String> createInput(String name, String xrefEntityClassName) throws HtmlInputException
	{
		return new RichtextInput(name);
	}
}
