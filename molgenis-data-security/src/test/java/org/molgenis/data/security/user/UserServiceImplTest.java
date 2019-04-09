package org.molgenis.data.security.user;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.UserMetadata.USER;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserMetadata;
import org.molgenis.data.security.user.UserServiceImplTest.Config;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {Config.class})
public class UserServiceImplTest extends AbstractTestNGSpringContextTests {

  private static SecurityContext previousContext;

  @Configuration
  static class Config {
    @Bean
    public UserServiceImpl molgenisUserServiceImpl() {
      return new UserServiceImpl(dataService());
    }

    @Bean
    public DataService dataService() {
      return mock(DataService.class);
    }
  }

  @Autowired private UserServiceImpl molgenisUserServiceImpl;

  @Autowired private DataService dataService;

  @Test(expectedExceptions = NullPointerException.class)
  public void MolgenisUserServiceImpl() {
    new UserServiceImpl(null);
  }

  @BeforeClass
  public static void setUpBeforeClass() {
    previousContext = SecurityContextHolder.getContext();
    UserDetails userDetails =
        when(mock(UserDetails.class).getUsername()).thenReturn("username").getMock();
    Authentication authentication =
        when(mock(Authentication.class).getPrincipal()).thenReturn(userDetails).getMock();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    testContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(testContext);
  }

  @AfterClass
  public void tearDownAfterClass() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  public void getUser() {
    String username = "username";

    User existingUser = mock(User.class);
    when(existingUser.getId()).thenReturn("1");
    when(existingUser.getUsername()).thenReturn(username);
    when(existingUser.getPassword()).thenReturn("encrypted-password");

    when(dataService.findOne(
            USER, new QueryImpl<User>().eq(UserMetadata.USERNAME, username), User.class))
        .thenReturn(existingUser);

    assertEquals(molgenisUserServiceImpl.getUser(username), existingUser);
  }

  @Test
  public void getUsers() {
    User existingUser = mock(User.class);

    when(dataService.findAll(USER, User.class)).thenReturn(Stream.of(existingUser));

    assertEquals(molgenisUserServiceImpl.getUsers(), Collections.singletonList(existingUser));
  }
}
