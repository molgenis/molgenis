package org.molgenis.data.migrate.version;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.jdbc.core.JdbcTemplate;

class Step47AddEntityDetailTemplateTest extends AbstractMockitoTest {

  @Mock private JdbcTemplate jdbcTemplate;
  private Step47AddEntityDetailTemplate step47;

  @BeforeEach
  void setUpBeforeEach() {
    step47 = new Step47AddEntityDetailTemplate(jdbcTemplate);
  }

  @Test
  void upgrade() {
    step47.upgrade();
    verify(jdbcTemplate)
        .execute(
            "ALTER TABLE public.\"sys_ts_DataExplorerEntitySettings#a7f151bf\" ADD detail_template text;");
  }
}
