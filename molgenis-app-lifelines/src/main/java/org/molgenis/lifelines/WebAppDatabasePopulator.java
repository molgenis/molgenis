package org.molgenis.lifelines;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.molgenis.MolgenisDatabasePopulator;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisRole;
import org.molgenis.omx.auth.MolgenisRoleGroupLink;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.service.AccountService;
import org.molgenis.omx.core.RuntimeProperty;
import org.molgenis.omx.filter.StudyDataRequest;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.Ontology;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.servlet.GuiService;
import org.molgenis.ui.CatalogueLoaderPluginPlugin;
import org.molgenis.ui.DataSetViewerPluginPlugin;
import org.molgenis.ui.StudyDefinitionLoaderPluginPlugin;
import org.springframework.beans.factory.annotation.Value;

public class WebAppDatabasePopulator extends MolgenisDatabasePopulator
{
	@Value("${lifelines.profile:@null}")
	private String appProfile;
	@Value("${admin.password:@null}")
	private String adminPassword;
	@Value("${lifelines.datamanager.password:@null}")
	private String dataManagerPassword;
	@Value("${lifelines.datamanager.email:molgenis+datamanager@gmail.com}")
	private String dataManagerEmail;
	@Value("${lifelines.researcher.password:@null}")
	private String researcherPassword;
	@Value("${lifelines.researcher.email:molgenis+researcher@gmail.com}")
	private String researcherEmail;

	@Override
	protected void initializeApplicationDatabase(Database database) throws Exception
	{
		if (appProfile == null || dataManagerPassword == null || researcherPassword == null || adminPassword == null)
		{
			StringBuilder message = new StringBuilder("please configure: ");
			if (appProfile == null) message.append("lifelines.profile(possible values: workspace or website), ");
			if (dataManagerPassword == null) message.append("default lifelines.datamanager.password, ");
			if (researcherPassword == null) message.append("default lifelines.researcher.password ");
			if (adminPassword == null) message.append("default admin.password ");
			message.append("in your molgenis-server.properties.");
			throw new RuntimeException(message.toString());
		}

		Login login = database.getLogin();
		database.setLogin(null);
		login.login(database, Login.USER_ADMIN_NAME, adminPassword);

		// set app name
		RuntimeProperty runtimeProperty = new RuntimeProperty();
		runtimeProperty.setIdentifier(RuntimeProperty.class.getSimpleName() + '_' + GuiService.KEY_APP_NAME);
		runtimeProperty.setName(GuiService.KEY_APP_NAME);
		runtimeProperty.setValue("LifeLines");
		database.add(runtimeProperty);

		RuntimeProperty runtimePropertyAuthentication = new RuntimeProperty();
		runtimePropertyAuthentication.setIdentifier(RuntimeProperty.class.getSimpleName() + '_'
				+ AccountService.KEY_PLUGIN_AUTH_ACTIVATIONMODE);
		runtimePropertyAuthentication.setName(AccountService.KEY_PLUGIN_AUTH_ACTIVATIONMODE);
		runtimePropertyAuthentication.setValue("user");
		database.add(runtimePropertyAuthentication);

		MolgenisUser userResearcher = createUser(database, "researcher", "researcher", "researcher", researcherEmail,
				researcherPassword, false);
		MolgenisUser userDataManager = createUser(database, "datamanager", "datamanager", "datamanager",
				dataManagerEmail, dataManagerPassword, false);

		MolgenisGroup groupDataManagers = createGroup(database, "dataManagers");
		MolgenisGroup groupResearchers = createGroup(database, "researchers");
		
		MolgenisRoleGroupLink linkDatamanager = new MolgenisRoleGroupLink();
		linkDatamanager.setGroup(groupDataManagers);
		linkDatamanager.setRole(userDataManager);	
		linkDatamanager.setIdentifier(UUID.randomUUID().toString());
		linkDatamanager.setName(UUID.randomUUID().toString());
		database.add(linkDatamanager);
		
		MolgenisRoleGroupLink linkResearcher = new MolgenisRoleGroupLink();
		linkResearcher.setGroup(groupResearchers);
		linkResearcher.setRole(userResearcher);	
		linkResearcher.setIdentifier(UUID.randomUUID().toString());
		linkResearcher.setName(UUID.randomUUID().toString());
		database.add(linkResearcher);
		
		MolgenisGroup allUsersGroup = null;
		List<MolgenisUser> users = database.find(MolgenisUser.class, new QueryRule(MolgenisUser.NAME, Operator.EQUALS,
				Login.USER_ANONYMOUS_NAME));
		if (users != null && !users.isEmpty())
		{
			MolgenisUser userAnonymous = users.get(0);
			List<MolgenisGroup> molgenisGroups = database.find(MolgenisGroup.class, new QueryRule(MolgenisGroup.NAME,
					Operator.EQUALS, Login.GROUP_USERS_NAME));
			if (molgenisGroups == null || molgenisGroups.isEmpty()) throw new DatabaseException(
					"missing required MolgenisGroup with name '" + Login.GROUP_USERS_NAME + "'");
			allUsersGroup = molgenisGroups.get(0);

			List<MolgenisRole> molgenisRoles = new ArrayList<MolgenisRole>();
			molgenisRoles.add(userAnonymous);
			molgenisRoles.add(userResearcher);
			molgenisRoles.add(allUsersGroup);

			List<Class<?>> visibleClasses = new ArrayList<Class<?>>();
			// add entity dependencies for protocol viewer plugin
			visibleClasses.add(DataSet.class);
			visibleClasses.add(Protocol.class);
			visibleClasses.add(ObservationSet.class);
			visibleClasses.add(ObservableFeature.class);
			visibleClasses.add(Category.class);
			visibleClasses.add(ObservedValue.class);

			for (Class<?> entityClass : visibleClasses)
			{
				for (MolgenisRole molgenisRole : molgenisRoles)
				{
					createPermission(database, entityClass, molgenisRole, "read");
				}
			}
			createPermission(database, StudyDataRequest.class, allUsersGroup, "own");
		}

		createPermission(database, Characteristic.class, groupDataManagers, "write");
		createPermission(database, OntologyTerm.class, groupDataManagers, "write");
		createPermission(database, Ontology.class, groupDataManagers, "write");
		createPermission(database, DataSet.class, groupDataManagers, "write");
		createPermission(database, Protocol.class, groupDataManagers, "write");
		createPermission(database, ObservationSet.class, groupDataManagers, "write");
		createPermission(database, ObservableFeature.class, groupDataManagers, "write");
		createPermission(database, Category.class, groupDataManagers, "write");
		createPermission(database, ObservedValue.class, groupDataManagers, "write");
		createPermission(database, MolgenisUser.class, groupDataManagers, "write");
		createPermission(database, MolgenisUser.class, groupResearchers, "write");
		createPermission(database, MolgenisUser.class, allUsersGroup, "write");

		if ("website".equals(appProfile))
		{
			createPermission(database, CatalogueLoaderPluginPlugin.class, userDataManager, "read");
		}
		else if ("workspace".equals(appProfile))
		{
			createPermission(database, DataSetViewerPluginPlugin.class, groupDataManagers, "read");
			createPermission(database, DataSetViewerPluginPlugin.class, groupResearchers, "read");
			createPermission(database, StudyDefinitionLoaderPluginPlugin.class, groupDataManagers, "read");
		}
		else
		{
			throw new RuntimeException(
					"please configure app.profile in your molgenis-server.properties. possible values: workspace or website");
		}

		database.setLogin(login); // restore login
	}

}