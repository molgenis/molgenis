package org.molgenis.security.acl;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.model.AclCache;

class MutableAclClassServiceImplTest extends AbstractMockitoTest {
  @Mock private JdbcTemplate jdbcTemplate;
  @Mock private AclCache aclCache;
  private MutableAclClassServiceImpl mutableAclClassService;

  @BeforeEach
  void setUpBeforeMethod() {
    mutableAclClassService = new MutableAclClassServiceImpl(jdbcTemplate, aclCache);
  }

  @Test
  void testCreateAclClass() {
    String type = "MyType";
    mutableAclClassService.createAclClass(type, String.class);
    verify(jdbcTemplate)
        .update(
            "insert into acl_class (class, class_id_type) values (?, ?)", type, "java.lang.String");
    verifyZeroInteractions(aclCache);
  }

  @Test
  void testDeleteAclClass() {
    String type = "MyType";
    mutableAclClassService.deleteAclClass(type);
    verify(jdbcTemplate).update("delete from acl_class where class=?", type);
    verify(aclCache).clearCache();
  }

  @Test
  void testHasAclClassTrueWithAclClassCacheInvalidation() {
    String type = "MyType";
    when(jdbcTemplate.queryForObject(
            "select count(*) from acl_class WHERE class = ?", new Object[] {type}, Integer.class))
        .thenReturn(1);
    assertTrue(mutableAclClassService.hasAclClass(type));
    mutableAclClassService.clearCache();
    assertTrue(mutableAclClassService.hasAclClass(type));
    verifyZeroInteractions(aclCache);
    verify(jdbcTemplate, times(2))
        .queryForObject(
            "select count(*) from acl_class WHERE class = ?", new Object[] {type}, Integer.class);
  }

  @Test
  void testHasAclClassFalseCached() {
    String type = "MyType";
    when(jdbcTemplate.queryForObject(
            "select count(*) from acl_class WHERE class = ?", new Object[] {type}, Integer.class))
        .thenReturn(0);
    assertFalse(mutableAclClassService.hasAclClass(type));
    assertFalse(mutableAclClassService.hasAclClass(type));
    verifyZeroInteractions(aclCache);
    verify(jdbcTemplate, times(1))
        .queryForObject(
            "select count(*) from acl_class WHERE class = ?", new Object[] {type}, Integer.class);
  }

  @Test
  void testGetAclClassTypes() {
    List<String> aclClassTypes = asList("MyType0", "MyType1");
    when(jdbcTemplate.queryForList("select class from acl_class", String.class))
        .thenReturn(aclClassTypes);
    assertEquals(mutableAclClassService.getAclClassTypes(), aclClassTypes);
    verifyZeroInteractions(aclCache);
  }
}
