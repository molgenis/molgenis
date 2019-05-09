package org.molgenis.data.migrate.version;

import static java.util.Objects.requireNonNull;

import javax.sql.DataSource;
import org.molgenis.data.migrate.framework.MolgenisUpgrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class Step37AddSettingsPluginToMenu extends MolgenisUpgrade {

  private static final Logger LOG = LoggerFactory.getLogger(Step37AddSettingsPluginToMenu.class);

  private final DataSource dataSource;

  public Step37AddSettingsPluginToMenu(DataSource dataSource) {
    super(36, 37);
    this.dataSource = requireNonNull(dataSource);
  }

  @Override
  public void upgrade() {
    LOG.debug("Adding the settings plugin to the menu ...");
    addSettingsPluginToMenu();
  }

  private void addSettingsPluginToMenu() {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    String menuString = getMenuString(jdbcTemplate);

    if (hasSettingsPlugin(menuString)) {
      LOG.info("Settings plugin already in the menu. No further action required.");
    } else {
      String newMenu = replaceOldPlugin(menuString);
      updateMenuInDatabase(jdbcTemplate, newMenu);
      LOG.info("Added settings plugin to the menu.");
    }
  }

  private static boolean hasSettingsPlugin(String menuString) {
    return menuString.contains("\"type\":\"plugin\",\"id\":\"settings\"");
  }

  /**
   * Replaces the old settings plugin with the new settings plugin. Doing it this way makes sure the
   * position of the plugin stays consistent. If the menu doesn't contain a settings plugin, the new
   * plugin won't be added.
   */
  private static String replaceOldPlugin(String menuString) {
    return menuString.replace(
        "\"type\":\"plugin\",\"id\":\"settingsmanager\"",
        "\"type\":\"plugin\",\"id\":\"settings\"");
  }

  private String getMenuString(JdbcTemplate jdbcTemplate) {
    return jdbcTemplate.queryForObject(
        "SELECT molgenis_menu FROM public.\"sys_set_app#4f91996f\"", String.class);
  }

  private static void updateMenuInDatabase(JdbcTemplate jdbcTemplate, String menuString) {
    jdbcTemplate.update(
        "UPDATE public.\"sys_set_app#4f91996f\" SET \"molgenis_menu\" = ?", menuString);
  }
}
