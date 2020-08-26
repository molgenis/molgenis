package org.molgenis.data.migrate.version;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.molgenis.data.migrate.framework.MolgenisUpgrade;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step42RemoveFormsUrlRow extends MolgenisUpgrade {
  private static final Logger LOG = LoggerFactory.getLogger(Step42RemoveFormsUrlRow.class);
  private final JdbcTemplate jdbcTemplate;

  Step42RemoveFormsUrlRow(JdbcTemplate jdbcTemplate) {
    super(41, 42);
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  @Override
  public void upgrade() {
    LOG.debug("Remove form_not_a_valid_url row");
    removeFromsUrlMsg();
    LOG.info("row (id = 'form_not_a_valid_url') has been removed from sys_L10nString");
  }

  private void removeFromsUrlMsg() {
    String sql;
    try {
      sql = ResourceUtils.getString("step42-removeFormsUrlMsg.sql");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    jdbcTemplate.execute(sql);
  }
}
