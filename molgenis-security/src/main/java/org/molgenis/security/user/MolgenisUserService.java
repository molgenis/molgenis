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
	List<String> getSuEmailAddresses() throws DatabaseException;

	MolgenisUser findById(Integer userId) throws DatabaseException;

	void update(MolgenisUser user) throws DatabaseException;

	/**
	 * Returns the currently logged in user
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	MolgenisUser getCurrentUser() throws DatabaseException;

	void checkPassword(String userName, String oldPwd, String newPwd1, String newPwd2) throws DatabaseException;
}
