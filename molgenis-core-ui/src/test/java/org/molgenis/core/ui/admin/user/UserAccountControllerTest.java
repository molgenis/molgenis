package org.molgenis.core.ui.admin.user;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting.ENABLED;

import com.google.common.collect.ImmutableList;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.core.util.CountryCodes;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.twofactor.model.RecoveryCode;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.user.MolgenisUserException;
import org.molgenis.security.user.UserAccountService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.LocaleResolver;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SecurityTestExecutionListeners
class UserAccountControllerTest {
  @Mock private UserAccountService userAccountService;
  @Mock private RecoveryService recoveryService;
  @Mock private TwoFactorAuthenticationService twoFactorAuthenticationService;
  @Mock private AuthenticationSettings authenticationSettings;
  @Mock private LocaleResolver localeResolver;
  private UserAccountController userAccountController;

  @Mock private Model model;
  @Mock private User user;

  @BeforeEach
  void setUp() {
    userAccountController =
        new UserAccountController(
            userAccountService,
            recoveryService,
            twoFactorAuthenticationService,
            authenticationSettings,
            localeResolver);
  }

  @Test
  void testShowAccountWithCodes() {
    when(authenticationSettings.getTwoFactorAuthentication()).thenReturn(ENABLED);
    when(userAccountService.getCurrentUser()).thenReturn(user);
    when(user.isTwoFactorAuthentication()).thenReturn(true);

    assertEquals("view-useraccount", userAccountController.showAccount(model, true));

    verify(model).addAttribute("user", user);
    verify(model).addAttribute("countries", CountryCodes.get());
    verify(model).addAttribute("min_password_length", 6);
    verify(model).addAttribute("two_factor_authentication_app_option", ENABLED);
    verify(model).addAttribute("two_factor_authentication_user_enabled", true);
    verify(model).addAttribute("show_recovery_codes", true);
    verifyNoMoreInteractions(model);
  }

  @Test
  void testUpdateAccountNoOldPassword() {
    AccountUpdateRequest accountUpdateRequest = new AccountUpdateRequest();
    accountUpdateRequest.setNewpwd("abc");

    Exception exception =
        assertThrows(
            MolgenisUserException.class,
            () -> userAccountController.updateAccount(accountUpdateRequest));
    assertThat(exception.getMessage())
        .containsPattern("Please enter old password to update your password.");
  }

  @Test
  void testUpdateAccountOldPasswordIncorrect() {
    when(userAccountService.validateCurrentUserPassword("incorrect")).thenReturn(false);

    AccountUpdateRequest accountUpdateRequest = new AccountUpdateRequest();
    accountUpdateRequest.setOldpwd("incorrect");
    accountUpdateRequest.setNewpwd("new");
    accountUpdateRequest.setNewpwd2("new");

    Exception exception =
        assertThrows(
            MolgenisUserException.class,
            () -> userAccountController.updateAccount(accountUpdateRequest));
    assertThat(exception.getMessage()).containsPattern("The password you entered is incorrect.");
  }

  @Test
  void testUpdateAccountNonMatchingNewPasswords() {
    when(userAccountService.validateCurrentUserPassword("old")).thenReturn(true);

    AccountUpdateRequest accountUpdateRequest = new AccountUpdateRequest();
    accountUpdateRequest.setOldpwd("old");
    accountUpdateRequest.setNewpwd("abc");
    accountUpdateRequest.setNewpwd2("different");

    Exception exception =
        assertThrows(
            MolgenisUserException.class,
            () -> userAccountController.updateAccount(accountUpdateRequest));
    assertThat(exception.getMessage())
        .containsPattern("'New password' does not match 'Repeat new password'.");
  }

  @Test
  void testUpdateAccountNewPasswordTooShort() {
    when(userAccountService.validateCurrentUserPassword("old")).thenReturn(true);

    AccountUpdateRequest accountUpdateRequest = new AccountUpdateRequest();
    accountUpdateRequest.setOldpwd("old");
    accountUpdateRequest.setNewpwd("abc");
    accountUpdateRequest.setNewpwd2("abc");

    Exception exception =
        assertThrows(
            MolgenisUserException.class,
            () -> userAccountController.updateAccount(accountUpdateRequest));
    assertThat(exception.getMessage())
        .containsPattern("New password must consist of at least 6 characters.");
  }

  @Test
  void testUpdateAccountUpdatesPassword() {
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
  void testUpdateAccountAddressFields() {
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
  void testUpdateAccountNameFields() {
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
  void testUpdateAccountWorkFields() {
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
  void testEnableTwoFactorAuthentication() {
    assertEquals("redirect:/2fa/activation", userAccountController.enableTwoFactorAuthentication());
  }

  @Test
  @WithMockUser
  void testDisableTwoFactorAuthentication() {
    when(userAccountService.getCurrentUser()).thenReturn(user);
    when(user.isTwoFactorAuthentication()).thenReturn(false);
    assertEquals("view-useraccount", userAccountController.disableTwoFactorAuthentication(model));

    verify(twoFactorAuthenticationService).disableForUser();
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertTrue(authentication.isAuthenticated());
    assertTrue(authentication instanceof UsernamePasswordAuthenticationToken);
    org.springframework.security.core.userdetails.User principal =
        (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
    assertEquals("user", principal.getUsername());
    assertEquals(
        singleton("ROLE_USER"),
        principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(toSet()));
  }

  @Test
  void testResetTwoFactorAuthentication() {
    assertEquals("redirect:/2fa/activation", userAccountController.resetTwoFactorAuthentication());
    verify(twoFactorAuthenticationService).resetSecretForUser();
  }

  @Test
  void testGetRecoveryCodes() {
    RecoveryCode code1 = mock(RecoveryCode.class);
    when(code1.getCode()).thenReturn("code1");
    RecoveryCode code2 = mock(RecoveryCode.class);
    when(code2.getCode()).thenReturn("code2");
    when(recoveryService.getRecoveryCodes()).thenReturn(Stream.of(code1, code2));
    assertEquals(
        of("recoveryCodes", ImmutableList.of("code1", "code2")),
        userAccountController.getRecoveryCodes());
  }

  @Test
  void testGenerateRecoveryCodes() {
    RecoveryCode code1 = mock(RecoveryCode.class);
    when(code1.getCode()).thenReturn("code1");
    RecoveryCode code2 = mock(RecoveryCode.class);
    when(code2.getCode()).thenReturn("code2");
    when(recoveryService.generateRecoveryCodes()).thenReturn(Stream.of(code1, code2));

    assertEquals(
        of("recoveryCodes", ImmutableList.of("code1", "code2")),
        userAccountController.generateRecoveryCodes());
  }
}
