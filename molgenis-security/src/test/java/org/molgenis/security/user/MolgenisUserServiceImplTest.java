package org.molgenis.security.user;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.omx.auth.MolgenisUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.annotations.Test;

public class MolgenisUserServiceImplTest
{

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MolgenisUserServiceImpl()
	{
		new MolgenisUserServiceImpl(null, null, null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void update() throws DatabaseException
	{
		Database database = mock(Database.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

		MolgenisUser existingMolgenisUser = mock(MolgenisUser.class);
		when(existingMolgenisUser.getId()).thenReturn(1);
		when(existingMolgenisUser.getPassword()).thenReturn("encrypted-password");

		Query<MolgenisUser> queryUser = mock(Query.class);
		Query<MolgenisUser> queryUserSuccess = mock(Query.class);
		when(database.query(MolgenisUser.class)).thenReturn(queryUser);
		when(queryUser.eq(MolgenisUser.ID, 1)).thenReturn(queryUserSuccess);
		when(queryUser.eq(MolgenisUser.ID, -1)).thenReturn(queryUser);
		when(queryUserSuccess.find()).thenReturn(Arrays.<MolgenisUser> asList(existingMolgenisUser));

		MolgenisUserServiceImpl molgenisUserService = new MolgenisUserServiceImpl(database, database, passwordEncoder);

		MolgenisUser updatedMolgenisUser = mock(MolgenisUser.class);
		when(updatedMolgenisUser.getId()).thenReturn(1);
		when(updatedMolgenisUser.getPassword()).thenReturn("encrypted-password");

		molgenisUserService.update(updatedMolgenisUser);
		verify(passwordEncoder, never()).encode("encrypted-password");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void update_changePassword() throws DatabaseException
	{
		Database database = mock(Database.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

		MolgenisUser existingMolgenisUser = mock(MolgenisUser.class);
		when(existingMolgenisUser.getId()).thenReturn(1);
		when(existingMolgenisUser.getPassword()).thenReturn("new-password");

		Query<MolgenisUser> queryUser = mock(Query.class);
		Query<MolgenisUser> queryUserSuccess = mock(Query.class);
		when(database.query(MolgenisUser.class)).thenReturn(queryUser);
		when(queryUser.eq(MolgenisUser.ID, 1)).thenReturn(queryUserSuccess);
		when(queryUser.eq(MolgenisUser.ID, -1)).thenReturn(queryUser);
		when(queryUserSuccess.find()).thenReturn(Arrays.<MolgenisUser> asList(existingMolgenisUser));

		MolgenisUserServiceImpl molgenisUserService = new MolgenisUserServiceImpl(database, database, passwordEncoder);

		MolgenisUser updatedMolgenisUser = mock(MolgenisUser.class);
		when(updatedMolgenisUser.getId()).thenReturn(1);
		when(updatedMolgenisUser.getPassword()).thenReturn("password");

		molgenisUserService.update(updatedMolgenisUser);
		verify(passwordEncoder, times(1)).encode("password");
	}
}
