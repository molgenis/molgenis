package org.molgenis.omx;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisDatabasePopulator;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisRole;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.core.RuntimeProperty;
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
import org.molgenis.ui.DataExplorerPluginPlugin;
import org.molgenis.ui.DataSetViewerPluginPlugin;
import org.molgenis.ui.ProtocolViewerControllerPlugin;
import org.molgenis.ui.UploadWizardPlugin;
import org.springframework.beans.factory.annotation.Value;

public class WebAppDatabasePopulator extends MolgenisDatabasePopulator
{

	@Value("${admin.password:@null}")
	private String adminPassword;

	@Override
	protected void initializeApplicationDatabase(Database database) throws Exception
	{

		Login login = database.getLogin();
		database.setLogin(null);
		login.login(database, Login.USER_ADMIN_NAME, adminPassword);

		// set app name
		RuntimeProperty runtimeProperty = new RuntimeProperty();
		runtimeProperty.setIdentifier(RuntimeProperty.class.getSimpleName() + '_' + GuiService.KEY_APP_NAME);
		runtimeProperty.setName(GuiService.KEY_APP_NAME);
		runtimeProperty.setValue("LifeLines");
		database.add(runtimeProperty);

		// set logo
		RuntimeProperty runtimePropertyLogo = new RuntimeProperty();
		runtimePropertyLogo.setIdentifier(RuntimeProperty.class.getSimpleName() + '_' + GuiService.KEY_APP_NAME
				+ ".href.logo");
		runtimePropertyLogo.setName(GuiService.KEY_APP_NAME + ".href.logo");
		runtimePropertyLogo.setValue("img/logo_default");
		database.add(runtimePropertyLogo);

		List<MolgenisGroup> listMolgenisGroup = database.find(MolgenisGroup.class, new QueryRule(MolgenisGroup.NAME,
				Operator.EQUALS, "AllUsers"));

		createPermission(database, MolgenisUser.class, listMolgenisGroup.get(0), "write");

		MolgenisGroup readUsersGroup = new MolgenisGroup();
		createGroup(database, "readUsers");

		MolgenisGroup readWriteUsersGroup = new MolgenisGroup();
		createGroup(database, "readWriteUsers");

		List<MolgenisRole> molgenisRoles = new ArrayList<MolgenisRole>();

		molgenisRoles.add(readUsersGroup);

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

		permissionGroup(database, readUsersGroup, "read");
		permissionGroup(database, readWriteUsersGroup, "write");

		database.setLogin(login); // restore login
	}

	private void permissionGroup(Database database, MolgenisRole groupName, String permission) throws DatabaseException
	{
		// Set entity permissions
		createPermission(database, Characteristic.class, groupName, permission);
		createPermission(database, OntologyTerm.class, groupName, permission);
		createPermission(database, Ontology.class, groupName, permission);
		createPermission(database, DataSet.class, groupName, permission);
		createPermission(database, Protocol.class, groupName, permission);
		createPermission(database, ObservationSet.class, groupName, permission);
		createPermission(database, ObservableFeature.class, groupName, permission);
		createPermission(database, Category.class, groupName, permission);
		createPermission(database, ObservedValue.class, groupName, permission);

		// Set plugin permissions
		createPermission(database, DataSetViewerPluginPlugin.class, groupName, "read");
		createPermission(database, DataExplorerPluginPlugin.class, groupName, "read");
		createPermission(database, ProtocolViewerControllerPlugin.class, groupName, "read");
		createPermission(database, UploadWizardPlugin.class, groupName, "read");
	}

}