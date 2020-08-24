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

public class Step42ReplaceFormsUrlRow extends MolgenisUpgrade {
  private static final Logger LOG = LoggerFactory.getLogger(Step42ReplaceFormsUrlRow.class);
  private final DataSource dataSource;

  public Step42ReplaceFormsUrlRow(DataSource dataSource) {
    super(41, 42);
    this.dataSource = requireNonNull(dataSource);
  }

  @Override
  public void upgrade() {
    LOG.debug("Replace forms form_not_a_valid_url row with form_not_a_valid_hyperlink row");
    try {
      replaceFormsL10nRow();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info(
        "Forms 'form_not_a_valid_url' row has been removed and replaced with 'form_not_a_valid_hyperlink' row");
  }

  private void replaceFormsL10nRow() throws IOException {
    String sql = ResourceUtils.getString("step42-replaceFormsUrlMsg.sql");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(sql);
  }
}
