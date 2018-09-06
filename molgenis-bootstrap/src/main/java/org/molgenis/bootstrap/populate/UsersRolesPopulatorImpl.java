package org.molgenis.bootstrap.populate;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.GroupService.*;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.molgenis.security.core.SidUtils.getRoleName;
import static org.molgenis.security.core.utils.SecurityUtils.*;

import com.google.common.collect.ImmutableList;
import java.util.UUID;
import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleFactory;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsersRolesPopulatorImpl implements UsersRolesPopulator {
  private static final Logger LOG = LoggerFactory.getLogger(UsersRolesPopulatorImpl.class);

  private static final String USERNAME_ADMIN = "admin";

  private final DataService dataService;
  private final UserFactory userFactory;
  private final RoleFactory roleFactory;

  @Value("${admin.password:@null}")
  private String adminPassword;

  @Value("${admin.email:molgenis+admin@gmail.com}")
  private String adminEmail;

  @Value("${anonymous.email:molgenis+anonymous@gmail.com}")
  private String anonymousEmail;

  UsersRolesPopulatorImpl(
      DataService dataService, UserFactory userFactory, RoleFactory roleFactory) {
    this.dataService = requireNonNull(dataService);
    this.userFactory = requireNonNull(userFactory);
    this.roleFactory = requireNonNull(roleFactory);
  }

  @Override
  @Transactional
  @RunAsSystem
  public void populate() {
    boolean changeAdminPassword = false;
    if (adminPassword == null) {
      adminPassword = UUID.randomUUID().toString();
      changeAdminPassword = true;
      LOG.info("Password for user 'admin': {}", adminPassword);
    }

    // create admin user
    User userAdmin = userFactory.create();
    userAdmin.setUsername(USERNAME_ADMIN);
    userAdmin.setPassword(adminPassword);
    userAdmin.setEmail(adminEmail);
    userAdmin.setActive(true);
    userAdmin.setSuperuser(true);
    userAdmin.setChangePassword(changeAdminPassword);

    // create anonymous user
    User anonymousUser = userFactory.create();
    anonymousUser.setUsername(ANONYMOUS_USERNAME);
    anonymousUser.setPassword(ANONYMOUS_USERNAME);
    anonymousUser.setEmail(anonymousEmail);
    anonymousUser.setActive(true);
    anonymousUser.setSuperuser(false);
    anonymousUser.setChangePassword(false);

    // create user role
    Role userRole = roleFactory.create();
    userRole.setName(getRoleName(AUTHORITY_USER));
    userRole.setLabel("User");
    userRole.setLabel("en", "User");
    userRole.setLabel("nl", "Gebruiker");
    userRole.setDescription("All authenticated users are a member of this Role.");
    userRole.setDescription("en", "All authenticated users are a member of this role.");
    userRole.setDescription("nl", "Alle geauthenticeerde gebruikers hebben deze rol.");

    Role viewer = roleFactory.create();
    viewer.setName(getRoleName(AUTHORITY_VIEWER));
    viewer.setLabel("Viewer");
    viewer.setDescription(
        "Role containing permissions needed to view data. This role is included in all group viewer roles.");

    Role editor = roleFactory.create();
    editor.setName(getRoleName(AUTHORITY_EDITOR));
    editor.setLabel("Editor");
    editor.setDescription(
        "Role containing permissions needed to edit data. This role is included in all group editor roles.");
    editor.setIncludes(ImmutableList.of(viewer));

    Role manager = roleFactory.create();
    manager.setName(getRoleName(AUTHORITY_MANAGER));
    manager.setLabel("Manager");
    manager.setDescription(
        "Role containing permissions needed to manage groups. This role is included in all group manager roles.");
    manager.setIncludes(ImmutableList.of(editor));

    Role aclTakeOwnership = roleFactory.create();
    aclTakeOwnership.setName(getRoleName(ROLE_ACL_TAKE_OWNERSHIP));
    aclTakeOwnership.setLabel("Take resource ownership");
    aclTakeOwnership.setDescription("Role granting the permission to change resource ownership.");

    Role aclModifyAuditing = roleFactory.create();
    aclModifyAuditing.setName(getRoleName(ROLE_ACL_MODIFY_AUDITING));
    aclModifyAuditing.setLabel("Modify access control auditing");
    aclModifyAuditing.setDescription("Role granting the permission to modify auditing details.");

    Role aclGeneralChanges = roleFactory.create();
    aclGeneralChanges.setName(getRoleName(ROLE_ACL_GENERAL_CHANGES));
    aclGeneralChanges.setLabel("General access control changes");
    aclGeneralChanges.setDescription(
        "Role granting the permission to make general access control list changes.");

    Role su = roleFactory.create();
    su.setName(getRoleName(AUTHORITY_SU));
    su.setLabel("Superuser");
    su.setDescription("Role granted to superusers.");
    su.setIncludes(ImmutableList.of(aclTakeOwnership, aclModifyAuditing, aclGeneralChanges));

    // persist entities
    dataService.add(USER, Stream.of(userAdmin, anonymousUser));
    dataService.add(
        ROLE,
        Stream.of(
            userRole,
            aclTakeOwnership,
            aclModifyAuditing,
            aclGeneralChanges,
            viewer,
            editor,
            manager,
            su));
  }
}
