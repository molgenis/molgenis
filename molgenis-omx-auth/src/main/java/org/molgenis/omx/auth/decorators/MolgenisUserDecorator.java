package org.molgenis.omx.auth.decorators;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.molgenis.omx.auth.util.PasswordHasher;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.security.SimpleLogin;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisRoleGroupLink;
import org.molgenis.omx.auth.MolgenisUser;

public class MolgenisUserDecorator<E extends MolgenisUser> extends MapperDecorator<E>
{
	// JDBCMapper is the generate thing
	public MolgenisUserDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		// add your pre-processing here, e.g.
		for (org.molgenis.omx.auth.MolgenisUser e : entities)
		{
			this.hashPassword(e);
		}

		// here we call the standard 'add'
		int count = super.add(entities);

		// add your post-processing here
		// if you throw and exception the previous add will be rolled back
		// First check if we have the kind of Login that allows the following
		// actions!
		if (!(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			for (MolgenisUser e : entities)
			{
				// Try to add the user to the AllUsers group
				MolgenisGroup mg;
				try
				{
					mg = getDatabase().find(MolgenisGroup.class,
							new QueryRule(MolgenisGroup.NAME, Operator.EQUALS, "AllUsers")).get(0);
				}
				catch (Exception ex)
				{
					// When running from Hudson, there will be no group
					// "AllUsers" so we return without giving
					// an error, to keep our friend Hudson from breaking
					return count;
				}

				MolgenisRoleGroupLink mugl = new MolgenisRoleGroupLink();
				mugl.setRole_Id(e.getId());
				mugl.setGroup_Id(mg.getId());
				try
				{
					getDatabase().add(mugl);
				}
				catch (DatabaseException e1)
				{
					e1.printStackTrace();
				}
			}
		}

		return count;
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{

		// add your pre-processing here, e.g.
		for (org.molgenis.omx.auth.MolgenisUser e : entities)
		{
			this.hashPassword(e);
		}

		int count = -1;

		// make security exception if the currently logged in user wants to
		// update him/herself
		// FIXME: we do this.getDatabase().getLogin() == null to work with the
		// "escaped security" solutions in the SimpleUserLogin.java..
		// FIXME: not good, or OK ???
		if (this.getDatabase().getLogin() == null || entities.size() == 1
				&& this.getDatabase().getLogin().getUserName().equals(entities.get(0).getName()))
		{
			Login saveLogin = this.getDatabase().getLogin();
			this.getDatabase().setLogin(null);
			count = super.update(entities);
			this.getDatabase().setLogin(saveLogin);
		}
		else
		{
			// here we call the standard 'update'
			count = super.update(entities);
		}

		// add your post-processing here
		// if you throw and exception the previous add will be rolled back

		return count;
	}

	@Override
	public int remove(List<E> entities) throws DatabaseException
	{
		// add your pre-processing here
		// first remove corresponding MolgenisRoleGroupLink entries, so we will
		// be allowed to remove the User entities
		// First check if we have the kind of Login that allows the following
		// actions!
		if (!(this.getDatabase().getLogin() instanceof SimpleLogin))
		{
			for (E e : entities)
			{
				try
				{
					List<MolgenisRoleGroupLink> molgenisRoleGroupLink = getDatabase()
							.query(MolgenisRoleGroupLink.class).eq(MolgenisRoleGroupLink.ROLE_, e.getIdValue()).find();
					getDatabase().remove(molgenisRoleGroupLink);
				}
				catch (Exception e1)
				{
					// Apparently this user was in no groups
					e1.printStackTrace();
				}
			}
		}

		// here we call the standard 'remove'
		int count = super.remove(entities);

		// add your post-processing here

		return count;
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
			e.printStackTrace();
		}
	}
}
