package org.molgenis.security.acl;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.security.permission.EntityHelper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

class ObjectIdentityServiceImplTest extends AbstractMolgenisSpringTest {

  @Mock NamedParameterJdbcTemplate jdbcTemplate;
  @Mock EntityHelper entityHelper;
  private ObjectIdentityServiceImpl objectIdentityService;

  @BeforeEach
  void setUp() {
    objectIdentityService = new ObjectIdentityServiceImpl(jdbcTemplate, entityHelper);
  }

  @Test
  void testGetNrOfObjectIdentities() {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("classId", "classId");
    doReturn(new Integer(12))
        .when(jdbcTemplate)
        .queryForObject(
            "SELECT COUNT( DISTINCT acl_object_identity.object_id_identity) FROM acl_object_identity LEFT JOIN acl_class ON acl_object_identity.object_id_class = acl_class.id LEFT JOIN acl_entry ON acl_entry.acl_object_identity = acl_object_identity.id LEFT JOIN acl_sid ON acl_entry.sid = acl_sid.id WHERE acl_class.class = :classId",
            paramMap,
            Integer.class);
    assertEquals(12, objectIdentityService.getNrOfObjectIdentities("classId").intValue());
  }

  @Test
  void testGetNrOfObjectIdentities1() {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("classId", "classId");
    paramMap.put("sids", Collections.singletonList("ROLE_role1"));
    doReturn(new Integer(12))
        .when(jdbcTemplate)
        .queryForObject(
            "SELECT COUNT( DISTINCT acl_object_identity.object_id_identity) FROM acl_object_identity LEFT JOIN acl_class ON acl_object_identity.object_id_class = acl_class.id LEFT JOIN acl_entry ON acl_entry.acl_object_identity = acl_object_identity.id LEFT JOIN acl_sid ON acl_entry.sid = acl_sid.id WHERE acl_class.class = :classId AND acl_sid.sid IN (:sids)",
            paramMap,
            Integer.class);
    Sid sid = new GrantedAuthoritySid("ROLE_role1");
    assertEquals(
        12, objectIdentityService.getNrOfObjectIdentities("classId", singleton(sid)).intValue());
  }

  @Test
  void testGetObjectIdentities() {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("classId", "classId");
    paramMap.put("limit", 10);
    paramMap.put("offset", 20);
    List<Map<String, Object>> result = new ArrayList<>();
    Map<String, Object> result1 = new HashMap<>();
    result1.put("object_id_identity", "test1");
    result1.put("class", "classId");
    Map<String, Object> result2 = new HashMap<>();
    result2.put("object_id_identity", "test2");
    result2.put("class", "classId");
    result.addAll(Arrays.asList(result1, result2));
    doReturn(result)
        .when(jdbcTemplate)
        .queryForList(
            "SELECT DISTINCT acl_object_identity.object_id_identity, acl_class.class FROM acl_object_identity LEFT JOIN acl_class ON acl_object_identity.object_id_class = acl_class.id LEFT JOIN acl_entry ON acl_entry.acl_object_identity = acl_object_identity.id LEFT JOIN acl_sid ON acl_entry.sid = acl_sid.id WHERE acl_class.class = :classId ORDER BY acl_object_identity.object_id_identity ASC LIMIT :limit OFFSET :offset",
            paramMap);
    ObjectIdentity identity1 = mock(ObjectIdentity.class);
    ObjectIdentity identity2 = mock(ObjectIdentity.class);
    doReturn(identity1).when(entityHelper).getObjectIdentity("classId", "test1");
    doReturn(identity2).when(entityHelper).getObjectIdentity("classId", "test2");
    List<ObjectIdentity> expected = Arrays.asList(identity1, identity2);
    assertEquals(expected, objectIdentityService.getObjectIdentities("classId", 10, 20));
  }

  @Test
  void testGetObjectIdentities1() {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("classId", "classId");
    paramMap.put("sids", Collections.singletonList("user1"));
    paramMap.put("limit", 10);
    paramMap.put("offset", 20);

    List<Map<String, Object>> result = new ArrayList<>();
    Map<String, Object> result1 = new HashMap<>();
    result1.put("object_id_identity", "test1");
    result1.put("class", "classId");
    Map<String, Object> result2 = new HashMap<>();
    result2.put("object_id_identity", "test2");
    result2.put("class", "classId");
    result.addAll(Arrays.asList(result1, result2));
    doReturn(result)
        .when(jdbcTemplate)
        .queryForList(
            "SELECT DISTINCT acl_object_identity.object_id_identity, acl_class.class FROM acl_object_identity LEFT JOIN acl_class ON acl_object_identity.object_id_class = acl_class.id LEFT JOIN acl_entry ON acl_entry.acl_object_identity = acl_object_identity.id LEFT JOIN acl_sid ON acl_entry.sid = acl_sid.id WHERE acl_class.class = :classId AND acl_sid.sid IN (:sids) ORDER BY acl_object_identity.object_id_identity ASC LIMIT :limit OFFSET :offset",
            paramMap);
    Sid sid = new PrincipalSid("user1");
    ObjectIdentity identity1 = mock(ObjectIdentity.class);
    ObjectIdentity identity2 = mock(ObjectIdentity.class);
    doReturn(identity1).when(entityHelper).getObjectIdentity("classId", "test1");
    doReturn(identity2).when(entityHelper).getObjectIdentity("classId", "test2");
    List<ObjectIdentity> expected = Arrays.asList(identity1, identity2);
    assertEquals(
        expected, objectIdentityService.getObjectIdentities("classId", singleton(sid), 10, 20));
  }

  @Test
  void testGetObjectIdentities2() {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("classId", "classId");
    List<Map<String, Object>> result = new ArrayList<>();
    Map<String, Object> result1 = new HashMap<>();
    result1.put("object_id_identity", "test1");
    result1.put("class", "classId");
    Map<String, Object> result2 = new HashMap<>();
    result2.put("object_id_identity", "test2");
    result2.put("class", "classId");
    result.addAll(Arrays.asList(result1, result2));
    doReturn(new Integer(12))
        .when(jdbcTemplate)
        .queryForObject(
            "SELECT COUNT( DISTINCT acl_object_identity.object_id_identity) FROM acl_object_identity LEFT JOIN acl_class ON acl_object_identity.object_id_class = acl_class.id LEFT JOIN acl_entry ON acl_entry.acl_object_identity = acl_object_identity.id LEFT JOIN acl_sid ON acl_entry.sid = acl_sid.id WHERE acl_class.class = :classId",
            paramMap,
            Integer.class);
    Map<String, Object> paramMap2 = new HashMap<>();
    paramMap2.put("classId", "classId");
    doReturn(result)
        .when(jdbcTemplate)
        .queryForList(
            "SELECT DISTINCT acl_object_identity.object_id_identity, acl_class.class FROM acl_object_identity LEFT JOIN acl_class ON acl_object_identity.object_id_class = acl_class.id LEFT JOIN acl_entry ON acl_entry.acl_object_identity = acl_object_identity.id LEFT JOIN acl_sid ON acl_entry.sid = acl_sid.id WHERE acl_class.class = :classId ORDER BY acl_object_identity.object_id_identity ASC",
            paramMap2);
    ObjectIdentity identity1 = mock(ObjectIdentity.class);
    ObjectIdentity identity2 = mock(ObjectIdentity.class);
    doReturn(identity1).when(entityHelper).getObjectIdentity("classId", "test1");
    doReturn(identity2).when(entityHelper).getObjectIdentity("classId", "test2");
    List<ObjectIdentity> expected = Arrays.asList(identity1, identity2);
    assertEquals(expected, objectIdentityService.getObjectIdentities("classId"));
  }

  @Test
  void testGetObjectIdentities3() {
    List<Map<String, Object>> result = new ArrayList<>();
    Map<String, Object> result1 = new HashMap<>();
    result1.put("object_id_identity", "test1");
    result1.put("class", "classId");
    Map<String, Object> result2 = new HashMap<>();
    result2.put("object_id_identity", "test2");
    result2.put("class", "classId");
    result.addAll(Arrays.asList(result1, result2));

    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("classId", "classId");
    paramMap.put("sids", Collections.singletonList("user1"));
    doReturn(new Integer(12))
        .when(jdbcTemplate)
        .queryForObject(
            "SELECT COUNT( DISTINCT acl_object_identity.object_id_identity) FROM acl_object_identity LEFT JOIN acl_class ON acl_object_identity.object_id_class = acl_class.id LEFT JOIN acl_entry ON acl_entry.acl_object_identity = acl_object_identity.id LEFT JOIN acl_sid ON acl_entry.sid = acl_sid.id WHERE acl_class.class = :classId AND acl_sid.sid IN (:sids)",
            paramMap,
            Integer.class);
    Map<String, Object> paramMap2 = new HashMap<>();
    paramMap2.put("classId", "classId");
    paramMap2.put("sids", Collections.singletonList("user1"));
    doReturn(result)
        .when(jdbcTemplate)
        .queryForList(
            "SELECT DISTINCT acl_object_identity.object_id_identity, acl_class.class FROM acl_object_identity LEFT JOIN acl_class ON acl_object_identity.object_id_class = acl_class.id LEFT JOIN acl_entry ON acl_entry.acl_object_identity = acl_object_identity.id LEFT JOIN acl_sid ON acl_entry.sid = acl_sid.id WHERE acl_class.class = :classId AND acl_sid.sid IN (:sids) ORDER BY acl_object_identity.object_id_identity ASC",
            paramMap2);
    Sid sid = new PrincipalSid("user1");
    ObjectIdentity identity1 = mock(ObjectIdentity.class);
    ObjectIdentity identity2 = mock(ObjectIdentity.class);
    doReturn(identity1).when(entityHelper).getObjectIdentity("classId", "test1");
    doReturn(identity2).when(entityHelper).getObjectIdentity("classId", "test2");
    List<ObjectIdentity> expected = Arrays.asList(identity1, identity2);
    assertEquals(expected, objectIdentityService.getObjectIdentities("classId", singleton(sid)));
  }
}
