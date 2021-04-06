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

public class Step47AddMaxLength extends MolgenisUpgrade {

  private static final Logger LOG = LoggerFactory.getLogger(Step47AddMaxLength.class);

  private final JdbcTemplate jdbcTemplate;

  public Step47AddMaxLength(DataSource dataSource) {
    this(new JdbcTemplate(dataSource));
  }

  Step47AddMaxLength(JdbcTemplate jdbcTemplate) {
    super(46, 47);
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  @Override
  public void upgrade() {
    LOG.debug("Add maxLength attribute...");
    try {
      addMaxLengthAttribute();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info("Added maxLength attribute.");
  }

  private void addMaxLengthAttribute() throws IOException {
    String sql = ResourceUtils.getString("step47-addMaxLength.sql");
    jdbcTemplate.execute(sql);
  }
}
