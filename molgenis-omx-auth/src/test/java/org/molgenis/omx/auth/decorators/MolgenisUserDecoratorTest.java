package org.molgenis.omx.auth.decorators;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisRoleGroupLink;
import org.molgenis.omx.auth.MolgenisUser;
import org.testng.annotations.Test;

public class MolgenisUserDecoratorTest
{

	@Test
	public void add() throws DatabaseException
	{
		MolgenisUser user1 = mock(MolgenisUser.class);
		when(user1.getPassword()).thenReturn("password1");
		MolgenisUser user2 = mock(MolgenisUser.class);
		when(user2.getPassword()).thenReturn("password2");
		List<MolgenisUser> users = Arrays.asList(user1, user2);

		MolgenisGroup allUsersGroup = mock(MolgenisGroup.class);
		Database database = mock(Database.class);
		when(
				database.find(MolgenisGroup.class, new QueryRule(MolgenisGroup.NAME, Operator.EQUALS,
						Login.GROUP_USERS_NAME))).thenReturn(Arrays.asList(allUsersGroup));
		@SuppressWarnings("unchecked")
		Mapper<MolgenisUser> mapper = Mockito.mock(Mapper.class);
		when(mapper.getDatabase()).thenReturn(database);
		when(mapper.add(users)).thenReturn(2);
		MolgenisUserDecorator<MolgenisUser> molgenisUserDecorator = new MolgenisUserDecorator<MolgenisUser>(mapper);
		assertEquals(molgenisUserDecorator.add(users), 2);
		verify(mapper).add(users);
		verify(database, times(2)).add(any(MolgenisRoleGroupLink.class));
	}

	@Test
	public void remove() throws DatabaseException
	{
		MolgenisUser user1 = mock(MolgenisUser.class);
		when(user1.getPassword()).thenReturn("password1");
		MolgenisUser user2 = mock(MolgenisUser.class);
		when(user2.getPassword()).thenReturn("password2");
		List<MolgenisUser> users = Arrays.asList(user1, user2);

		MolgenisRoleGroupLink link1 = mock(MolgenisRoleGroupLink.class);
		MolgenisRoleGroupLink link2 = mock(MolgenisRoleGroupLink.class);

		Database database = mock(Database.class);
		when(database.find(MolgenisRoleGroupLink.class, new QueryRule(MolgenisRoleGroupLink.ROLE_, Operator.IN, users)))
				.thenReturn(Arrays.asList(link1, link2));

		@SuppressWarnings("unchecked")
		Mapper<MolgenisUser> mapper = Mockito.mock(Mapper.class);
		when(mapper.getDatabase()).thenReturn(database);
		when(mapper.remove(users)).thenReturn(2);
		MolgenisUserDecorator<MolgenisUser> molgenisUserDecorator = new MolgenisUserDecorator<MolgenisUser>(mapper);
		assertEquals(molgenisUserDecorator.remove(users), 2);
		verify(mapper).remove(users);
		verify(database).remove(anyListOf(MolgenisRoleGroupLink.class));
	}
}
