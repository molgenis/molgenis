package org.molgenis.api.permissions;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import org.molgenis.api.permissions.rsql.PermissionsQuery;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.testng.annotations.Test;

public class SidConversionUtilsTest {

  @Test
  public void testGetSids() {
    PermissionsQuery permissionsQuery =
        new PermissionsQuery(Collections.singletonList("user"), Collections.singletonList("role"));
    assertTrue(
        SidConversionUtils.getSids(permissionsQuery)
            .containsAll(
                Arrays.asList(new PrincipalSid("user"), new GrantedAuthoritySid("ROLE_role"))));
  }

  @Test
  public void testGetSid() {
    assertEquals(SidConversionUtils.getSid("user", null), new PrincipalSid("user"));
  }

  @Test
  public void testGetSidRole() {
    assertEquals(SidConversionUtils.getSid(null, "role"), new GrantedAuthoritySid("ROLE_role"));
  }

  @Test
  public void testGetUser() {
    assertEquals(SidConversionUtils.getUser(new PrincipalSid("test")), "test");
  }

  @Test
  public void testGetRole() {
    assertEquals(SidConversionUtils.getRole(new GrantedAuthoritySid("ROLE_test")), "test");
  }

  @Test
  public void testGetNameRole() {
    assertEquals(SidConversionUtils.getName(new GrantedAuthoritySid("ROLE_test")), "test");
  }

  @Test
  public void testGetName() {
    assertEquals(SidConversionUtils.getName(new PrincipalSid("test")), "test");
  }
}
