package org.molgenis.data.security.auth;

import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;

import org.springframework.stereotype.Component;

@Component
class UserValidator {

  void validate(User user) {
    validateAddOrUpdate(user);
  }

  void validate(User user, User updatedUser) {
    validateAddOrUpdate(user);
    validateUpdate(user, updatedUser);
  }

  private void validateAddOrUpdate(User user) {
    if (user.isSuperuser() && !currentUserIsSuOrSystem()) {
      throw new UserSuModificationException(user);
    }
  }

  private void validateUpdate(User user, User updatedUser) {
    if (!user.isSuperuser() && updatedUser.isSuperuser() && !currentUserIsSuOrSystem()) {
      throw new UserSuModificationException(user);
    }
  }
}
