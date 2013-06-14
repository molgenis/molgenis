package org.molgenis.compute.db;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute.runtime.ComputeParameterValue;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute.runtime.ComputeTaskHistory;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.security.Login;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisPermission;
import org.molgenis.omx.auth.MolgenisRole;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.util.PasswordHasher;
import org.molgenis.omx.core.MolgenisEntity;
import org.springframework.beans.factory.annotation.Value;

import org.molgenis.MolgenisDatabasePopulator;
import org.molgenis.ui.ComputeBackendsFormController;
import org.molgenis.ui.ComputeRunsFormController;
import org.molgenis.ui.ComputeTaskHistoryFormController;
import org.molgenis.ui.ComputeTaskValuesFormController;
import org.molgenis.ui.ComputeTasksFormController;
import org.molgenis.ui.PilotDashboardPluginPlugin;

public class WebAppDatabasePopulator extends MolgenisDatabasePopulator
{
	private static final Logger logger = Logger.getLogger(WebAppDatabasePopulator.class);

	@Value("${api.user.name:api}")
	private String apiUserName; // specify in molgenis-server.properties

	@Value("${api.user.password:api}")
	private String apiUserPassword; // specify in molgenis-server.properties

	@Override
	protected void initializeApplicationDatabase(Database database) throws Exception
	{
		Login login = database.getLogin();
		database.setLogin(null); // so we don't run into trouble with the
									// Security Decorators

		database.beginTx();
		try
		{
			// add api user
			MolgenisUser userApi = new MolgenisUser();
			userApi.setName(apiUserName);
			userApi.setIdentifier(UUID.randomUUID().toString());
			userApi.setPassword(new PasswordHasher().toMD5(apiUserPassword));
			userApi.setEmail("molgenis@gmail.com");
			userApi.setFirstName("api");
			userApi.setLastName("api");
			userApi.setActive(true);
			database.add(userApi);
			logger.info("Added api user");

			MolgenisPermission runPermission = new MolgenisPermission();
			runPermission.setEntity(MolgenisEntity.findByClassName(database, ComputeRun.class.getName()));
			runPermission.setName("ApiUser_ComputeRun_Read_Permission");
			runPermission.setIdentifier(UUID.randomUUID().toString());
			runPermission.setPermission("read");
			runPermission.setRole(userApi);
			database.add(runPermission);

			MolgenisPermission taskPermission = new MolgenisPermission();
			taskPermission.setEntity(MolgenisEntity.findByClassName(database, ComputeTask.class.getName()));
			taskPermission.setName("ApiUser_ComputeTask_Read_Permission");
			taskPermission.setIdentifier(UUID.randomUUID().toString());
			taskPermission.setPermission("read");
			taskPermission.setRole(userApi);
			database.add(taskPermission);

			MolgenisPermission taskWritePermission = new MolgenisPermission();
			taskWritePermission.setEntity(MolgenisEntity.findByClassName(database, ComputeTask.class.getName()));
			taskWritePermission.setName("ApiUser_ComputeTask_Write_Permission");
			taskWritePermission.setIdentifier(UUID.randomUUID().toString());
			taskWritePermission.setPermission("write");
			taskWritePermission.setRole(userApi);
			database.add(taskWritePermission);
			logger.info("Added api user permissions");

			// Create compute user group
			MolgenisGroup userGroup = new MolgenisGroup();
			userGroup.setIdentifier(UUID.randomUUID().toString());
			userGroup.setName("ComputeUser");
			database.add(userGroup);
			logger.info("Added compute user group");

			// Add permissions to user group
			MolgenisPermission runOwnPermission = new MolgenisPermission();
			runOwnPermission.setEntity(MolgenisEntity.findByClassName(database, ComputeRun.class.getName()));
			runOwnPermission.setName("ComputeUser_ComputeTask_Own_Permission");
			runOwnPermission.setIdentifier(UUID.randomUUID().toString());
			runOwnPermission.setPermission("own");
			runOwnPermission.setRole(userGroup);
			database.add(runOwnPermission);

			MolgenisPermission taskOwnPermission = new MolgenisPermission();
			taskOwnPermission.setEntity(MolgenisEntity.findByClassName(database, ComputeTask.class.getName()));
			taskOwnPermission.setName("ComputeUser_ComputeTask_Own_Permission");
			taskOwnPermission.setIdentifier(UUID.randomUUID().toString());
			taskOwnPermission.setPermission("own");
			taskOwnPermission.setRole(userGroup);
			database.add(taskOwnPermission);

			MolgenisPermission taskHistoryOwnPermission = new MolgenisPermission();
			taskHistoryOwnPermission.setEntity(MolgenisEntity.findByClassName(database,
					ComputeTaskHistory.class.getName()));
			taskHistoryOwnPermission.setName("ComputeUser_ComputeTaskHistory_Own_Permission");
			taskHistoryOwnPermission.setIdentifier(UUID.randomUUID().toString());
			taskHistoryOwnPermission.setPermission("own");
			taskHistoryOwnPermission.setRole(userGroup);
			database.add(taskHistoryOwnPermission);

			MolgenisPermission parameterValueOwnPermission = new MolgenisPermission();
			parameterValueOwnPermission.setEntity(MolgenisEntity.findByClassName(database,
					ComputeParameterValue.class.getName()));
			parameterValueOwnPermission.setName("ComputeUser_ComputeParameterValue_Own_Permission");
			parameterValueOwnPermission.setIdentifier(UUID.randomUUID().toString());
			parameterValueOwnPermission.setPermission("own");
			parameterValueOwnPermission.setRole(userGroup);
			database.add(parameterValueOwnPermission);

			MolgenisPermission computeBackendOwnPermission = new MolgenisPermission();
			computeBackendOwnPermission.setEntity(MolgenisEntity.findByClassName(database,
					ComputeBackend.class.getName()));
			computeBackendOwnPermission.setName("ComputeUser_ComputeBackend_Own_Permission");
			computeBackendOwnPermission.setIdentifier(UUID.randomUUID().toString());
			computeBackendOwnPermission.setPermission("own");
			computeBackendOwnPermission.setRole(userGroup);
			database.add(computeBackendOwnPermission);

			// UI
			createReadPermission(database, PilotDashboardPluginPlugin.class, userGroup);
			createReadPermission(database, ComputeRunsFormController.class, userGroup);
			createReadPermission(database, ComputeTasksFormController.class, userGroup);
			createReadPermission(database, ComputeTaskValuesFormController.class, userGroup);
			createReadPermission(database, ComputeTaskHistoryFormController.class, userGroup);
			createReadPermission(database, ComputeBackendsFormController.class, userGroup);
			logger.info("Added compute user group permissions");

			insertBackends(database);
			logger.info("Added backends");

			database.commitTx();
		}
		catch (Exception e)
		{
			database.rollbackTx();
			throw e;
		}

		database.setLogin(login);
	}

	private void createReadPermission(Database database, Class<?> clazz, MolgenisRole role) throws DatabaseException
	{
		MolgenisPermission permission = new MolgenisPermission();
		permission.setEntity(MolgenisEntity.findByClassName(database, clazz.getName()));
		permission.setName(role.getName() + "_" + clazz.getSimpleName() + "_Permission");
		permission.setIdentifier(UUID.randomUUID().toString());
		permission.setPermission("read");
		permission.setRole(role);
		database.add(permission);
	}

	private void insertBackends(Database database) throws DatabaseException
	{
		ComputeBackend localhost = new ComputeBackend();
		localhost.setName("localhost");
		localhost.setBackendUrl("localhost");
		localhost.setHostType("LOCALHOST");
		localhost.setCommand("sh src/main/shell/local/maverick.sh");
		database.add(localhost);

		ComputeBackend grid = new ComputeBackend();
		grid.setName("ui.grid.sara.nl");
		grid.setBackendUrl("ui.grid.sara.nl");
		grid.setHostType("GRID");
		grid.setCommand("glite-wms-job-submit  -d $USER -o pilot-one $HOME/maverick/maverick${pilotid}.jdl");
		database.add(grid);
	}
}