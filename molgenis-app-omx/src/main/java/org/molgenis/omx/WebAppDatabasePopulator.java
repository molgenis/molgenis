package org.molgenis.omx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.molgenis.MolgenisDatabasePopulator;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisRole;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.OmxPermissionService;
import org.molgenis.omx.controller.ContactController;
import org.molgenis.omx.controller.DataSetsIndexerController;
import org.molgenis.omx.controller.HomeController;
import org.molgenis.omx.controller.ReferencesController;
import org.molgenis.omx.core.RuntimeProperty;
import org.molgenis.omx.importer.ImportWizardController;
import org.molgenis.ui.MolgenisMenuController.VoidPluginController;
import org.molgenis.util.Entity;
import org.springframework.beans.factory.annotation.Value;

//import org.molgenis.ui.ProtocolViewerControllerPlugin;

public class WebAppDatabasePopulator extends MolgenisDatabasePopulator
{

	@Value("${admin.password:@null}")
	private String adminPassword;

	@Override
	protected void initializeApplicationDatabase(Database database) throws Exception
	{
		Map<String, String> runtimePropertyMap = new HashMap<String, String>();
		runtimePropertyMap.put("app.href.logo", "/img/logo_molgenis_letterbox.png");
		runtimePropertyMap.put("app.home.html", "Welcome to Molgenis!");
		runtimePropertyMap.put("app.background", "There is no background information");
		runtimePropertyMap.put("app.news", "There is no news ");
		runtimePropertyMap.put("app.href.css", "");
		runtimePropertyMap.put("app.references", "There are no references");
		runtimePropertyMap.put("app.contact", "There is no contact information");
		runtimePropertyMap.put("app.name", "Molgenis");

		Login login = database.getLogin();
		database.setLogin(null);
		login.login(database, Login.USER_ADMIN_NAME, adminPassword);

		MolgenisPermissionService permissionService = new OmxPermissionService(database, login);

		for (Entry<String, String> entry : runtimePropertyMap.entrySet())
		{
			RuntimeProperty runtimeProperty = new RuntimeProperty();
			String app = entry.getKey();
			runtimeProperty.setIdentifier(RuntimeProperty.class.getSimpleName() + '_' + app);
			runtimeProperty.setName(app);
			runtimeProperty.setValue(entry.getValue());
			database.add(runtimeProperty);
		}

		List<MolgenisGroup> listMolgenisGroup = database.find(MolgenisGroup.class, new QueryRule(MolgenisGroup.NAME,
				Operator.EQUALS, "AllUsers"));

		MolgenisGroup readUsersGroup = createGroup(database, "readUsers");
		MolgenisGroup readWriteUsersGroup = createGroup(database, "readWriteUsers");

		// Allow anonymous user to see Home plugin
		MolgenisUser anonymousUser = MolgenisUser.findByName(database, Login.USER_ANONYMOUS_NAME);
		permissionService.setPermissionOnPlugin(VoidPluginController.class, anonymousUser.getId(), Permission.READ);
		permissionService.setPermissionOnPlugin(HomeController.class, anonymousUser.getId(), Permission.READ);

		// Set write permissions that a user can edit own account
		permissionService.setPermissionOnEntity(MolgenisUser.class, anonymousUser.getId(), Permission.READ);
		permissionService.setPermissionOnEntity(MolgenisUser.class, listMolgenisGroup.get(0).getId(), Permission.WRITE);

		setPermissionsForUserGroup(permissionService, database, readUsersGroup, Permission.READ);
		setPermissionsForUserGroup(permissionService, database, readWriteUsersGroup, Permission.WRITE);

		// Add UploadWizard for readWriteUsersGroup
		permissionService.setPermissionOnPlugin(ImportWizardController.class, readWriteUsersGroup.getId(),
				Permission.WRITE);

		database.setLogin(login); // restore login
	}

	private void setPermissionsForUserGroup(MolgenisPermissionService permissionService, Database database,
			MolgenisRole groupName, Permission permission) throws DatabaseException
	{
		// Set entity permissions
		Vector<org.molgenis.model.elements.Entity> entities = database.getMetaData().getEntities(false, false);
		for (org.molgenis.model.elements.Entity e : entities)
		{
			Class<? extends Entity> entityClass = database.getClassForName(e.getName());
			permissionService.setPermissionOnEntity(entityClass, groupName.getId(), permission);

		}

		// Set plugin permissions
		permissionService.setPermissionOnPlugin(VoidPluginController.class, groupName.getId(), Permission.READ);
		permissionService.setPermissionOnPlugin(HomeController.class, groupName.getId(), Permission.READ);
		permissionService.setPermissionOnPlugin(ContactController.class, groupName.getId(), Permission.READ);
		permissionService.setPermissionOnPlugin(ReferencesController.class, groupName.getId(), Permission.READ);
		permissionService.setPermissionOnPlugin(DataExplorerController.class, groupName.getId(), Permission.READ);
		permissionService.setPermissionOnPlugin(DataSetsIndexerController.class, groupName.getId(), Permission.READ);
	}
}