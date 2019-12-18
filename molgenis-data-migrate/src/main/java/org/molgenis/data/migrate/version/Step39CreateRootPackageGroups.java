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

public class Step39CreateRootPackageGroups extends MolgenisUpgrade {
  private static final Logger LOG = LoggerFactory.getLogger(Step39CreateRootPackageGroups.class);

  private final DataSource dataSource;

  public Step39CreateRootPackageGroups(DataSource dataSource) {
    super(38, 39);
    this.dataSource = requireNonNull(dataSource);
  }

  @Override
  public void upgrade() {
    LOG.debug("Creating groups for root packages without associated group ...");
    try {
      createRootPackageGroups();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info("Created groups for root packages without associated group");
  }

  private void createRootPackageGroups() throws IOException {
    String sql = ResourceUtils.getString("step39-createRootPackageGroups.sql");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(sql);
  }
}
