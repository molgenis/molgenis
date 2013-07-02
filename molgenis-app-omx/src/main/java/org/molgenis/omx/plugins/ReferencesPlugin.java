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

public class ReferencesPlugin extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_KEY_APP_CONTACT = "<p>Paste the code</p>";
	private static final String KEY_APP_CONTACT = "app.references";

	private ReferencesModel referencesModel;

	public ReferencesModel getMyModel()
	{
		return referencesModel;
	}

	public ReferencesPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return ReferencesPlugin.class.getSimpleName();
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + ReferencesPlugin.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{

	}

	@Override
	public void reload(Database db)
	{
		referencesModel = new ReferencesModel();
		referencesModel.setReferences(getMolgenisSetting(KEY_APP_CONTACT, DEFAULT_KEY_APP_CONTACT));
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

	public static class ReferencesModel implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String references;

		public String getReferences()
		{
			return references;
		}

		public void setReferences(String references)
		{
			this.references = references;
		}
	}
}
