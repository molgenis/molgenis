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

public class Step38AddAnonymouseRoleToDb extends MolgenisUpgrade {
  private static final Logger LOG = LoggerFactory.getLogger(Step33UpdateForeignKeyDeferred.class);

  private final DataSource dataSource;

  public Step38AddAnonymouseRoleToDb(DataSource dataSource) {
    super(37, 38);
    this.dataSource = requireNonNull(dataSource);
  }

  @Override
  public void upgrade() {
    LOG.debug("Adding role ANONYMOUSE to roles tabel and including it for the USER role");
    try {
      addAnonymouseRoleToDb();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info("Added role ANONYMOUSE to roles tabel and included it for the USER role");
  }

  private void addAnonymouseRoleToDb() throws IOException {
    String sql = ResourceUtils.getString("step38-addAnonymousRole.sql");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(sql);
  }
}
