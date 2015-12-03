package org.molgenis.migrate.version.v1_8;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.Iterator;

import javax.sql.DataSource;

import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuItem;
import org.molgenis.ui.menu.MenuItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.gson.Gson;

public class Step13RemoveCatalogueMenuEntries extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step13RemoveCatalogueMenuEntries.class);

	private static final String PLUGIN_ID = "catalogue";

	private final DataSource dataSource;
	private final Gson gson;

	public Step13RemoveCatalogueMenuEntries(DataSource dataSource, Gson gson)
	{
		super(12, 13);
		this.dataSource = requireNonNull(dataSource);
		this.gson = requireNonNull(gson);
	}

	@Override
	public void upgrade()
	{
		runAsSystem(() -> upgradeAsSystem());
	}

	private Step13RemoveCatalogueMenuEntries upgradeAsSystem()
	{
		LOG.info("Removing catalogue plugin menu entries from menu ...");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		// check if RuntimeProperty table exists
		if (!jdbcTemplate.queryForList("SHOW TABLES LIKE 'RuntimeProperty'").isEmpty())
		{
			String menuJson = jdbcTemplate
					.queryForObject("SELECT Value FROM RuntimeProperty WHERE Name='molgenis.menu'", String.class);

			// new value
			Menu menu = gson.fromJson(menuJson, Menu.class);
			removePluginEntriesFromMenuRec(menu, PLUGIN_ID);
			String updatedMenuJson = gson.toJson(menu);

			jdbcTemplate
					.execute("UPDATE RuntimeProperty SET value='" + updatedMenuJson + "' WHERE Name='molgenis.menu'");
			LOG.info("Removed catalogue plugin menu entries from menu");

			// remove permissions
			LOG.info("Removing catalogue plugin permissions ...");
			String pattern = "ROLE_PLUGIN_%_" + PLUGIN_ID.toUpperCase();
			jdbcTemplate.execute("DELETE FROM UserAuthority WHERE role LIKE '" + pattern + "'");
			jdbcTemplate.execute("DELETE FROM GroupAuthority WHERE role LIKE '" + pattern + "'");
			LOG.info("Removed catalogue plugin permissions");
		}
		return this;
	}

	private void removePluginEntriesFromMenuRec(MenuItem menu, String pluginId)
	{
		for (Iterator<MenuItem> it = menu.getItems().iterator(); it.hasNext();)
		{
			MenuItem menuItem = it.next();
			if (menuItem.getType() == MenuItemType.MENU)
			{
				removePluginEntriesFromMenuRec(menuItem, pluginId);
				if (menuItem.getItems().isEmpty())
				{
					it.remove();
				}
			}
			else if (menuItem.getId().equals(pluginId))
			{
				it.remove();
			}
		}
	}
}
