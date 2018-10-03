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

public class Step33UpdateForeignKeyDeferred extends MolgenisUpgrade {
  private static final Logger LOG = LoggerFactory.getLogger(Step33UpdateForeignKeyDeferred.class);

  private final DataSource dataSource;

  public Step33UpdateForeignKeyDeferred(DataSource dataSource) {
    super(32, 33);
    this.dataSource = requireNonNull(dataSource);
  }

  @Override
  public void upgrade() {
    LOG.debug("Updating foreign keys to 'DEFERRABLE INITIALLY DEFERRED' ...");
    try {
      updateForeignKeys();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info("Updated foreign keys to 'DEFERRABLE INITIALLY DEFERRED'");
  }

  private void updateForeignKeys() throws IOException {
    String sql = ResourceUtils.getString("step33-deferredForeignKeys.sql");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(sql);
  }
}
