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

public class Step44CascadeDeleteSids extends MolgenisUpgrade {

  private static final Logger LOG = LoggerFactory.getLogger(Step44CascadeDeleteSids.class);

  private final JdbcTemplate jdbcTemplate;

  public Step44CascadeDeleteSids(DataSource dataSource) {
    this(new JdbcTemplate(dataSource));
  }

  Step44CascadeDeleteSids(JdbcTemplate jdbcTemplate) {
    super(43, 44);
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  @Override
  public void upgrade() {
    LOG.debug("Adding cascading delete constraint for ACL Sids");
    try {
      addCascadeDeleteConstraint();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info("Added cascading delete constraint for ACL Sids");
  }

  private void addCascadeDeleteConstraint() throws IOException {
    String sql = ResourceUtils.getString("step44-cascadeDeleteSidConstraint.sql");
    jdbcTemplate.execute(sql);
  }
}
