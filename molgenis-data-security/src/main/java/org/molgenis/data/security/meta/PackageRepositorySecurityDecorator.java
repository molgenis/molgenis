package org.molgenis.data.security.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.owned.AbstractRowLevelSecurityRepositoryDecorator.Action.*;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.molgenis.data.EntityAlreadyExistsException;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.PackagePermission;
import org.molgenis.data.security.exception.NullParentPackageNotSuException;
import org.molgenis.data.security.exception.PackagePermissionDeniedException;
import org.molgenis.data.security.owned.AbstractRowLevelSecurityRepositoryDecorator;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.security.acls.model.*;

public class PackageRepositorySecurityDecorator
    extends AbstractRowLevelSecurityRepositoryDecorator<Package> {
  private final MutableAclService mutableAclService;
  private final UserPermissionEvaluator userPermissionEvaluator;

  public PackageRepositorySecurityDecorator(
      Repository<Package> delegateRepository,
      MutableAclService mutableAclService,
      UserPermissionEvaluator userPermissionEvaluator) {
    super(delegateRepository, mutableAclService);
    this.mutableAclService = requireNonNull(mutableAclService);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
  }

  @Override
  public boolean isActionPermitted(Package pack, Action action) {
    boolean permitted;
    if (action == CREATE || action == DELETE) {
      permitted = isActionPermittedOnParent(pack, action);
    } else if (action == UPDATE) {
      permitted =
          isActionPermittedOnParent(pack, action)
              && internalIsActionPermitted(pack.getId(), action);
    } else {
      permitted = internalIsActionPermitted(pack.getId(), action);
    }
    return permitted;
  }

  @Override
  public boolean isActionPermitted(Object id, Action action) {
    boolean isPermitted = false;
    Package pack = delegate().findOneById(id);
    if (pack != null) {
      isPermitted = isActionPermitted(pack, action);
    }
    return isPermitted;
  }

  private boolean internalIsActionPermitted(Object id, Action action) {
    return userPermissionEvaluator.hasPermission(
        new PackageIdentity(id.toString()), getPermissionForAction(action));
  }

  @Override
  public void throwPermissionException(Package pack, Action action) {
    throw new PackagePermissionDeniedException(getPermissionForAction(action), pack);
  }

  @Override
  public Package findOneById(Object id) {
    Package pack = delegate().findOneById(id);
    if (pack != null && !isActionPermitted(id, READ)) {
      throwPermissionException(pack, READ);
    }
    return pack;
  }

  @Override
  public void add(Package pack) {
    if (isActionPermitted(pack, CREATE)) {
      createAcl(pack);
      delegate().add(pack);
    } else {
      if (pack.getParent() == null) {
        throw new NullParentPackageNotSuException();
      } else {
        throw new PackagePermissionDeniedException(getPermissionForAction(CREATE), pack);
      }
    }
  }

  @Override
  public void update(Package pack) {
    if (!isActionPermitted(pack, UPDATE)) {
      if (pack.getParent() == null) {
        throw new NullParentPackageNotSuException();
      }
      throwPermissionException(pack, UPDATE);
    }
    delegate().update(pack);
    updateAcl(pack);
  }

  @Override
  public Integer add(Stream<Package> packages) {
    LinkedList<Package> resolved = new LinkedList<>();
    resolveDependencies(packages.collect(Collectors.toList()), resolved);
    return super.add(resolved.stream());
  }

  private void resolveDependencies(List<Package> packages, LinkedList<Package> resolved) {
    if (packages.size() != resolved.size()) {
      for (Package pack : packages) {
        if (!resolved.contains(pack)
            && (!packages.contains(pack.getParent()) || resolved.contains(pack.getParent()))) {
          resolved.add(pack);
        }
      }
      resolveDependencies(packages, resolved);
    }
  }

  @Override
  public void createAcl(Package pack) {
    PackageIdentity packageIdentity = new PackageIdentity(pack);
    MutableAcl acl;
    try {
      acl = mutableAclService.createAcl(packageIdentity);
    } catch (AlreadyExistsException e) {
      throw new EntityAlreadyExistsException(pack, e);
    }
    if (pack.getParent() != null) {
      ObjectIdentity parentIdentity = new PackageIdentity(pack.getParent());
      Acl parentAcl = mutableAclService.readAclById(parentIdentity);
      acl.setParent(parentAcl);
      mutableAclService.updateAcl(acl);
    }
  }

  @Override
  public void deleteAcl(Object id) {
    PackageIdentity packageIdentity = new PackageIdentity(id.toString());
    mutableAclService.deleteAcl(packageIdentity, true);
  }

  @Override
  public void deleteAcl(Package pack) {
    deleteAcl(pack.getId());
  }

  @Override
  public void updateAcl(Package pack) {
    PackageIdentity packageIdentity = new PackageIdentity(pack);
    MutableAcl acl = (MutableAcl) mutableAclService.readAclById(packageIdentity);
    if (pack.getParent() != null) {
      ObjectIdentity parentIdentity = new PackageIdentity(pack.getParent());
      Acl parentAcl = mutableAclService.readAclById(parentIdentity);
      if (!parentAcl.equals(acl.getParentAcl())) {
        acl.setParent(parentAcl);
        mutableAclService.updateAcl(acl);
      }
    }
  }

  private boolean isActionPermittedOnParent(Package pack, Action action) {
    boolean isPermitted = true;
    Package parent = pack.getParent();
    PackagePermission permission = getPermissionForAction(action);
    if (parent == null) {
      if (isParentUpdated(action, pack) && !currentUserIsSuOrSystem()) {
        isPermitted = false;
      }
    } else if (isParentUpdated(action, pack)
        && !userPermissionEvaluator.hasPermission(
            new PackageIdentity(parent.getId()), permission)) {
      isPermitted = false;
    }
    return isPermitted;
  }

  private boolean isParentUpdated(Action action, Package pack) {
    boolean updated;
    if (action == CREATE || action == DELETE) {
      updated = true;
    } else {
      Package currentPackage = delegate().findOneById(pack.getId());
      if (currentPackage.getParent() == null) {
        updated = pack.getParent() != null;
      } else {
        updated = !currentPackage.getParent().equals(pack.getParent());
      }
    }
    return updated;
  }

  private static PackagePermission getPermissionForAction(Action action) {
    // In case of delete and create the permission has to have been granted on the parent package
    PackagePermission permission;
    switch (action) {
      case COUNT:
      case READ:
        permission = PackagePermission.VIEW;
        break;
      case UPDATE:
      case DELETE:
        permission = PackagePermission.UPDATE;
        break;
      case CREATE:
        permission = PackagePermission.ADD_PACKAGE;
        break;
      default:
        throw new IllegalArgumentException("Illegal repository Action");
    }
    return permission;
  }
}
