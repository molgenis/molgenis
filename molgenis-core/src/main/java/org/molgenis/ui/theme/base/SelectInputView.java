package org.molgenis.ui.theme.base;

import org.molgenis.ui.SelectInput;
import org.molgenis.ui.theme.RenderException;
import org.molgenis.ui.theme.Theme;
import org.molgenis.ui.theme.TwoStepView;
import org.molgenis.util.ValueLabel;

public class SelectInputView implements TwoStepView<SelectInput>
{

	@Override
	public String render(SelectInput element, Theme renderer) throws RenderException
	{
		StringBuilder optionsBuilder = new StringBuilder();
		for (ValueLabel vl : element.getOptions())
		{
			String selected = vl.getValue().equals(element.getValue()) ? " selected" : "";
			optionsBuilder.append(String.format("<option %svalue=\"%s\">%s</option>", selected, vl.getValue(),
					vl.getLabel()));
		}

		return String.format("<select id=\"%s\">%s</select>", element.getId(), optionsBuilder.toString());
	}

}
