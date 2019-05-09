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

public class Step35UpdateAclSystemSid extends MolgenisUpgrade {
  private static final Logger LOG = LoggerFactory.getLogger(Step35UpdateAclSystemSid.class);

  private final DataSource dataSource;

  public Step35UpdateAclSystemSid(DataSource dataSource) {
    super(34, 35);
    this.dataSource = requireNonNull(dataSource);
  }

  @Override
  public void upgrade() {
    LOG.debug("Updating acl_sid 'principal SYSTEM' to non-principal 'ROLE_SYSTEM' ...");
    try {
      updateAclSystemSid();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info("Updated acl_sid 'principal SYSTEM' to non-principal 'ROLE_SYSTEM'");
  }

  private void updateAclSystemSid() throws IOException {
    String sql = ResourceUtils.getString("step35-aclSystemSid.sql");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(sql);
  }
}
