package org.molgenis.omx;

import java.util.List;
import java.util.Vector;

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
import org.molgenis.servlet.GuiService;
import org.molgenis.ui.DataExplorerPluginPlugin;
import org.molgenis.ui.DataSetViewerPluginPlugin;
import org.molgenis.ui.ProtocolViewerControllerPlugin;
import org.molgenis.ui.UploadWizardPlugin;
import org.molgenis.util.Entity;
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
		runtimeProperty.setValue("OMX");
		database.add(runtimeProperty);

		// set logo
		RuntimeProperty runtimePropertyLogo = new RuntimeProperty();
		runtimePropertyLogo.setIdentifier(RuntimeProperty.class.getSimpleName() + '_' + "app.href.logo");
		runtimePropertyLogo.setName("app.href.logo");
		runtimePropertyLogo.setValue("img/logo_default.png");
		database.add(runtimePropertyLogo);

		List<MolgenisGroup> listMolgenisGroup = database.find(MolgenisGroup.class, new QueryRule(MolgenisGroup.NAME,
				Operator.EQUALS, "AllUsers"));

		MolgenisGroup readUsersGroup = createGroup(database, "readUsers");
		MolgenisGroup readWriteUsersGroup = createGroup(database, "readWriteUsers");

		// Set write permissions that a user can edit own account
		createPermission(database, MolgenisUser.class, listMolgenisGroup.get(0), "write");
		setPermissionsForUserGroup(database, readUsersGroup, "read");
		setPermissionsForUserGroup(database, readWriteUsersGroup, "write");

		// Add UploadWizard for readWriteUsersGroup
		createPermission(database, UploadWizardPlugin.class, readWriteUsersGroup, "write");
		database.setLogin(login); // restore login
	}

	private void setPermissionsForUserGroup(Database database, MolgenisRole groupName, String permission)
			throws DatabaseException
	{
		// Set entity permissions
		Vector<org.molgenis.model.elements.Entity> entities = database.getMetaData().getEntities(false, false);
		for (org.molgenis.model.elements.Entity e : entities)
		{
			Class<? extends Entity> entityClass = database.getClassForName(e.getName());
			createPermission(database, entityClass, groupName, permission);
		}

		// Set plugin permissions
		createPermission(database, DataSetViewerPluginPlugin.class, groupName, "read");
		createPermission(database, DataExplorerPluginPlugin.class, groupName, "read");
		createPermission(database, ProtocolViewerControllerPlugin.class, groupName, "read");

	}

}