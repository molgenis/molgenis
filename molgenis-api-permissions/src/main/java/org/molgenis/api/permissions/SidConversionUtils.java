package org.molgenis.api.permissions;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.molgenis.security.core.SidUtils.createRoleSid;
import static org.molgenis.security.core.SidUtils.createUserSid;

import java.util.ArrayList;
import java.util.List;
import org.molgenis.api.permissions.rsql.PermissionsQuery;
import org.molgenis.security.core.SidUtils;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;

public class SidConversionUtils {

  private SidConversionUtils() {}

  public static List<Sid> getSids(PermissionsQuery permissionsQuery) {
    return getSids(permissionsQuery.getUsers(), permissionsQuery.getRoles());
  }

  public static Sid getSid(String user, String role) {
    if ((isNullOrEmpty(user) && isNullOrEmpty(role))
        || (!isNullOrEmpty(user) && !isNullOrEmpty(role))) {
      return null;
    }
    if (!isNullOrEmpty(user)) {
      return createUserSid(user);
    }
    return createRoleSid(role);
  }

  private static List<Sid> getSids(List<String> users, List<String> roles) {
    List<Sid> results = new ArrayList<>();
    for (String user : users) {
      results.add(createUserSid(user));
    }
    for (String role : roles) {
      results.add(createRoleSid(role));
    }
    return results;
  }

  public static String getUser(Sid sid) {
    if (sid instanceof PrincipalSid) {
      return ((PrincipalSid) sid).getPrincipal();
    }
    return null;
  }

  public static String getRole(Sid sid) {
    if (sid instanceof GrantedAuthoritySid) {
      String role = ((GrantedAuthoritySid) sid).getGrantedAuthority();
      return SidUtils.getRoleName(role);
    }
    return null;
  }

  public static String getName(Sid sid) {
    String name = getRole(sid);
    if (name == null) {
      name = getUser(sid);
    }
    if (name == null) {
      throw new IllegalStateException(
          "Sid should always be either a GrantedAuthoritySid or a PrincipalSid");
    }
    return name;
  }
}
