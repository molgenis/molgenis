package org.molgenis.security.user;

import org.molgenis.data.security.auth.User;

/** Manage account of the current user */
public interface UserAccountService {
  int MIN_PASSWORD_LENGTH = 6;

  /** Returns the currently logged in user */
  User getCurrentUser();

  /**
   * Update the currently logged in user
   *
   * @param user updated user
   */
  void updateCurrentUser(User user);

  /** Validates the password of the current logged in user */
  boolean validateCurrentUserPassword(String password);
}
