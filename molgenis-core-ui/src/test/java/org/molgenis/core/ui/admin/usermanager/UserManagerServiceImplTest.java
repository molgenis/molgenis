package org.molgenis.core.ui.admin.usermanager;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import org.molgenis.core.ui.admin.usermanager.UserManagerServiceImplTest.Config;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserMetaData;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.UserDetailsService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {Config.class})
public class UserManagerServiceImplTest extends AbstractTestNGSpringContextTests {
  private static Authentication AUTHENTICATION_PREVIOUS;

  @Configuration
  @EnableWebSecurity
  @EnableGlobalMethodSecurity(prePostEnabled = true)
  static class Config extends WebSecurityConfigurerAdapter {
    @Bean
    public DataService dataService() {
      return mock(DataService.class);
    }

    @Bean
    public UserManagerService userManagerService() {
      return new UserManagerServiceImpl(dataService());
    }

    @Override
    protected org.springframework.security.core.userdetails.UserDetailsService
        userDetailsService() {
      return mock(UserDetailsService.class);
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

  @BeforeClass
  public void setUpBeforeClass() {
    AUTHENTICATION_PREVIOUS = SecurityContextHolder.getContext().getAuthentication();
  }

  @AfterClass
  public static void tearDownAfterClass() {
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_PREVIOUS);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void userManagerServiceImpl() {
    new UserManagerServiceImpl(null);
  }

  @Test
  public void getAllMolgenisUsersSu() {
    String molgenisUserId0 = "id0";
    String molgenisUserName0 = "user0";
    User user0 = when(mock(User.class).getId()).thenReturn(molgenisUserId0).getMock();
    when(user0.getIdValue()).thenReturn(molgenisUserId0);
    when(user0.getUsername()).thenReturn(molgenisUserName0);
    String molgenisUserId1 = "id1";
    String molgenisUserName1 = "user1";
    User user1 = when(mock(User.class).getId()).thenReturn(molgenisUserId1).getMock();
    when(user1.getIdValue()).thenReturn(molgenisUserId1);
    when(user1.getUsername()).thenReturn(molgenisUserName1);
    when(dataService.findOneById(UserMetaData.USER, molgenisUserId0, User.class)).thenReturn(user0);
    when(dataService.findOneById(UserMetaData.USER, molgenisUserId1, User.class)).thenReturn(user1);
    when(dataService.findAll(UserMetaData.USER, User.class)).thenReturn(Stream.of(user0, user1));
    this.setSecurityContextSuperUser();
    assertEquals(
        userManagerService.getAllUsers(),
        Arrays.asList(new UserViewData(user0), new UserViewData(user1)));
  }

  @Test(expectedExceptions = AccessDeniedException.class)
  public void getAllMolgenisUsersNonSu() {
    this.setSecurityContextNonSuperUserWrite();
    this.userManagerService.getAllUsers();
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
