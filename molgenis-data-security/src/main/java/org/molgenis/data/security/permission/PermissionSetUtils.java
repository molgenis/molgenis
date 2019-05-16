package org.molgenis.data.security.permission;

import static java.lang.String.format;
import static org.molgenis.security.core.PermissionSet.COUNT;
import static org.molgenis.security.core.PermissionSet.COUNT_MASK;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.READMETA;
import static org.molgenis.security.core.PermissionSet.READ_MASK;
import static org.molgenis.security.core.PermissionSet.READ_META_MASK;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;
import static org.molgenis.security.core.PermissionSet.WRITEMETA_MASK;
import static org.molgenis.security.core.PermissionSet.WRITE_MASK;

import java.util.Optional;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.security.core.PermissionSet;
import org.springframework.security.acls.model.AccessControlEntry;

public class PermissionSetUtils {
  public static final String READMETA = "READMETA";
  public static final String COUNT = "COUNT";
  public static final String READ = "READ";
  public static final String WRITE = "WRITE";
  public static final String WRITEMETA = "WRITEMETA";

  private PermissionSetUtils() {}

  public static PermissionSet getPermissionSet(AccessControlEntry accessControlEntry) {
    int mask = accessControlEntry.getPermission().getMask();
    switch (mask) {
      case READ_META_MASK:
        return PermissionSet.READMETA;
      case COUNT_MASK:
        return PermissionSet.COUNT;
      case READ_MASK:
        return PermissionSet.READ;
      case WRITE_MASK:
        return PermissionSet.WRITE;
      case WRITEMETA_MASK:
        return PermissionSet.WRITEMETA;
      default:
        throw new IllegalArgumentException(format("Unexpected mask '%d'", mask));
    }
  }

  public static PermissionSet paramValueToPermissionSet(String paramValue) {
    switch (paramValue.toUpperCase()) {
      case READMETA:
        return PermissionSet.READMETA;
      case COUNT:
        return PermissionSet.COUNT;
      case READ:
        return PermissionSet.READ;
      case WRITE:
        return PermissionSet.WRITE;
      case WRITEMETA:
        return PermissionSet.WRITEMETA;
      default:
        throw new IllegalArgumentException(format("Unknown PermissionSet '%s'", paramValue));
    }
  }

  public static Optional<String> getPermissionStringValue(LabelledPermission labelledPermission) {
    PermissionSet permissionSet = labelledPermission.getPermission();
    if (permissionSet != null) {
      int mask = permissionSet.getMask();
      switch (mask) {
        case READ_META_MASK:
          return Optional.of(READMETA);
        case COUNT_MASK:
          return Optional.of(COUNT);
        case READ_MASK:
          return Optional.of(READ);
        case WRITE_MASK:
          return Optional.of(WRITE);
        case WRITEMETA_MASK:
          return Optional.of(WRITEMETA);
        default:
          throw new IllegalArgumentException(format("Unexpected mask '%d'", mask));
      }
    }
    return Optional.empty();
  }

  public static String getPermissionStringValue(PermissionSet permissionSet) {
    if (permissionSet != null) {
      int mask = permissionSet.getMask();
      switch (mask) {
        case READ_META_MASK:
          return READMETA;
        case COUNT_MASK:
          return COUNT;
        case READ_MASK:
          return READ;
        case WRITE_MASK:
          return WRITE;
        case WRITEMETA_MASK:
          return WRITEMETA;
        default:
          throw new IllegalArgumentException(format("Unexpected mask '%d'", mask));
      }
    }
    return null;
  }
}
