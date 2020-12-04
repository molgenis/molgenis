package org.molgenis.security.acl;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.model.Sid;

class MutableSidServiceImplTest extends AbstractMockitoTest {

  @Mock private JdbcTemplate jdbcTemplate;
  private MutableSidServiceImpl mutableSidService;

  @BeforeEach
  void setUpBeforeMethod() {
    mutableSidService = new MutableSidServiceImpl(jdbcTemplate);
  }

  @Test
  void testDeleteSid() {
    Sid sid = new GrantedAuthoritySid("ROLE_test_VIEWER");
    mutableSidService.deleteSid(sid);
    verify(jdbcTemplate).update("delete from acl_sid where sid=?", "ROLE_test_VIEWER");
  }
}
