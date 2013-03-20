/* Date:        February 2, 2010
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.2-testing
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.lifelines.plugins.home;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.Entity;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import app.FillMetadata;

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
		return "Home";
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/org/molgenis/lifelines/plugins/home/Home.ftl";
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

		List<MolgenisUser> listUsers;
		try
		{
			listUsers = db.find(MolgenisUser.class, new QueryRule(MolgenisUser.NAME, Operator.EQUALS, "admin"));

			System.out.println("listUsers: " + listUsers.size());
			if (listUsers.isEmpty())
			{

				FillMetadata.fillMetadata(db, false, "SimpleUserLoginPlugin");
				this.setMessages(new ScreenMessage("User setup complete!", true));
			}

		}
		catch (DatabaseException e)
		{
			this.setMessages(new ScreenMessage("Database error: " + e.getMessage(), false));
			e.printStackTrace();
		}
		catch (Exception e)
		{
			this.setMessages(new ScreenMessage("error: " + e.getMessage(), false));
			e.printStackTrace();
		}

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

	public static class HomeModel
	{
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
