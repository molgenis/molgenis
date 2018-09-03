package org.molgenis.core.ui.admin.usermanager;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.auth.UserMetaData.USER;

import java.util.List;
import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagerServiceImpl implements UserManagerService {
  private final DataService dataService;

  UserManagerServiceImpl(DataService dataService) {
    this.dataService = requireNonNull(dataService);
  }

  @Override
  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional(readOnly = true)
  public List<UserViewData> getAllUsers() {
    Stream<User> users = dataService.findAll(USER, User.class);
    return this.parseToMolgenisUserViewData(users);
  }

  @Override
  @PreAuthorize("hasAnyRole('ROLE_SU')")
  @Transactional
  public void setActivationUser(String userId, Boolean active) {
    User mu = this.dataService.findOneById(USER, userId, User.class);
    mu.setActive(active);
    this.dataService.update(USER, mu);
  }

  private List<UserViewData> parseToMolgenisUserViewData(Stream<User> users) {
    return users.map(UserViewData::new).collect(toList());
  }
}
