package org.molgenis.ui.theme.bootstrap;

import org.molgenis.ui.Form;
import org.molgenis.ui.HtmlInput;
import org.molgenis.ui.MolgenisComponent;
import org.molgenis.ui.theme.RenderException;
import org.molgenis.ui.theme.Theme;
import org.molgenis.ui.theme.TwoStepView;

public class FormView implements TwoStepView<Form>
{

	@Override
	public String render(Form element, Theme theme) throws RenderException
	{
		String legend = element.getLegend() != null ? String.format("<legend>%s</legend>", element.getLegend()) : "";

		switch (element.getType())
		{
			case VERTICAL:
				return String.format("<form class=\"well\">%s%s</form>", legend, renderDefault(element, theme));
			case INLINE:
				return String.format("<form class=\"well form-inline\">%s%s</form>", legend,
						renderDefault(element, theme));
			case HORIZONTAL:
				return String.format("<form class=\"well form-horizontal\">%s<fieldset>%s</fieldset></form>", legend,
						renderHorizontal(element, theme));
			default:
				return "FORMTYPE " + element.getType().name() + " not supported";
		}
	}

	private String renderDefault(Form element, Theme theme) throws RenderException
	{
		StringBuilder strBuilder = new StringBuilder();
		for (MolgenisComponent<?> c : element.getComponents())
		{
			strBuilder.append(theme.render(c));
		}
		return strBuilder.toString();
	}

	private String renderHorizontal(Form element, Theme theme) throws RenderException
	{
		StringBuilder strBuilder = new StringBuilder();

		// each label triggers a new control group
		for (MolgenisComponent<?> c : element.getComponents())
		{
			if (c instanceof HtmlInput)
			{
				HtmlInput<?, ?> i = (HtmlInput<?, ?>) c;

				// remove label, we will render it differently then default
				String label = i.getLabel();
				i.label(null);

				strBuilder
						.append(String
								.format("<div class=\"control-group\"><label class=\"control-label\" for=\"%s\">%s</label><div class=\"controls\">%s</div></div>",
										c.getId(), label, theme.render(c)));

				// restore
				i.label(label);
			}
			else
			{
				strBuilder.append(theme.render(c));
			}
		}

		return strBuilder.toString();
	}
}
