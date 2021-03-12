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

public class Step47AddEntityDetailTemplate extends MolgenisUpgrade {

  private static final Logger LOG = LoggerFactory.getLogger(Step47AddEntityDetailTemplate.class);

  private final JdbcTemplate jdbcTemplate;

  public Step47AddEntityDetailTemplate(DataSource dataSource) {
    this(new JdbcTemplate(dataSource));
  }

  Step47AddEntityDetailTemplate(JdbcTemplate jdbcTemplate) {
    super(46, 47);
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  @Override
  public void upgrade() {
    LOG.debug("Adding custom entity details column");
    try {
      addCustumEntityDetailTemplateColumn();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info("Added custom entity details column");
  }

  private void addCustumEntityDetailTemplateColumn() throws IOException {
    String sql = ResourceUtils.getString("step47-addEntityDetailTemplate.sql");
    jdbcTemplate.execute(sql);
  }
}
