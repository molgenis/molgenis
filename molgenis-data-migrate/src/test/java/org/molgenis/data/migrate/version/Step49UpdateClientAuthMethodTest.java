package org.molgenis.data.migrate.version;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.jdbc.core.JdbcTemplate;

class Step49UpdateClientAuthMethodTest extends AbstractMockitoTest {

  @Mock private JdbcTemplate jdbcTemplate;
  private Step49UpdateClientAuthMethod step49;

  @BeforeEach
  void setUpBeforeEach() {
    step49 = new Step49UpdateClientAuthMethod(jdbcTemplate);
  }

  @Test
  void upgrade() {
    step49.upgrade();
    verify(jdbcTemplate)
        .execute(
            "UPDATE \"sys_sec_oidc_OidcClient#3e7b1b4d\"\n"
                + "SET \"clientAuthenticationMethod\" =\n"
                + "    CASE\n"
                + "        WHEN \"clientAuthenticationMethod\" in ('post', 'client_secret_post') THEN 'client_secret_post'\n"
                + "        WHEN \"clientAuthenticationMethod\" = 'none' THEN 'none'\n"
                + "        WHEN \"clientAuthenticationMethod\" IS NULL THEN NULL\n"
                + "        ELSE 'client_secret_basic'\n"
                + "    END;");
  }
}
