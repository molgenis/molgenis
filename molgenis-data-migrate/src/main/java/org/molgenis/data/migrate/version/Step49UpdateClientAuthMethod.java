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

public class Step49UpdateClientAuthMethod extends MolgenisUpgrade {

  private static final Logger LOG = LoggerFactory.getLogger(Step49UpdateClientAuthMethod.class);

  private final JdbcTemplate jdbcTemplate;

  public Step49UpdateClientAuthMethod(DataSource dataSource) {
    this(new JdbcTemplate(dataSource));
  }

  Step49UpdateClientAuthMethod(JdbcTemplate jdbcTemplate) {
    super(48, 49);
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  @Override
  public void upgrade() {
    LOG.debug("Update Client authentication method attribute...");
    try {
      updateClientAuthMethod();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info("Updated Client authentication method attribute...");
  }

  private void updateClientAuthMethod() throws IOException {
    String sql = ResourceUtils.getString("step49-updateClientAuthMethod.sql");
    jdbcTemplate.execute(sql);
  }
}
