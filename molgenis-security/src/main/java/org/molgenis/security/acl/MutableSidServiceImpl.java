package org.molgenis.security.acl;

import static java.util.Objects.requireNonNull;

import org.molgenis.security.core.SidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.model.Sid;
import org.springframework.transaction.annotation.Transactional;

public class MutableSidServiceImpl implements MutableSidService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MutableSidServiceImpl.class);

  private static final String SQL_DELETE_FROM_ACL_SID = "delete from acl_sid where sid=?";

  private final JdbcTemplate jdbcTemplate;

  public MutableSidServiceImpl(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  @Transactional
  @Override
  public void deleteSid(Sid sid) {
    String sidValue = SidUtils.getStringValue(sid);
    LOGGER.debug("Delete ACL Sid {}.", sidValue);
    jdbcTemplate.update(SQL_DELETE_FROM_ACL_SID, sidValue);
  }
}
