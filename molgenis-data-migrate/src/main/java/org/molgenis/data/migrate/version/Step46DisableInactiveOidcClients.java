package org.molgenis.data.migrate.version;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import javax.sql.DataSource;
import org.molgenis.data.migrate.framework.MolgenisUpgrade;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step46DisableInactiveOidcClients extends MolgenisUpgrade {

  private static final Logger LOG = LoggerFactory.getLogger(Step46DisableInactiveOidcClients.class);

  private final JdbcTemplate jdbcTemplate;

  public Step46DisableInactiveOidcClients(DataSource dataSource) {
    this(new JdbcTemplate(dataSource));
  }

  Step46DisableInactiveOidcClients(JdbcTemplate jdbcTemplate) {
    super(45, 46);
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  @Override
  public void upgrade() {
    LOG.debug("Disabling inactive OIDC clients");
    try {
      updateAuthenticationSettings();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info("Disabled inactive OIDC clients");
  }

  private void updateAuthenticationSettings() throws IOException {
    String sql = ResourceUtils.getString("step46-disableInactiveOidcClients.sql");
    jdbcTemplate.execute(sql);
  }
}
