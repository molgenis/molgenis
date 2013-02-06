package org.molgenis.ui.theme.bootstrap;

import org.apache.log4j.Logger;
import org.molgenis.framework.ui.ApplicationController;
import org.molgenis.framework.ui.ScreenView;
import org.molgenis.framework.ui.html.HtmlInputException;
import org.molgenis.ui.Button;
import org.molgenis.ui.Form;
import org.molgenis.ui.MolgenisComponent;
import org.molgenis.ui.StringInput;
import org.molgenis.ui.theme.RenderException;
import org.molgenis.ui.theme.base.BaseTheme;

/**
 * Theme that renders the ui using bootstrap. If no suitable renderer is
 * available, the element will be rendered using the BaseTheme
 * 
 * NB: temporarily this implements ScreenView (to bridge old and new).
 */
public class BootstrapTheme extends BaseTheme implements ScreenView
{
	private static final Logger logger = Logger.getLogger(ApplicationController.class);

	private MolgenisComponent component;

	public BootstrapTheme(MolgenisComponent c)
	{
		this();
		this.component = c;
	}

	public BootstrapTheme()
	{
		super();

		renderers.put(Button.class.getName(), new ButtonView());
		renderers.put(Form.class.getName(), new FormView());
		renderers.put(StringInput.class.getName(), new StringInputView());
	}

	@Override
	public String render() throws HtmlInputException
	{
		try
		{
			return this.render(component);
		}
		catch (RenderException e)
		{
			logger.warn(e);
			return e.getMessage();
		}
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		// bootstrap included by default
		return null;
	}
}
