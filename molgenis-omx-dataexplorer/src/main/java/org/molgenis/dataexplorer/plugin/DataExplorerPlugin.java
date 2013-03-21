package org.molgenis.dataexplorer.plugin;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.Entity;

public class DataExplorerPlugin extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	public DataExplorerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + DataExplorerPlugin.class.getSimpleName().replace('.', '/') + ".ftl";
	}

	@Override
	public String getViewName()
	{
		return DataExplorerPlugin.class.getSimpleName();
	}

	@Override
	public void reload(Database db)
	{
	}

}
