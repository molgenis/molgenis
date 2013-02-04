package org.molgenis.fieldtypes;

import org.molgenis.framework.ui.html.FreemarkerInput;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.framework.ui.html.HtmlInputException;

public class FreemarkerField extends TextField
{
	private static final long serialVersionUID = 1L;

	@Override
	public HtmlInput<String> createInput(String name, String xrefEntityClassName) throws HtmlInputException
	{
		return new FreemarkerInput(name);
	}

}
