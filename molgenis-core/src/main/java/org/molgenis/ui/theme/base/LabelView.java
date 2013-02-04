package org.molgenis.ui.theme.base;

import org.molgenis.ui.Label;
import org.molgenis.ui.MolgenisComponent;
import org.molgenis.ui.theme.RenderException;
import org.molgenis.ui.theme.Theme;
import org.molgenis.ui.theme.TwoStepView;

public class LabelView implements TwoStepView<Label>
{
	MolgenisComponent labelFor;
	
	@Override
	public String render(Label element, Theme renderer) throws RenderException
	{
		String attributes = element.getClazz() != null ? " class=\""+element.getClazz()+"\"" : "";
		attributes += labelFor != null ? "\" for=\""+labelFor.getId()+"\"" : "";

		return String.format("<label%s>%s</label>", attributes, element.getValue());
	}
}
