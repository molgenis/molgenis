package org.molgenis.lifelines.plugins.header;

import java.io.Serializable;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.Entity;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class Header extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_KEY_APP_HREF_LOGO = "img/lifelines_letterbox_270x100.png";
	private static final String DEFAULT_KEY_APP_HREF_CSS = "css/lifelines.css";
	private static final String KEY_APP_HREF_LOGO = "app.href.logo";
	private static final String KEY_APP_HREF_CSS = "app.href.css";

	private HeaderModel headerModel;

	public Header(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	public HeaderModel getMyModel()
	{
		return headerModel;
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		String customCss = getMolgenisSetting(KEY_APP_HREF_CSS, DEFAULT_KEY_APP_HREF_CSS);
		return "<link rel=\"stylesheet\" style=\"text/css\" type=\"text/css\" href=\"" + customCss + "\">\n";
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
	public void handleRequest(Database db, MolgenisRequest request) throws DatabaseException
	{
	}

	@Override
	public void reload(Database db)
	{
		this.headerModel = new HeaderModel();
		headerModel.setHrefLogo(getMolgenisSetting(KEY_APP_HREF_LOGO, DEFAULT_KEY_APP_HREF_LOGO));
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

	public static class HeaderModel implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String hrefLogo;

		public String getHrefLogo()
		{
			return hrefLogo;
		}

		public void setHrefLogo(String hrefLogo)
		{
			this.hrefLogo = hrefLogo;
		}
	}
}
