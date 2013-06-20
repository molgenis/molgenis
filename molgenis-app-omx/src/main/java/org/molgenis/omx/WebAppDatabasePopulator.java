package org.molgenis.omx;

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
import org.molgenis.omx.observ.value.BoolValue;
import org.molgenis.omx.observ.value.CategoricalValue;
import org.molgenis.omx.observ.value.DateTimeValue;
import org.molgenis.omx.observ.value.DateValue;
import org.molgenis.omx.observ.value.DecimalValue;
import org.molgenis.omx.observ.value.EmailValue;
import org.molgenis.omx.observ.value.HtmlValue;
import org.molgenis.omx.observ.value.IntValue;
import org.molgenis.omx.observ.value.LongValue;
import org.molgenis.omx.observ.value.MrefValue;
import org.molgenis.omx.observ.value.StringValue;
import org.molgenis.omx.observ.value.TextValue;
import org.molgenis.omx.observ.value.XrefValue;
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

		MolgenisGroup readUsersGroup = createGroup(database, "readUsers");

		MolgenisGroup readWriteUsersGroup = createGroup(database, "readWriteUsers");

		createPermission(database, MolgenisUser.class, listMolgenisGroup.get(0), "read");
		permissionGroup(database, readUsersGroup, "read");
		permissionGroup(database, readWriteUsersGroup, "write");
		createPermission(database, UploadWizardPlugin.class, readWriteUsersGroup, "write");
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
		createPermission(database, StringValue.class, groupName, permission);
		createPermission(database, BoolValue.class, groupName, permission);
		createPermission(database, CategoricalValue.class, groupName, permission);
		createPermission(database, DateTimeValue.class, groupName, permission);
		createPermission(database, DateValue.class, groupName, permission);
		createPermission(database, DecimalValue.class, groupName, permission);
		createPermission(database, EmailValue.class, groupName, permission);
		createPermission(database, HtmlValue.class, groupName, permission);
		createPermission(database, IntValue.class, groupName, permission);
		createPermission(database, LongValue.class, groupName, permission);
		createPermission(database, MrefValue.class, groupName, permission);
		createPermission(database, TextValue.class, groupName, permission);
		createPermission(database, XrefValue.class, groupName, permission);

		// Set plugin permissions
		createPermission(database, DataSetViewerPluginPlugin.class, groupName, "read");
		createPermission(database, DataExplorerPluginPlugin.class, groupName, "read");
		createPermission(database, ProtocolViewerControllerPlugin.class, groupName, "read");

	}

}