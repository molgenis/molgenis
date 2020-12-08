package org.molgenis.data.migrate.version;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.jdbc.core.JdbcTemplate;

class Step44CascadeDeleteSidsTest extends AbstractMockitoTest {

  @Mock private JdbcTemplate jdbcTemplate;
  private Step44CascadeDeleteSids step44CascadeDeleteSids;

  @BeforeEach
  void setUpBeforeEach() {
    step44CascadeDeleteSids = new Step44CascadeDeleteSids(jdbcTemplate);
  }

  @Test
  void upgrade() {
    step44CascadeDeleteSids.upgrade();
    verify(jdbcTemplate)
        .execute(
            "ALTER TABLE acl_entry\n"
                + "    DROP CONSTRAINT foreign_fk_5,\n"
                + "    ADD CONSTRAINT foreign_fk_5\n"
                + "        FOREIGN KEY (sid)\n"
                + "            REFERENCES acl_sid (id)\n"
                + "            ON DELETE CASCADE;");
  }
}
