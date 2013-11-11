package org.molgenis.security.user;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.auth.MolgenisUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountServiceImpl implements UserAccountService
{
	@Autowired
	private MolgenisUserService molgenisUserService;

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_USERACCOUNT')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public MolgenisUser getCurrentUser() throws DatabaseException
	{
		// TODO do not expose password
		MolgenisUser currentUser = molgenisUserService.getCurrentUser();
		return currentUser;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_WRITE_USERACCOUNT')")
	@Transactional(rollbackFor = DatabaseException.class)
	public void updateCurrentUser(MolgenisUser molgenisUser) throws DatabaseException
	{
		MolgenisUser currentUser = molgenisUserService.getCurrentUser();
		if (!currentUser.getUsername().equals(molgenisUser.getUsername()))
		{
			throw new RuntimeException("Updated user differs from the current user");
		}
		if (molgenisUser.getPassword() == null)
		{
			molgenisUser.setPassword(currentUser.getPassword());
		}
		molgenisUserService.update(molgenisUser);
	}
}
