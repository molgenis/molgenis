package org.molgenis.data.security.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.UserMetadata.USER;

import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserMetadata;
import org.molgenis.data.security.user.UserServiceImplTest.Config;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {Config.class})
class UserServiceImplTest extends AbstractMockitoSpringContextTests {

  private static SecurityContext previousContext;

  @Configuration
  static class Config {
    @Bean
    UserServiceImpl molgenisUserServiceImpl() {
      return new UserServiceImpl(dataService());
    }

    @Bean
    DataService dataService() {
      return mock(DataService.class);
    }
  }

  @Autowired private UserServiceImpl molgenisUserServiceImpl;

  @Autowired private DataService dataService;

  @Test
  void MolgenisUserServiceImpl() {
    assertThrows(NullPointerException.class, () -> new UserServiceImpl(null));
  }

  @BeforeAll
  static void setUpBeforeClass() {
    previousContext = SecurityContextHolder.getContext();
    UserDetails userDetails =
        when(mock(UserDetails.class).getUsername()).thenReturn("username").getMock();
    Authentication authentication =
        when(mock(Authentication.class).getPrincipal()).thenReturn(userDetails).getMock();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    testContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(testContext);
  }

  @AfterAll
  static void tearDownAfterClass() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  void getUser() {
    String username = "username";

    User existingUser = mock(User.class);
    when(dataService.findOne(
            USER, new QueryImpl<User>().eq(UserMetadata.USERNAME, username), User.class))
        .thenReturn(existingUser);

    assertEquals(molgenisUserServiceImpl.getUser(username), existingUser);
  }

  @Test
  void getUsers() {
    User existingUser = mock(User.class);

    when(dataService.findAll(USER, User.class)).thenReturn(Stream.of(existingUser));

    assertEquals(molgenisUserServiceImpl.getUsers(), Collections.singletonList(existingUser));
  }
}
