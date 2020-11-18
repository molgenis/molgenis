package org.molgenis.data.migrate.version;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import javax.sql.DataSource;
import org.molgenis.data.migrate.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step43SetUsernameAttributeName extends MolgenisUpgrade {
  private static final Logger LOG = LoggerFactory.getLogger(Step43SetUsernameAttributeName.class);
  private final JdbcTemplate jdbcTemplate;

  public Step43SetUsernameAttributeName(DataSource dataSource) {
    this(new JdbcTemplate(dataSource));
  }

  Step43SetUsernameAttributeName(JdbcTemplate jdbcTemplate) {
    super(42, 43);
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  @Override
  public void upgrade() {
    LOG.debug("Updating userNameAttributeName in OIDC clients...");
    var count = updateUsernameAttributeName();
    LOG.info("Updated userNameAttributeName in {} existing OIDC client(s).", count);
  }

  private long updateUsernameAttributeName() {
    jdbcTemplate.execute(
        "UPDATE \"sys_sec_oidc_OidcClient#3e7b1b4d\" "
            + "SET \"userNameAttributeName\" = 'email';");
    return Optional.ofNullable(
            jdbcTemplate.queryForObject(
                "SELECT count(*) from \"sys_sec_oidc_OidcClient#3e7b1b4d\";", Long.class))
        .orElseThrow();
  }
}
