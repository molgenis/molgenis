package org.molgenis.data.security.auth;

import java.util.List;
import org.molgenis.data.meta.model.Package;

public interface GroupPackageService {

  void createGroup(Package aPackage);

  void createGroups(List<Package> packages);

  void deleteGroup(Package aPackage);
}
