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

public class Step36EnableDataRowEdit extends MolgenisUpgrade {
  private static final Logger LOG = LoggerFactory.getLogger(Step36EnableDataRowEdit.class);

  private final DataSource dataSource;

  public Step36EnableDataRowEdit(DataSource dataSource) {
    super(35, 36);
    this.dataSource = requireNonNull(dataSource);
  }

  @Override
  public void upgrade() {
    LOG.debug("Setting sys_set_dataexplorer.use_vue_data_row_edit to true' ...");
    try {
      enableDataRowEdit();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info("sys_set_dataexplorer.use_vue_data_row_edit has been set to true");
  }

  private void enableDataRowEdit() throws IOException {
    String sql = ResourceUtils.getString("step36-enableDataRowEdit.sql");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(sql);
  }
}
