package org.molgenis.omx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

		Map<String, String> runtimePropertyMap = new HashMap<String, String>();
		runtimePropertyMap.put(GuiService.KEY_APP_NAME, "OMX");
		runtimePropertyMap.put("app.href.logo", "img/logo_molgenis_letterbox.png");
		runtimePropertyMap.put("app.home.html", "Welcome to Molgenis!");
		runtimePropertyMap.put("app.background", "There is no background information");
		runtimePropertyMap.put("app.news", "There is no news ");
		runtimePropertyMap.put("app.href.css", "");

		Login login = database.getLogin();
		database.setLogin(null);
		login.login(database, Login.USER_ADMIN_NAME, adminPassword);

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
		createPermission(database, DataExplorerPluginPlugin.class, groupName, "read");
		createPermission(database, ProtocolViewerControllerPlugin.class, groupName, "read");

	}

}