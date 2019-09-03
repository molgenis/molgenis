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
    if (Boolean.TRUE.equals(user.isSuperuser()) && !currentUserIsSuOrSystem()) {
      throw new UserSuModificationException(user);
    }
  }

  private void validateUpdate(User user, User updatedUser) {
    if (!Boolean.TRUE.equals(user.isSuperuser())
        && updatedUser.isSuperuser()
        && !currentUserIsSuOrSystem()) {
      throw new UserSuModificationException(user);
    }
  }
}
