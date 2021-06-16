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

public class Step48RemoveMagmaPlaceholderLocalization extends MolgenisUpgrade {

  private static final Logger LOG =
      LoggerFactory.getLogger(Step48RemoveMagmaPlaceholderLocalization.class);

  private final JdbcTemplate jdbcTemplate;

  public Step48RemoveMagmaPlaceholderLocalization(DataSource dataSource) {
    this(new JdbcTemplate(dataSource));
  }

  Step48RemoveMagmaPlaceholderLocalization(JdbcTemplate jdbcTemplate) {
    super(47, 48);
    this.jdbcTemplate = requireNonNull(jdbcTemplate);
  }

  @Override
  public void upgrade() {
    LOG.debug("Removing outdated localization keys that refer to MagmaScript...");
    try {
      removeKeys();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    LOG.info("Removed outdated localization keys that refer to MagmaScript.");
  }

  private void removeKeys() throws IOException {
    var sql = ResourceUtils.getString("step48-removeMagmaPlaceholderLocalization.sql");
    jdbcTemplate.execute(sql);
  }
}
