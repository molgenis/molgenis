package org.molgenis.bootstrap.populate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleFactory;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.test.AbstractMockitoTest;

class UsersRolesPopulatorImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private UserFactory userFactory;
  @Mock private RoleFactory roleFactory;
  private UsersRolesPopulatorImpl usersRolesPopulatorImpl;

  @BeforeEach
  void setUpBeforeEach() {
    usersRolesPopulatorImpl = new UsersRolesPopulatorImpl(dataService, userFactory, roleFactory);
  }

  @SuppressWarnings("unchecked")
  @Test
  void populate() {
    User user = mock(User.class);
    when(userFactory.create()).thenReturn(user);
    Role role = mock(Role.class);
    when(roleFactory.create()).thenReturn(role);

    usersRolesPopulatorImpl.populate();
    verify(dataService).add(eq("sys_sec_User"), any(Stream.class));
    verify(dataService).add(eq("sys_sec_Role"), any(Stream.class));
  }
}
