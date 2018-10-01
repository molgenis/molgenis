package org.molgenis.core.ui.admin.user;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting.ENABLED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.stream.Stream;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.core.util.CountryCodes;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.twofactor.model.RecoveryCode;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.ui.Model;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
@ContextConfiguration(classes = UserAccountControllerTestConfig.class)
public class UserAccountControllerTest extends AbstractMockitoTestNGSpringContextTests {
  @Autowired private UserAccountControllerTestConfig config;
  @Autowired private UserAccountService userAccountService;
  @Autowired private RecoveryService recoveryService;
  @Autowired private TwoFactorAuthenticationService twoFactorAuthenticationService;
  @Autowired private AuthenticationSettings authenticationSettings;
  @Autowired private UserAccountController userAccountController;
  @Mock private Model model;
  @Mock private User user;

  @BeforeMethod
  public void setUp() {
    config.resetMocks();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testShowAccountWithCodes() {
    when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(ENABLED);
    when(userAccountService.getCurrentUser()).thenReturn(user);
    when(user.isTwoFactorAuthentication()).thenReturn(true);

    assertEquals(userAccountController.showAccount(model, true), "view-useraccount");

    verify(model).addAttribute("user", user);
    verify(model).addAttribute("countries", CountryCodes.get());
    verify(model).addAttribute("min_password_length", 6);
    verify(model).addAttribute("two_factor_authentication_app_option", ENABLED);
    verify(model).addAttribute("two_factor_authentication_user_enabled", true);
    verify(model).addAttribute("show_recovery_codes", true);
    verifyNoMoreInteractions(model);
  }

  @Test(
      expectedExceptions = MolgenisUserException.class,
      expectedExceptionsMessageRegExp = "Please enter old password to update your password.")
  public void testUpdateAccountNoOldPassword() {
    AccountUpdateRequest accountUpdateRequest = new AccountUpdateRequest();
    accountUpdateRequest.setNewpwd("abc");

    userAccountController.updateAccount(accountUpdateRequest);
  }

  @Test(
      expectedExceptions = MolgenisUserException.class,
      expectedExceptionsMessageRegExp = "The password you entered is incorrect.")
  public void testUpdateAccountOldPasswordIncorrect() {
    when(userAccountService.validateCurrentUserPassword("incorrect")).thenReturn(false);

    AccountUpdateRequest accountUpdateRequest = new AccountUpdateRequest();
    accountUpdateRequest.setOldpwd("incorrect");
    accountUpdateRequest.setNewpwd("new");
    accountUpdateRequest.setNewpwd2("new");

    userAccountController.updateAccount(accountUpdateRequest);
  }

  @Test(
      expectedExceptions = MolgenisUserException.class,
      expectedExceptionsMessageRegExp = "'New password' does not match 'Repeat new password'.")
  public void testUpdateAccountNonMatchingNewPasswords() {
    when(userAccountService.validateCurrentUserPassword("old")).thenReturn(true);

    AccountUpdateRequest accountUpdateRequest = new AccountUpdateRequest();
    accountUpdateRequest.setOldpwd("old");
    accountUpdateRequest.setNewpwd("abc");
    accountUpdateRequest.setNewpwd2("different");

    userAccountController.updateAccount(accountUpdateRequest);
  }

  @Test(
      expectedExceptions = MolgenisUserException.class,
      expectedExceptionsMessageRegExp = "New password must consist of at least 6 characters.")
  public void testUpdateAccountNewPasswordTooShort() {
    when(userAccountService.validateCurrentUserPassword("old")).thenReturn(true);

    AccountUpdateRequest accountUpdateRequest = new AccountUpdateRequest();
    accountUpdateRequest.setOldpwd("old");
    accountUpdateRequest.setNewpwd("abc");
    accountUpdateRequest.setNewpwd2("abc");

    userAccountController.updateAccount(accountUpdateRequest);
  }

  @Test
  public void testUpdateAccountUpdatesPassword() {
    when(userAccountService.validateCurrentUserPassword("old")).thenReturn(true);
    when(userAccountService.getCurrentUser()).thenReturn(user);

    AccountUpdateRequest accountUpdateRequest = new AccountUpdateRequest();
    accountUpdateRequest.setOldpwd("old");
    accountUpdateRequest.setNewpwd("abcdef");
    accountUpdateRequest.setNewpwd2("abcdef");

    userAccountController.updateAccount(accountUpdateRequest);

    verify(user).setPassword("abcdef");
    verify(userAccountService).updateCurrentUser(user);
    verifyNoMoreInteractions(user);
  }

  @Test
  public void testUpdateAccountAddressFields() {
    when(userAccountService.getCurrentUser()).thenReturn(user);

    AccountUpdateRequest accountUpdateRequest = new AccountUpdateRequest();
    accountUpdateRequest.setAddress("address");
    accountUpdateRequest.setCity("city");
    accountUpdateRequest.setCountry("TV");

    userAccountController.updateAccount(accountUpdateRequest);

    verify(user).setAddress("address");
    verify(user).setCity("city");
    verify(user).setCountry("Tuvalu");
    verify(userAccountService).updateCurrentUser(user);
    verifyNoMoreInteractions(user);
  }

  @Test
  public void testUpdateAccountNameFields() {
    when(userAccountService.getCurrentUser()).thenReturn(user);

    AccountUpdateRequest accountUpdateRequest = new AccountUpdateRequest();
    accountUpdateRequest.setTitle("Mr.");
    accountUpdateRequest.setFirstname("First");
    accountUpdateRequest.setMiddleNames("Middle Middle");
    accountUpdateRequest.setLastname("Last");

    userAccountController.updateAccount(accountUpdateRequest);

    verify(user).setTitle("Mr.");
    verify(user).setFirstName("First"); // <- Casing doesn't match
    verify(user).setMiddleNames("Middle Middle");
    verify(user).setLastName("Last");
    verify(userAccountService).updateCurrentUser(user);
    verifyNoMoreInteractions(user);
  }

  @Test
  public void testUpdateAccountWorkFields() {
    when(userAccountService.getCurrentUser()).thenReturn(user);

    AccountUpdateRequest accountUpdateRequest = new AccountUpdateRequest();
    accountUpdateRequest.setPosition("Position");
    accountUpdateRequest.setDepartment("Department");
    accountUpdateRequest.setInstitute("Institute");
    accountUpdateRequest.setFax("Fax");
    accountUpdateRequest.setPhone("Phone");
    accountUpdateRequest.setTollFreePhone("Nope");

    userAccountController.updateAccount(accountUpdateRequest);

    verify(user).setRole("Position"); // <- Names don't match
    verify(user).setDepartment("Department");
    verify(user).setAffiliation("Institute"); // <- Names don't match
    verify(user).setFax("Fax");
    verify(user).setPhone("Phone");
    verify(user).setTollFreePhone("Nope");

    verify(userAccountService).updateCurrentUser(user);
    verifyNoMoreInteractions(user);
  }

  @Test
  public void testEnableTwoFactorAuthentication() {
    assertEquals(userAccountController.enableTwoFactorAuthentication(), "redirect:/2fa/activation");
  }

  @Test
  @WithMockUser
  public void testDisableTwoFactorAuthentication() {
    when(userAccountService.getCurrentUser()).thenReturn(user);
    when(user.isTwoFactorAuthentication()).thenReturn(false);
    assertEquals(userAccountController.disableTwoFactorAuthentication(model), "view-useraccount");

    verify(twoFactorAuthenticationService).disableForUser();
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertTrue(authentication.isAuthenticated());
    assertTrue(authentication instanceof UsernamePasswordAuthenticationToken);
    org.springframework.security.core.userdetails.User principal =
        (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
    assertEquals(principal.getUsername(), "user");
    assertEquals(
        principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(toSet()),
        singleton("ROLE_USER"));
  }

  @Test
  public void testResetTwoFactorAuthentication() {
    assertEquals(userAccountController.resetTwoFactorAuthentication(), "redirect:/2fa/activation");
    verify(twoFactorAuthenticationService).resetSecretForUser();
  }

  @Test
  public void testGetRecoveryCodes() {
    RecoveryCode code1 = mock(RecoveryCode.class);
    when(code1.getCode()).thenReturn("code1");
    RecoveryCode code2 = mock(RecoveryCode.class);
    when(code2.getCode()).thenReturn("code2");
    when(recoveryService.getRecoveryCodes()).thenReturn(Stream.of(code1, code2));
    assertEquals(
        userAccountController.getRecoveryCodes(),
        ImmutableMap.of("recoveryCodes", ImmutableList.of("code1", "code2")));
  }

  @Test
  public void testGenerateRecoveryCodes() {
    RecoveryCode code1 = mock(RecoveryCode.class);
    when(code1.getCode()).thenReturn("code1");
    RecoveryCode code2 = mock(RecoveryCode.class);
    when(code2.getCode()).thenReturn("code2");
    when(recoveryService.generateRecoveryCodes()).thenReturn(Stream.of(code1, code2));

    assertEquals(
        userAccountController.generateRecoveryCodes(),
        ImmutableMap.of("recoveryCodes", ImmutableList.of("code1", "code2")));
  }
}
