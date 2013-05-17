package org.molgenis.lifelines;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisPermission;
import org.molgenis.omx.auth.MolgenisRole;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.core.MolgenisEntity;
import org.molgenis.omx.filter.StudyDataRequest;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservationSet;
import org.molgenis.omx.observ.ObservedValue;

import app.MolgenisDatabasePopulator;

import com.sun.mail.iap.Protocol;

public class WebAppDatabasePopulator extends MolgenisDatabasePopulator
{
	@Override
	protected void initializeApplicationDatabase(Database database) throws Exception
	{
		Login login = database.getLogin();
		database.setLogin(null);
		login.login(database, Login.USER_ADMIN_NAME, "admin"); // FIXME hardcoded reference to admin password

		List<MolgenisUser> users = database.find(MolgenisUser.class, new QueryRule(MolgenisUser.NAME, Operator.EQUALS,
				Login.USER_ANONYMOUS_NAME));
		if (users != null && !users.isEmpty())
		{
			MolgenisUser userAnonymous = users.get(0);
			List<MolgenisGroup> molgenisGroups = database.find(MolgenisGroup.class, new QueryRule(MolgenisGroup.NAME,
					Operator.EQUALS, Login.GROUP_USERS_NAME));
			if (molgenisGroups == null || molgenisGroups.isEmpty()) throw new DatabaseException(
					"missing required MolgenisGroup with name '" + Login.GROUP_USERS_NAME + "'");
			MolgenisGroup allUsersGroup = molgenisGroups.get(0);

			List<MolgenisRole> molgenisRoles = new ArrayList<MolgenisRole>();
			molgenisRoles.add(userAnonymous);
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
				MolgenisEntity molgenisEntity = database.find(MolgenisEntity.class,
						new QueryRule(MolgenisEntity.NAME, Operator.EQUALS, entityClass.getSimpleName())).get(0);

				for (MolgenisRole molgenisRole : molgenisRoles)
				{
					MolgenisPermission entityPermission = new MolgenisPermission();
					entityPermission.setName(entityClass.getSimpleName() + '_' + molgenisRole.getName());
					entityPermission.setIdentifier(UUID.randomUUID().toString());
					entityPermission.setRole(molgenisRole);
					entityPermission.setEntity(molgenisEntity);
					entityPermission.setPermission("read");
					database.add(entityPermission);
				}
			}

			Class<?> studyDataRequestClass = StudyDataRequest.class;
			MolgenisEntity studyDataRequestEntity = database.find(MolgenisEntity.class,
					new QueryRule(MolgenisEntity.CLASSNAME, Operator.EQUALS, studyDataRequestClass.getName())).get(0);

			MolgenisPermission entityPermission = new MolgenisPermission();
			entityPermission.setName(studyDataRequestClass.getSimpleName() + '_' + allUsersGroup.getName());
			entityPermission.setIdentifier(UUID.randomUUID().toString());
			entityPermission.setRole(allUsersGroup);
			entityPermission.setEntity(studyDataRequestEntity);
			entityPermission.setPermission("own");
			database.add(entityPermission);
		}

		database.setLogin(login); // restore login
	}
}