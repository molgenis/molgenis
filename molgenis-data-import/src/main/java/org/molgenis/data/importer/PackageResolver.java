package org.molgenis.data.importer;

import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGE_NAME;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGE_PARENT;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.molgenis.data.Entity;
import org.molgenis.data.importer.emx.exception.MissingRootPackageException;
import org.molgenis.data.importer.emx.exception.PackageResolveException;

public class PackageResolver {

  private PackageResolver() {}

  /** Resolves package fullNames by looping through all the packages and their parents */
  public static List<Entity> resolvePackages(Iterable<Entity> packageRepo) {
    List<Entity> resolved = new LinkedList<>();
    if ((packageRepo == null) || Iterables.isEmpty(packageRepo)) return resolved;

    List<Entity> unresolved = new ArrayList<>();
    Map<String, Entity> resolvedByName = new HashMap<>();

    for (Entity pack : packageRepo) {
      String name = pack.getString(EMX_PACKAGE_NAME);
      String parentName = pack.getString(EMX_PACKAGE_PARENT);

      if (Strings.isNullOrEmpty(parentName)) {
        resolved.add(pack);
        resolvedByName.put(name, pack);
      } else {
        unresolved.add(pack);
      }
    }

    if (resolved.isEmpty()) {
      throw new MissingRootPackageException();
    }

    List<Entity> ready = new ArrayList<>();
    while (!unresolved.isEmpty()) {
      for (Entity pack : unresolved) {
        Entity parent = resolvedByName.get(pack.getString(EMX_PACKAGE_PARENT));
        if (parent != null) {
          String name = pack.getString(EMX_PACKAGE_NAME);
          ready.add(pack);
          resolvedByName.put(name, pack);
        }
      }

      if (ready.isEmpty()) {
        throw new PackageResolveException();
      }
      resolved.addAll(ready);
      unresolved.removeAll(ready);
      ready.clear();
    }

    return resolved;
  }
}
