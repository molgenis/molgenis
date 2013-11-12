package org.molgenis.security.user;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountServiceImpl implements UserAccountService
{
	@Autowired
	private Database unsecuredDatabase;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_USERACCOUNT')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public MolgenisUser getCurrentUser() throws DatabaseException
	{
		return MolgenisUser.findByUsername(unsecuredDatabase, SecurityUtils.getCurrentUsername());
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_WRITE_USERACCOUNT')")
	@Transactional(rollbackFor = DatabaseException.class)
	public void updateCurrentUser(MolgenisUser updatedCurrentUser) throws DatabaseException
	{
		String currentUsername = SecurityUtils.getCurrentUsername();
		if (!currentUsername.equals(updatedCurrentUser.getUsername()))
		{
			throw new RuntimeException("Updated user differs from the current user");
		}

		MolgenisUser currentUser = getCurrentUser();
		if (currentUser == null)
		{
			throw new RuntimeException("User does not exist [" + currentUsername + "]");
		}
		String password = currentUser.getPassword();
		String updatedPassword = updatedCurrentUser.getPassword();
		if (StringUtils.isNotEmpty(updatedPassword) && !password.equals(updatedPassword))
		{
			// encode updated password
			String encodedPassword = passwordEncoder.encode(updatedPassword);
			updatedCurrentUser.setPassword(encodedPassword);
		}
		unsecuredDatabase.update(updatedCurrentUser);
	}
}
