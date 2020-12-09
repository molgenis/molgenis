package org.molgenis.data.migrate.version;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.jdbc.core.JdbcTemplate;

class Step45RemoveDanglingSidsTest extends AbstractMockitoTest {

  @Mock private JdbcTemplate jdbcTemplate;
  private Step45RemoveDanglingSids step45RemoveDanglingSids;

  @BeforeEach
  void setUpBeforeEach() {
    step45RemoveDanglingSids = new Step45RemoveDanglingSids(jdbcTemplate);
  }

  @Test
  void upgrade() {
    step45RemoveDanglingSids.upgrade();
    verify(jdbcTemplate)
        .execute(
            "DELETE\n"
                + "FROM acl_sid AS sids\n"
                + "WHERE id IN (SELECT id\n"
                + "             FROM acl_sid\n"
                + "             WHERE NOT EXISTS(SELECT *\n"
                + "                              FROM \"sys_sec_Role#b6639604\" as roles\n"
                + "                              WHERE CONCAT('ROLE_', roles.name) = sids.sid)\n"
                + "               AND NOT sids.principal);");
  }
}
