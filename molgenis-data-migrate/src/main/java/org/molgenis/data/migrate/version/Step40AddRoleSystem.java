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

public class Step40AddRoleSystem extends MolgenisUpgrade {
  private static final Logger LOG = LoggerFactory.getLogger(Step40AddRoleSystem.class);

  private final JdbcTemplate jdbcTemplate;

  public Step40AddRoleSystem(DataSource dataSource) {
    this(new JdbcTemplate(dataSource));
  }

  Step40AddRoleSystem(JdbcTemplate jdbcTemplate) {
    super(39, 40);
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  @Override
  public void upgrade() {
    LOG.debug("Adding ROLE_SYSTEM to ROLE_SU...");
    addRoleSystem();
    LOG.info("Added ROLE_SYSTEM to ROLE_SU");
  }

  private void addRoleSystem() {
    String sql;
    try {
      sql = ResourceUtils.getString("step40-roleSystem.sql");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    jdbcTemplate.execute(sql);
  }
}
