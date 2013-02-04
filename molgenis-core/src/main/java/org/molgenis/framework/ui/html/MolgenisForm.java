package org.molgenis.framework.ui.html;

import java.text.ParseException;

import org.molgenis.framework.ui.FreemarkerView;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.util.tuple.Tuple;

public class MolgenisForm extends AbstractHtmlElement
{
	private ScreenModel model = null;
	private Layout layout = new FlowLayout();

	public MolgenisForm(ScreenModel model)
	{
		this(model, new FlowLayout());
	}

	public MolgenisForm(ScreenModel model, Layout layout)
	{
		this.model = model;
		this.layout = layout;
	}

	@Override
	public String render()
	{
		// use freemarker macros to render form header and footer
		FreemarkerView view = new FreemarkerView(MolgenisForm.class.getPackage().getName().replace(".", "/")
				+ "/MolgenisForm.ftl", getModel());
		view.addParameter("content", layout.render());
		String result = view.render();
		return result;
	}

	public ScreenModel getModel()
	{
		return model;
	}

	public void setModel(ScreenModel model)
	{
		this.model = model;
	}

	public void add(HtmlElement element)
	{
		this.layout.add(element);
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return null;
	}

	@Override
	public String render(Tuple params) throws ParseException, HtmlInputException
	{
		throw new UnsupportedOperationException("not implemented");
	}
}
