package org.molgenis.omx.auth.decorators;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisRoleGroupLink;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.util.PasswordHasher;

public class MolgenisUserDecorator<E extends MolgenisUser> extends MapperDecorator<E>
{
	public MolgenisUserDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		// pre-processing
		for (E molgenisUser : entities)
			this.hashPassword(molgenisUser);

		int count = super.add(entities);

		// post-processing
		for (E molgenisUser : entities)
		{
			List<MolgenisGroup> molgenisGroups = getDatabase().find(MolgenisGroup.class,
					new QueryRule(MolgenisGroup.NAME, Operator.EQUALS, Login.GROUP_USERS_NAME));
			if (molgenisGroups == null || molgenisGroups.isEmpty()) throw new DatabaseException(
					"missing required user group '" + Login.GROUP_USERS_NAME + "'");
			MolgenisGroup userGroup = molgenisGroups.get(0);

			MolgenisRoleGroupLink molgenisRoleGroupLink = new MolgenisRoleGroupLink();
			molgenisRoleGroupLink.setName(userGroup.getName() + '-' + molgenisUser.getName());
			molgenisRoleGroupLink.setIdentifier(UUID.randomUUID().toString());
			molgenisRoleGroupLink.setRole(molgenisUser);
			molgenisRoleGroupLink.setGroup(userGroup);
			getDatabase().add(molgenisRoleGroupLink);

		}

		return count;
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{
		// pre-processing
		for (E molgenisUser : entities)
			this.hashPassword(molgenisUser);

		return super.update(entities);
	}

	@Override
	public int remove(List<E> entities) throws DatabaseException
	{
		// pre-processing: remove MolgenisRoleGroupLinks referencing users
		List<MolgenisRoleGroupLink> molgenisGroups = getDatabase().find(MolgenisRoleGroupLink.class,
				new QueryRule(MolgenisRoleGroupLink.ROLE_, Operator.IN, entities));
		if (molgenisGroups != null && !molgenisGroups.isEmpty()) getDatabase().remove(molgenisGroups);

		return super.remove(entities);
	}

	private void hashPassword(org.molgenis.omx.auth.MolgenisUser user)
	{
		// if password already encrypted, nothing changed -> return
		if (StringUtils.startsWith(user.getPassword(), "md5_")) return;

		try
		{
			PasswordHasher md5 = new PasswordHasher();
			String newPassword = md5.toMD5(user.getPassword());
			user.setPassword(newPassword);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}
}
