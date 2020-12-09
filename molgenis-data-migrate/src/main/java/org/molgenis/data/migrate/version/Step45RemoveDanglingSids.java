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

public class Step45RemoveDanglingSids extends MolgenisUpgrade {

  private static final Logger LOG = LoggerFactory.getLogger(Step45RemoveDanglingSids.class);

  private final JdbcTemplate jdbcTemplate;

  public Step45RemoveDanglingSids(DataSource dataSource) {
    this(new JdbcTemplate(dataSource));
  }

  Step45RemoveDanglingSids(JdbcTemplate jdbcTemplate) {
    super(44, 45);
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  @Override
  public void upgrade() {
    LOG.debug("Cleaning dirty ACL tables: removing dangling Sids and associated ACEs");
    try {
      removeDanglingSids();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info("Cleaned dirty ACL tables: removed dangling Sids and associated ACEs");
  }

  private void removeDanglingSids() throws IOException {
    String sql = ResourceUtils.getString("step45-removeDanglingSids.sql");
    jdbcTemplate.execute(sql);
  }
}
