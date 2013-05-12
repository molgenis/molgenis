/* Date:        February 2, 2010
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.2-testing
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.lifelines.plugins.home;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisPermission;
import org.molgenis.omx.auth.MolgenisRole;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.core.MolgenisEntity;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.Entity;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import app.FillMetadata;
import app.ui.ProtocolViewerPlugin;
import app.ui.SimpleUserLoginPlugin;

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

		List<MolgenisUser> listUsers;
		try
		{
			listUsers = db.find(MolgenisUser.class, new QueryRule(MolgenisUser.NAME, Operator.EQUALS, "admin"));

			if (listUsers.isEmpty())
			{
				fillMetaData(db);
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

	private void fillMetaData(Database db) throws Exception
	{
		FillMetadata.fillMetadata(db, false, SimpleUserLoginPlugin.class.getSimpleName());

		List<MolgenisUser> users = db.find(MolgenisUser.class, new QueryRule(MolgenisUser.NAME, Operator.EQUALS,
				Login.USER_ANONYMOUS_NAME));
		if (users != null && !users.isEmpty())
		{
			MolgenisUser userAnonymous = users.get(0);
			List<MolgenisGroup> molgenisGroups = db.find(MolgenisGroup.class, new QueryRule(MolgenisGroup.NAME,
					Operator.EQUALS, Login.GROUP_USERS_NAME));
			if (molgenisGroups == null || molgenisGroups.isEmpty()) throw new DatabaseException(
					"missing required MolgenisGroup with name '" + Login.GROUP_USERS_NAME + "'");
			MolgenisGroup allUsersGroup = molgenisGroups.get(0);

			List<MolgenisRole> molgenisRoles = new ArrayList<MolgenisRole>();
			molgenisRoles.add(userAnonymous);
			molgenisRoles.add(allUsersGroup);

			List<Class<?>> visibleClasses = new ArrayList<Class<?>>();
			visibleClasses.add(ProtocolViewerPlugin.class);
			// add entity dependencies for protocol viewer plugin
			visibleClasses.add(DataSet.class);
			visibleClasses.add(Protocol.class);
			visibleClasses.add(ObservationSet.class);
			visibleClasses.add(ObservableFeature.class);
			visibleClasses.add(Category.class);
			visibleClasses.add(ObservedValue.class);

			for (Class<?> entityClass : visibleClasses)
			{
				MolgenisEntity molgenisEntity = db.find(MolgenisEntity.class,
						new QueryRule(MolgenisEntity.CLASSNAME, Operator.EQUALS, entityClass.getName())).get(0);

				for (MolgenisRole molgenisRole : molgenisRoles)
				{
					MolgenisPermission entityPermission = new MolgenisPermission();
					entityPermission.setName(entityClass.getSimpleName() + '_' + molgenisRole.getName());
					entityPermission.setIdentifier(UUID.randomUUID().toString());
					entityPermission.setRole(molgenisRole);
					entityPermission.setEntity(molgenisEntity);
					entityPermission.setPermission("read");
					db.add(entityPermission);
				}
			}
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
