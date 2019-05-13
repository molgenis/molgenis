package org.molgenis.security.acl;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

public class ObjectIdentityServiceImpl implements ObjectIdentityService {

  private static final String SQL_COUNT_OBJECT_IDENTITIES =
      "SELECT COUNT( DISTINCT acl_object_identity.object_id_identity)"
          + " FROM acl_object_identity LEFT JOIN acl_class ON acl_object_identity.object_id_class = acl_class.id LEFT JOIN acl_entry ON acl_entry.acl_object_identity = acl_object_identity.id LEFT JOIN acl_sid ON acl_entry.sid = acl_sid.id";
  private static final String SQL_SELECT_OBJECT_IDENTITIES =
      "SELECT DISTINCT acl_object_identity.object_id_identity, acl_class.class"
          + " FROM acl_object_identity LEFT JOIN acl_class ON acl_object_identity.object_id_class = acl_class.id LEFT JOIN acl_entry ON acl_entry.acl_object_identity = acl_object_identity.id LEFT JOIN acl_sid ON acl_entry.sid = acl_sid.id";

  private static final String WHERE_CLASS = " WHERE acl_class.class = :classId";
  private static final String AND_SID = " AND acl_sid.sid IN (:sids)";
  private static final String ORDER_BY = " ORDER BY acl_object_identity.object_id_identity ASC";
  private static final String PAGE = " LIMIT :limit OFFSET :offset";

  public static final Integer MAX_RESULTS = 10000;
  public static final String OBJECT_ID_IDENTITY = "object_id_identity";
  public static final String CLASS = "class";

  public static final String CLASS_ID = "classId";
  public static final String LIMIT = "limit";
  public static final String OFFSET = "offset";
  public static final String SIDS = "sids";

  private JdbcTemplate jdbcTemplate;
  private NamedParameterJdbcTemplate namedJdbcTemplate;

  public ObjectIdentityServiceImpl(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  // for tests
  ObjectIdentityServiceImpl(NamedParameterJdbcTemplate jdbcTemplate) {
    this.namedJdbcTemplate = requireNonNull(jdbcTemplate);
  }

  private NamedParameterJdbcTemplate getTemplate() {
    if (namedJdbcTemplate == null) {
      namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }
    return namedJdbcTemplate;
  }

  @Override
  public List<ObjectIdentity> getObjectIdentities(String classId) {
    int rowCount = getNrOfObjectIdentities(classId);
    if (rowCount > MAX_RESULTS) {
      throw new UnsupportedOperationException(
          "Unfiltered select on object identities not supported for classes with more than '"
              + MAX_RESULTS
              + "' rows.");
    }
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(CLASS_ID, classId);
    List<Map<String, Object>> result =
        getTemplate().queryForList(SQL_SELECT_OBJECT_IDENTITIES + WHERE_CLASS + ORDER_BY, paramMap);
    return parseToStringList(result);
  }

  @Override
  public Integer getNrOfObjectIdentities(String classId) {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(CLASS_ID, classId);
    return getTemplate()
        .queryForObject(SQL_COUNT_OBJECT_IDENTITIES + WHERE_CLASS, paramMap, Integer.class);
  }

  @Override
  public Integer getNrOfObjectIdentities(String classId, Set<Sid> sids) {
    List<String> sidStrings = getSidIdentifiers(sids);
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(SIDS, sidStrings);
    paramMap.put(CLASS_ID, classId);
    return getTemplate()
        .queryForObject(
            SQL_COUNT_OBJECT_IDENTITIES + WHERE_CLASS + AND_SID, paramMap, Integer.class);
  }

  @Override
  public List<ObjectIdentity> getObjectIdentities(String classId, Integer limit, Integer offset) {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(CLASS_ID, classId);
    paramMap.put(LIMIT, limit);
    paramMap.put(OFFSET, offset);
    List<Map<String, Object>> result =
        getTemplate()
            .queryForList(SQL_SELECT_OBJECT_IDENTITIES + WHERE_CLASS + ORDER_BY + PAGE, paramMap);
    return parseToStringList(result);
  }

  @Override
  public List<ObjectIdentity> getObjectIdentities(
      String classId, Set<Sid> sids, Integer limit, Integer offset) {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put(SIDS, getSidIdentifiers(sids));
    paramMap.put(CLASS_ID, classId);
    paramMap.put(LIMIT, limit);
    paramMap.put(OFFSET, offset);
    List<Map<String, Object>> result =
        getTemplate()
            .queryForList(
                SQL_SELECT_OBJECT_IDENTITIES + WHERE_CLASS + AND_SID + ORDER_BY + PAGE, paramMap);
    return parseToStringList(result);
  }

  @Override
  public List<ObjectIdentity> getObjectIdentities(String classId, Set<Sid> sids) {

    int rowCount = getNrOfObjectIdentities(classId, sids);
    if (rowCount > MAX_RESULTS) {
      throw new UnsupportedOperationException(
          "Unfiltered select on object identities not supported for classes with more than '"
              + MAX_RESULTS
              + "' rows.");
    }
    List<Map<String, Object>> result;
    if (sids.isEmpty()) {
      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put(CLASS_ID, classId);
      result =
          getTemplate()
              .queryForList(SQL_SELECT_OBJECT_IDENTITIES + WHERE_CLASS + ORDER_BY, paramMap);
    } else {
      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put(SIDS, getSidIdentifiers(sids));
      paramMap.put(CLASS_ID, classId);

      result =
          getTemplate()
              .queryForList(
                  SQL_SELECT_OBJECT_IDENTITIES + WHERE_CLASS + AND_SID + ORDER_BY, paramMap);
    }
    return parseToStringList(result);
  }

  private List<String> getSidIdentifiers(Set<Sid> sids) {
    List<String> sidStrings = new ArrayList<>();
    for (Sid sid : sids) {
      sidStrings.add(getSidString(sid));
    }
    return sidStrings;
  }

  private List<ObjectIdentity> parseToStringList(List<Map<String, Object>> result) {
    return result.stream().map(this::parseRow).collect(Collectors.toList());
  }

  private ObjectIdentity parseRow(Map<String, Object> row) {
    return new ObjectIdentityImpl(
        row.get(CLASS).toString(), (Serializable) row.get(OBJECT_ID_IDENTITY));
  }

  private String getSidString(Sid sid) {
    if (sid instanceof PrincipalSid) {
      return ((PrincipalSid) sid).getPrincipal();
    } else if (sid instanceof GrantedAuthoritySid) {
      return ((GrantedAuthoritySid) sid).getGrantedAuthority();
    }
    throw new IllegalArgumentException(
        "Sid type should always be either PrincipalSid or GrantedAuthoritySid");
  }
}
