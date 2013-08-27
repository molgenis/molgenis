package org.molgenis.omx.auth.ui;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class AccountPlugin extends IframePlugin
{
	private static final long serialVersionUID = -3084964114182861171L;
	
	public AccountPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		StringBuilder s = new StringBuilder();
		s.append("<script type=\"text/javascript\" src=\"js/jquery.autogrowinput.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.bt.min.js\"></script>");
		s.append("<script type=\"text/javascript\" src=\"js/jquery.validate.min.js\"></script>");
		return s.toString();
	}

	@Override
	public String getIframeSrc()
	{
		return "/account";
	}

	@Override
	public boolean isVisible()
	{
		return getLogin().isAuthenticated();
	}
}
