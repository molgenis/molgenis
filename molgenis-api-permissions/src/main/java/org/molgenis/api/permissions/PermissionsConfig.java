package org.molgenis.api.permissions;

import static java.util.Objects.requireNonNull;

import org.molgenis.api.permissions.inheritance.PermissionInheritanceResolver;
import org.molgenis.data.DataService;
import org.molgenis.security.acl.AclClassService;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.acl.ObjectIdentityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.model.MutableAclService;

@Configuration
public class PermissionsConfig {
  private final MutableAclService mutableAclService;
  private final AclClassService aclClassService;
  private final PermissionInheritanceResolver inheritanceResolver;
  private final ObjectIdentityService objectIdentityService;
  private final DataService dataService;
  private final MutableAclClassService mutableAclClassService;
  private final UserRoleTools userRoleTools;
  private final IdentityTools identityTools;

  PermissionsConfig(
      MutableAclService mutableAclService,
      AclClassService aclClassService,
      PermissionInheritanceResolver inheritanceResolver,
      ObjectIdentityService objectIdentityService,
      DataService dataService,
      MutableAclClassService mutableAclClassService,
      UserRoleTools userRoleTools,
      IdentityTools identityTools) {
    this.mutableAclService = requireNonNull(mutableAclService);
    this.aclClassService = requireNonNull(aclClassService);
    this.inheritanceResolver = requireNonNull(inheritanceResolver);
    this.objectIdentityService = requireNonNull(objectIdentityService);
    this.dataService = requireNonNull(dataService);
    this.mutableAclClassService = requireNonNull(mutableAclClassService);
    this.userRoleTools = requireNonNull(userRoleTools);
    this.identityTools = requireNonNull(identityTools);
  }

  @Bean
  public PermissionsService permissionsService() {
    PermissionsService permissionsService =
        new PermissionsServiceImpl(
            mutableAclService,
            aclClassService,
            inheritanceResolver,
            objectIdentityService,
            dataService,
            mutableAclClassService,
            userRoleTools,
            identityTools);
    return new PermissionServiceDecorator(
        permissionsService, dataService, identityTools, userRoleTools, mutableAclService);
  }
}
