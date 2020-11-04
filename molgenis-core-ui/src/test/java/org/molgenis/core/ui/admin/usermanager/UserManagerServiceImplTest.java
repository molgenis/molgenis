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

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.core.ui.admin.usermanager.UserManagerServiceImplTest.Config;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.core.SecurityContextRegistry;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
    SecurityContextRegistry securityContextRegistry() {
      return mock(SecurityContextRegistry.class);
    }

    @Bean
    UserManagerService userManagerService() {
      return new UserManagerServiceImpl(dataService(), securityContextRegistry());
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

  @Autowired private SecurityContextRegistry securityContextRegistry;

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

    Stream mockStream = mock(Stream.class);
    when(mockStream.count()).thenReturn(14L);
    when(securityContextRegistry.getSecurityContexts()).thenReturn(mockStream);

    setSecurityContextSuperUser();
    assertEquals(14L, userManagerService.getActiveSessionsCount());
  }

  @Test
  void testGetActiveSessionUserName() {
    SecurityContext securityContext = mockContextForUser("user");
    SecurityContext securityContext1 = mockContextForUser("user1");
    SecurityContext securityContextNoAuth = mock(SecurityContext.class);
    SecurityContext securityContextOtherPrinciple = mock(SecurityContext.class);
    Authentication authenticationOtherPrinciple = mock(Authentication.class);
    when(authenticationOtherPrinciple.getPrincipal()).thenReturn(new Object());
    when(securityContextOtherPrinciple.getAuthentication())
        .thenReturn(authenticationOtherPrinciple);
    when(securityContextRegistry.getSecurityContexts())
        .thenReturn(
            Stream.of(
                securityContext,
                securityContext1,
                securityContextNoAuth,
                securityContextOtherPrinciple));
    setSecurityContextSuperUser();
    assertEquals(Arrays.asList("user", "user1"), userManagerService.getActiveSessionUserNames());
  }

  private SecurityContext mockContextForUser(String userName) {
    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);
    org.springframework.security.core.userdetails.User user =
        mock(org.springframework.security.core.userdetails.User.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    when(user.getUsername()).thenReturn(userName);
    return securityContext;
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
