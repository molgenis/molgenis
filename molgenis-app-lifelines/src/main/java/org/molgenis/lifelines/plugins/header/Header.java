package org.molgenis.lifelines.plugins.header;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.Entity;

public class Header extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	public Header(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return "<link rel=\"stylesheet\" style=\"text/css\" type=\"text/css\" href=\"css/lifelines.css\">\n";
	}

	@Override
	public String getViewName()
	{
		return "plugins_header_Header";
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + Header.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
	}

	@Override
	public void reload(Database db)
	{
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}
}
