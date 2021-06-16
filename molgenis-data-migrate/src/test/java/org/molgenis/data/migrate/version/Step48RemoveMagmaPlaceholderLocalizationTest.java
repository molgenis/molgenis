package org.molgenis.data.migrate.version;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.jdbc.core.JdbcTemplate;

class Step48RemoveMagmaPlaceholderLocalizationTest extends AbstractMockitoTest {
  @Mock private JdbcTemplate jdbcTemplate;
  private Step48RemoveMagmaPlaceholderLocalization step48;

  @BeforeEach
  void setUpBeforeEach() {
    step48 = new Step48RemoveMagmaPlaceholderLocalization(jdbcTemplate);
  }

  @Test
  void upgrade() {
    step48.upgrade();
    verify(jdbcTemplate)
        .execute(
            "DELETE\n"
                + "FROM public.\"sys_L10nString#95a21e09\"\n"
                + "WHERE id in ('attribute-edit-form-computed-expression-placeholder',\n"
                + "             'attribute-edit-form-nullable-expression-placeholder',\n"
                + "             'attribute-edit-form-visible-expression-placeholder',\n"
                + "             'attribute-edit-form-validation-expression-placeholder');");
  }
}
