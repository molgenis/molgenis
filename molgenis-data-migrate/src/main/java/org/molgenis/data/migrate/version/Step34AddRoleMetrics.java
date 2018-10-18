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

public class Step34AddRoleMetrics extends MolgenisUpgrade {
  private static final Logger LOG = LoggerFactory.getLogger(Step34AddRoleMetrics.class);

  private final DataSource dataSource;

  public Step34AddRoleMetrics(DataSource dataSource) {
    super(33, 34);
    this.dataSource = requireNonNull(dataSource);
  }

  @Override
  public void upgrade() {
    LOG.debug("Adding ROLE_METRICS to ROLE_SU...");
    try {
      addRoleMetrics();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info("Added ROLE_METRICS to ROLE_SU");
  }

  private void addRoleMetrics() throws IOException {
    String sql = ResourceUtils.getString("step34-roleMetrics.sql");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(sql);
  }
}
