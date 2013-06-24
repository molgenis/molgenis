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

public class NewsPlugin extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_KEY_APP_NEWS = "<p>Paste here some news !</p>";
	private static final String KEY_APP_NEWS = "app.news";

	private NewsModel newsModel;

	public NewsModel getMyModel()
	{
		return newsModel;
	}

	public NewsPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return NewsPlugin.class.getSimpleName();
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + NewsPlugin.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{

	}

	@Override
	public void reload(Database db)
	{
		newsModel = new NewsModel();
		newsModel.setNews(getMolgenisSetting(KEY_APP_NEWS, DEFAULT_KEY_APP_NEWS));
	}

	@Override
	public boolean isVisible()
	{
		return true;
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

	public static class NewsModel implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String news;

		public String getNews()
		{
			return news;
		}

		public void setNews(String news)
		{
			this.news = news;
		}
	}
}
