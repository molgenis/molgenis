package org.molgenis.api.permissions;

import static java.lang.String.format;
import static org.molgenis.security.core.PermissionSet.COUNT_MASK;
import static org.molgenis.security.core.PermissionSet.READ_MASK;
import static org.molgenis.security.core.PermissionSet.READ_META_MASK;
import static org.molgenis.security.core.PermissionSet.WRITEMETA_MASK;
import static org.molgenis.security.core.PermissionSet.WRITE_MASK;

import org.molgenis.security.core.PermissionSet;
import org.springframework.security.acls.model.AccessControlEntry;

public class PermissionSetUtils {
  public static final String READMETA = "READMETA";
  public static final String COUNT = "COUNT";
  public static final String READ = "READ";
  public static final String WRITE = "WRITE";
  public static final String WRITEMETA = "WRITEMETA";

  private PermissionSetUtils() {}

  public static String getPermissionStringValue(AccessControlEntry accessControlEntry) {
    int mask = accessControlEntry.getPermission().getMask();
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
}
