package org.molgenis.security.user;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.auth.MolgenisUser;

public interface MolgenisUserService
{
	/**
	 * Returns e-mail addresses of super users
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	List<String> getSuEmailAddresses();

	MolgenisUser findById(Integer userId);

	void update(MolgenisUser user);

	/*
	 * Returns the currently logged in user
	 */
	MolgenisUser getCurrentUser();

	void checkPassword(String userName, String oldPwd, String newPwd1, String newPwd2);
}
