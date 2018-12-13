package org.molgenis.data.security.user;

import java.util.List;
import javax.annotation.CheckForNull;
import org.molgenis.data.security.auth.User;

public interface UserService {
  List<User> getUsers();

  /** Returns e-mail addresses of super users */
  List<String> getSuEmailAddresses();

  /** Returns the given user */
  @CheckForNull
  User getUser(String username);

  /**
   * Find a user by it's email.
   *
   * @return the user or null if not found
   */
  User getUserByEmail(String email);

  /** Update user */
  void update(User user);
}
