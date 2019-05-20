package org.molgenis.data.security.permission;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.DataService;
import org.molgenis.data.security.permission.inheritance.PermissionInheritanceResolver;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.acl.ObjectIdentityService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.model.MutableAclService;

@Configuration
public class PermissionsConfig {
  private final MutableAclService mutableAclService;
  private final PermissionInheritanceResolver inheritanceResolver;
  private final ObjectIdentityService objectIdentityService;
  private final DataService dataService;
  private final MutableAclClassService mutableAclClassService;
  private final UserRoleTools userRoleTools;
  private final EntityHelper entityHelper;
  private final UserPermissionEvaluator userPermissionEvaluator;

  PermissionsConfig(
      MutableAclService mutableAclService,
      PermissionInheritanceResolver inheritanceResolver,
      ObjectIdentityService objectIdentityService,
      DataService dataService,
      MutableAclClassService mutableAclClassService,
      UserRoleTools userRoleTools,
      EntityHelper entityHelper,
      UserPermissionEvaluator userPermissionEvaluator) {
    this.mutableAclService = requireNonNull(mutableAclService);
    this.inheritanceResolver = requireNonNull(inheritanceResolver);
    this.objectIdentityService = requireNonNull(objectIdentityService);
    this.dataService = requireNonNull(dataService);
    this.mutableAclClassService = requireNonNull(mutableAclClassService);
    this.userRoleTools = requireNonNull(userRoleTools);
    this.entityHelper = requireNonNull(entityHelper);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
  }

  @Bean
  public PermissionService permissionsService() {
    PermissionService permissionService =
        new PermissionServiceImpl(
            mutableAclService,
            inheritanceResolver,
            objectIdentityService,
            dataService,
            mutableAclClassService,
            userRoleTools,
            entityHelper,
            userPermissionEvaluator);
    return new PermissionServiceDecorator(
        permissionService,
        entityHelper,
        userRoleTools,
        mutableAclService,
        mutableAclClassService,
        userPermissionEvaluator);
  }
}
