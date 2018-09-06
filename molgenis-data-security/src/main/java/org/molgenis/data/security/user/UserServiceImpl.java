package org.molgenis.data.security.user;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.auth.UserMetaData.USER;

import java.util.List;
import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
  private final DataService dataService;

  UserServiceImpl(DataService dataService) {
    this.dataService = requireNonNull(dataService);
  }

  @Override
  @RunAsSystem
  public List<User> getUsers() {
    return dataService.findAll(USER, User.class).collect(toList());
  }

  @Override
  @RunAsSystem
  public List<String> getSuEmailAddresses() {
    Stream<User> superUsers =
        dataService.findAll(
            USER, new QueryImpl<User>().eq(UserMetaData.SUPERUSER, true), User.class);
    return superUsers.map(User::getEmail).collect(toList());
  }

  @Override
  @RunAsSystem
  public User getUser(String username) {
    return dataService.findOne(
        USER, new QueryImpl<User>().eq(UserMetaData.USERNAME, username), User.class);
  }

  @Override
  @RunAsSystem
  public void update(User user) {
    dataService.update(USER, user);
  }

  @Override
  @RunAsSystem
  public User getUserByEmail(String email) {
    return dataService.findOne(
        USER, new QueryImpl<User>().eq(UserMetaData.EMAIL, email), User.class);
  }
}
