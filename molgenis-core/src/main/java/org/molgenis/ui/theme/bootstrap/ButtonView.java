package org.molgenis.ui.theme.bootstrap;

import org.molgenis.ui.Button;
import org.molgenis.ui.theme.Theme;
import org.molgenis.ui.theme.TwoStepView;

public class ButtonView implements TwoStepView<Button>
{
	@Override
	public String render(Button model, Theme layout)
	{
		String css = "btn";
		
		//if(model.getIcon() != null) css += " icon-"+model.getIcon();
		if(model.getClazz() != null) css += " "+model.getClazz(); 
		return String.format("<button id=\"%s\" class=\"%s\" type=\"submit\">%s</button>", model.getId(), css, model.getLabel());
	}

}
