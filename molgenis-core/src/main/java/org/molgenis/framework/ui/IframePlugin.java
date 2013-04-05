package org.molgenis.framework.ui;

import org.molgenis.framework.db.Database;
import org.molgenis.util.Entity;

/**
 * Plugin with a view that contains an iframe.
 * 
 * The iframe content is defined in subclasses by implementing getIframeSrc.
 * 
 * @author erwin
 * 
 */
public abstract class IframePlugin extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	public IframePlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	public abstract String getIframeSrc();

	@Override
	public String getViewTemplate()
	{
		return IframePlugin.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public String getViewName()
	{
		return IframePlugin.class.getSimpleName();
	}

	@Override
	public void reload(Database db)
	{

	}

}
