package org.molgenis.core.ui.admin.usermanager;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.UserMetadata.USER;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.core.ui.admin.usermanager.UserManagerServiceImplTest.Config;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.UserDetailsServiceImpl;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {Config.class})
class UserManagerServiceImplTest extends AbstractMockitoSpringContextTests {

  private SecurityContext previousContext;

  @Configuration
  @EnableWebSecurity
  @EnableGlobalMethodSecurity(prePostEnabled = true)
  static class Config extends WebSecurityConfigurerAdapter {
    @Bean
    DataService dataService() {
      return mock(DataService.class);
    }

    @Bean
    SessionRegistry sessionRegistry() {
      return mock(SessionRegistry.class);
    }

    @Bean
    UserManagerService userManagerService() {
      return new UserManagerServiceImpl(dataService(), sessionRegistry());
    }

    @Override
    protected org.springframework.security.core.userdetails.UserDetailsService
        userDetailsService() {
      return mock(UserDetailsServiceImpl.class);
    }

    @Bean
    @Override
    public org.springframework.security.core.userdetails.UserDetailsService
        userDetailsServiceBean() {
      return userDetailsService();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
      return super.authenticationManagerBean();
    }

    @Autowired
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth.inMemoryAuthentication().withUser("user").password("password").authorities("ROLE_USER");
    }
  }

  @Autowired private UserManagerService userManagerService;

  @Autowired private DataService dataService;

  @Autowired private SessionRegistry sessionRegistry;

  @BeforeEach
  void setUpBeforeEach() {
    previousContext = SecurityContextHolder.getContext();
    SecurityContext testContext = SecurityContextHolder.createEmptyContext();
    SecurityContextHolder.setContext(testContext);
  }

  @AfterEach
  void tearDownAfterEach() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  void userManagerServiceImpl() {
    assertThrows(NullPointerException.class, () -> new UserManagerServiceImpl(null, null));
  }

  @Test
  void getAllMolgenisUsersSu() {
    String molgenisUserId0 = "id0";
    String molgenisUserName0 = "user0";
    User user0 = when(mock(User.class).getId()).thenReturn(molgenisUserId0).getMock();
    when(user0.getUsername()).thenReturn(molgenisUserName0);
    String molgenisUserId1 = "id1";
    String molgenisUserName1 = "user1";
    User user1 = when(mock(User.class).getId()).thenReturn(molgenisUserId1).getMock();
    when(user1.getUsername()).thenReturn(molgenisUserName1);
    when(dataService.findOneById(USER, molgenisUserId0, User.class)).thenReturn(user0);
    when(dataService.findOneById(USER, molgenisUserId1, User.class)).thenReturn(user1);
    when(dataService.findAll(USER, User.class)).thenReturn(Stream.of(user0, user1));
    this.setSecurityContextSuperUser();
    assertEquals(
        asList(new UserViewData(user0), new UserViewData(user1)), userManagerService.getAllUsers());
  }

  @Test
  void getAllMolgenisUsersNonSu() {
    this.setSecurityContextNonSuperUserWrite();
    assertThrows(AccessDeniedException.class, () -> this.userManagerService.getAllUsers());
  }

  @Test
  void testSetActivationUser() {
    this.setSecurityContextSuperUser();

    String userId = "MyUserId";
    User user = mock(User.class);
    when(dataService.findOneById(USER, userId, User.class)).thenReturn(user);
    userManagerService.setActivationUser(userId, true);
    verify(user).setActive(true);
    verify(dataService).update(USER, user);
  }

  @Test
  void testSetActivationUserUnknown() {
    this.setSecurityContextSuperUser();

    assertThrows(
        UnknownEntityException.class,
        () -> userManagerService.setActivationUser("unknownUserId", true));
  }

  @Test
  void testGetActiveSessionsCount() {

    User user1 = mock(User.class);
    User user2 = mock(User.class);

    List<Object> principles = asList(user1, user2);
    when(sessionRegistry.getAllPrincipals()).thenReturn(principles);

    when(sessionRegistry.getAllSessions(user1, false))
        .thenReturn(singletonList(mock(SessionInformation.class)));

    when(sessionRegistry.getAllSessions(user2, false)).thenReturn(Collections.emptyList());

    setSecurityContextSuperUser();
    assertEquals(1L, userManagerService.getActiveSessionsCount());
  }

  private void setSecurityContextSuperUser() {
    Collection<? extends GrantedAuthority> authorities =
        singletonList(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(null, null, authorities));
  }

  private void setSecurityContextNonSuperUserWrite() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(null, null, emptyList()));
  }
}
