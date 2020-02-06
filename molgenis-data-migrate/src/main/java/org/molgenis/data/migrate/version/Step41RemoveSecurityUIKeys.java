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

public class Step41RemoveSecurityUIKeys extends MolgenisUpgrade {
  private static final Logger LOG = LoggerFactory.getLogger(Step41RemoveSecurityUIKeys.class);

  private final JdbcTemplate jdbcTemplate;

  public Step41RemoveSecurityUIKeys(DataSource dataSource) {
    this(new JdbcTemplate(dataSource));
  }

  Step41RemoveSecurityUIKeys(JdbcTemplate jdbcTemplate) {
    super(40, 41);
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  @Override
  public void upgrade() {
    LOG.debug("Removing unused Security Manager UI l10n keys");
    addRoleSystem();
    LOG.info("Done removing unused Security Manager UI l10n keys");
  }

  private void addRoleSystem() {
    String sql;
    try {
      sql = ResourceUtils.getString("step41-removeSecurityUIKeys.sql");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    jdbcTemplate.execute(sql);
  }
}
