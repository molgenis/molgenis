package org.molgenis.core.ui.admin.usermanager;

import java.util.List;

public interface UserManagerService {
  List<UserViewData> getAllUsers();

  void setActivationUser(String userId, Boolean active);
}
