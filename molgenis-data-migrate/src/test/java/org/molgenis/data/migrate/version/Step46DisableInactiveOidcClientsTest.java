package org.molgenis.data.migrate.version;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.jdbc.core.JdbcTemplate;

class Step46DisableInactiveOidcClientsTest extends AbstractMockitoTest {

  @Mock private JdbcTemplate jdbcTemplate;
  private Step46DisableInactiveOidcClients step46;

  @BeforeEach
  void setUpBeforeEach() {
    step46 = new Step46DisableInactiveOidcClients(jdbcTemplate);
  }

  @Test
  void upgrade() {
    step46.upgrade();
    verify(jdbcTemplate)
        .execute("DO $$\n"
            + "DECLARE\n"
            + "    settings public.\"sys_set_auth#98c4c015\"%ROWTYPE;\n"
            + "\n"
            + "BEGIN\n"
            + "    SELECT *\n"
            + "    INTO settings\n"
            + "    FROM \"sys_set_auth#98c4c015\"\n"
            + "    WHERE signup IS TRUE;\n"
            + "\n"
            + "    IF FOUND THEN\n"
            + "        TRUNCATE public.\"sys_set_auth#98c4c015_oidcClients\";\n"
            + "    END IF;\n"
            + "END $$");
  }
}
