package org.molgenis.ui.theme.bootstrap;

import org.molgenis.ui.StringInput;
import org.molgenis.ui.theme.RenderException;
import org.molgenis.ui.theme.Theme;
import org.molgenis.ui.theme.TwoStepView;

public class StringInputView implements TwoStepView<StringInput>
{

	@Override
	public String render(StringInput e, Theme renderer) throws RenderException
	{
		String result = "<input class=\"input-small\" type=\"text\" id=\""+e.getId()+"\">";
		
		return result;
	}

}
