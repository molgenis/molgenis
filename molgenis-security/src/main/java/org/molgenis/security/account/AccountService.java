package org.molgenis.security.account;

import org.molgenis.data.security.auth.User;
import org.molgenis.security.account.exception.EmailAlreadyExistsException;
import org.molgenis.security.account.exception.UsernameAlreadyExistsException;

public interface AccountService {
  void createUser(User user, String baseActivationUri)
      throws UsernameAlreadyExistsException, EmailAlreadyExistsException;

  /** Activate a registered user */
  void activateUser(String activationCode);

  void changePassword(String username, String newPassword);

  void resetPassword(String userEmail);
}
