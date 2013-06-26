package org.molgenis.lifelines;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisDatabasePopulator;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.lifelines.plugins.HomePlugin;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisRole;
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
import org.molgenis.omx.plugins.ProtocolViewerController;
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

		RuntimeProperty runtimePropertyViewButton = new RuntimeProperty();
		runtimePropertyViewButton.setIdentifier(RuntimeProperty.class.getSimpleName() + '_'
				+ ProtocolViewerController.KEY_ACTION_VIEW);
		runtimePropertyViewButton.setName(ProtocolViewerController.KEY_ACTION_VIEW);
		runtimePropertyViewButton.setValue("false");
		database.add(runtimePropertyViewButton);

		String homeHtml = "<div class=\"container-fluid\">"
				+ "<div class=\"row-fluid\">"
				+ "<div class=\"span6\">"
				+ "<h3>Welcome at the LifeLines Data Catalogue!</h3>"
				+ "<p>The LifeLines Data Catalogue provides an overview of all the data collected in LifeLines.</p>"
				+ "<p>When you click 'catalogue' you can browse all available data items from questionnaires,  measurements and (blood and urine) sample analysis. Also, you can make a selection of data  items that you will need for your research, and download the list.</p>"
				+ "<p>If you want to save your selection and apply for LifeLines data, you need to  register first. You can register by clicking the 'login' button on top. After you  have registered, you will receive a confirmation email. Subsequently, you are able  to download your selection or submit the selection together with you proposal.</p>"
				+ "<p>The catalogue will regularly be updated with new collected data items.  For questions regarding the catalogue or submission of your proposal, please contact the  LifeLines Research Office  <a href=\"mailto:LLscience@umcg.nl\">LLscience@umcg.nl</a></p>"
				+ "<p>The catalogue is working in the newest browsers. <u>If you are experiencing any problems  please switch to a modern browser (IE9+, Chrome, Firefox, Safari).</u></p>"
				+ "</div>" + "<div class=\"span6\">"
				+ "<img src=\"/img/lifelines_family.png\" alt=\"LifeLines family\">" + "</div>" + "</div>" + "</div>";

		RuntimeProperty runtimePropertyHomeHtml = new RuntimeProperty();
		runtimePropertyHomeHtml.setIdentifier(RuntimeProperty.class.getSimpleName() + '_'
				+ HomePlugin.KEY_APP_HOME_HTML);
		runtimePropertyHomeHtml.setName(HomePlugin.KEY_APP_HOME_HTML);
		runtimePropertyHomeHtml.setValue(homeHtml);
		database.add(runtimePropertyHomeHtml);

		MolgenisUser userResearcher = createUser(database, "researcher", "researcher", "researcher", researcherEmail,
				researcherPassword, false);
		MolgenisUser userDataManager = createUser(database, "datamanager", "datamanager", "datamanager",
				dataManagerEmail, dataManagerPassword, false);

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

		createPermission(database, Characteristic.class, userDataManager, "write");
		createPermission(database, OntologyTerm.class, userDataManager, "write");
		createPermission(database, Ontology.class, userDataManager, "write");
		createPermission(database, DataSet.class, userDataManager, "write");
		createPermission(database, Protocol.class, userDataManager, "write");
		createPermission(database, ObservationSet.class, userDataManager, "write");
		createPermission(database, ObservableFeature.class, userDataManager, "write");
		createPermission(database, Category.class, userDataManager, "write");
		createPermission(database, ObservedValue.class, userDataManager, "write");
		createPermission(database, MolgenisUser.class, userDataManager, "write");
		createPermission(database, MolgenisUser.class, userResearcher, "write");
		createPermission(database, MolgenisUser.class, allUsersGroup, "write");

		if ("website".equals(appProfile))
		{
			createPermission(database, CatalogueLoaderPluginPlugin.class, userDataManager, "read");
		}
		else if ("workspace".equals(appProfile))
		{
			createPermission(database, DataSetViewerPluginPlugin.class, userDataManager, "read");
			createPermission(database, DataSetViewerPluginPlugin.class, userResearcher, "read");
			createPermission(database, StudyDefinitionLoaderPluginPlugin.class, userDataManager, "read");
		}
		else
		{
			throw new RuntimeException(
					"please configure app.profile in your molgenis-server.properties. possible values: workspace or website");
		}

		database.setLogin(login); // restore login
	}

}