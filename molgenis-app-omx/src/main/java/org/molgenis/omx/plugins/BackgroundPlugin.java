package org.molgenis.omx.plugins;

import java.io.Serializable;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.Entity;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class BackgroundPlugin extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_KEY_APP_BACKGROUND = "<p>Paste the code</p>";
	private static final String KEY_APP_BACKGROUND = "app.background";

	private BackgroundModel backgroundModel;

	public BackgroundModel getMyModel()
	{
		return backgroundModel;
	}

	public BackgroundPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return BackgroundPlugin.class.getSimpleName();
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + BackgroundPlugin.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{

	}

	@Override
	public void reload(Database db)
	{
		backgroundModel = new BackgroundModel();
		backgroundModel.setBackground(getMolgenisSetting(KEY_APP_BACKGROUND, DEFAULT_KEY_APP_BACKGROUND));
	}

	private String getMolgenisSetting(String key, String defaultValue)
	{
		try
		{
			MolgenisSettings molgenisSettings = ApplicationContextProvider.getApplicationContext().getBean(
					MolgenisSettings.class);
			return molgenisSettings.getProperty(key, defaultValue);
		}
		catch (NoSuchBeanDefinitionException e)
		{
			logger.warn(e);
			return defaultValue;
		}
	}

	public static class BackgroundModel implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String background;

		public String getBackground()
		{
			return background;
		}

		public void setBackground(String background)
		{
			this.background = background;
		}
	}
}
