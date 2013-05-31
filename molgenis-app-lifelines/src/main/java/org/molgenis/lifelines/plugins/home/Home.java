/* Date:        February 2, 2010
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.2-testing
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.lifelines.plugins.home;

import java.io.Serializable;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.Entity;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Shows table of experiment information for WormQTL
 */
public class Home extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_KEY_APP_HOME_HTML = "<p>Welcome to LifeLines!</p>";
	private static final String KEY_APP_HOME_HTML = "app.home.html";

	private HomeModel homeModel;

	public HomeModel getMyModel()
	{
		return homeModel;
	}

	public Home(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return Home.class.getSimpleName();
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + Home.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{

	}

	@Override
	public void reload(Database db)
	{
		homeModel = new HomeModel();
		homeModel.setHomeHtml(getMolgenisSetting(KEY_APP_HOME_HTML, DEFAULT_KEY_APP_HOME_HTML));
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

	public static class HomeModel implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String homeHtml;

		public String getHomeHtml()
		{
			return homeHtml;
		}

		public void setHomeHtml(String homeHtml)
		{
			this.homeHtml = homeHtml;
		}
	}
}
