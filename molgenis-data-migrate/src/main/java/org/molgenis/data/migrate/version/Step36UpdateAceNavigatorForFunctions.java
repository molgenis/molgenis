package org.molgenis.data.migrate.version;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.sql.DataSource;
import org.molgenis.data.migrate.framework.MolgenisUpgrade;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step36UpdateAceNavigatorForFunctions extends MolgenisUpgrade {
  private static final Logger LOG =
      LoggerFactory.getLogger(Step36UpdateAceNavigatorForFunctions.class);

  private final DataSource dataSource;

  public Step36UpdateAceNavigatorForFunctions(DataSource dataSource) {
    super(35, 36);
    this.dataSource = requireNonNull(dataSource);
  }

  @Override
  public void upgrade() {
    LOG.debug("Updating ace's for navigator functionality ...");
    try {
      createFunction();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    Map<String, Integer> permissions = new HashMap<>();
    // mask 4 == read, 8 == WRITE
    permissions.put("sys_job_ResourceDownloadJobExecution", 8);
    permissions.put("sys_FileMeta", 8);
    updateAces("ROLE_EDITOR", permissions);

    dropFunction();
    LOG.info("Updated ace's for navigator functionality.");
  }

  private void updateAces(String role, Map<String, Integer> permissions) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    // params for the function: role VARCHAR, identifier VARCHAR, mask INTEGER
    for (Entry<String, Integer> permission : permissions.entrySet()) {
      jdbcTemplate.execute(
          String.format(
              "SELECT update_acl_entry('%s','%s',%s)",
              role, permission.getKey(), permission.getValue()));
    }
  }

  private void createFunction() throws IOException {
    String sql = ResourceUtils.getString("update_acl_entry.sql");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(sql);
  }

  private void dropFunction() {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute(
        "DROP FUNCTION update_acl_entry(role VARCHAR, object_identifier VARCHAR, new_mask INTEGER);");
  }
}
