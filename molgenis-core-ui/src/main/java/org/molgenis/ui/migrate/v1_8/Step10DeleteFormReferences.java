package org.molgenis.ui.migrate.v1_8;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.version.MolgenisUpgrade;
import org.molgenis.system.core.RuntimeProperty;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuItem;
import org.molgenis.ui.menu.MenuItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class Step10DeleteFormReferences extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step10DeleteFormReferences.class);

	private static final String FORM_ID_PREFIX = "form.";

	private final DataSource dataSource;

	public Step10DeleteFormReferences(DataSource dataSource)
	{
		super(9, 10);
		if (dataSource == null) throw new IllegalArgumentException("DataSource is null");
		this.dataSource = dataSource;
	}

	@Override
	public void upgrade()
	{
		runAsSystem(() -> upgradeAsSystem());
	}

	private Step10DeleteFormReferences upgradeAsSystem()
	{
		LOG.info("Removing form plugin entries from menu ...");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		// check if RuntimeProperty table exists
		if (!jdbcTemplate.queryForList("SHOW TABLES LIKE 'RuntimeProperty'").isEmpty())
		{
			String menuJson = jdbcTemplate.queryForObject(
					"SELECT Value FROM RuntimeProperty WHERE Name='molgenis.menu'", String.class);

			// new value
			Menu menu = new Gson().fromJson(menuJson, Menu.class);
			removeFormPluginsFromMenuRec(menu);
			String updatedMenuJson = new Gson().toJson(menu);

			jdbcTemplate.execute("UPDATE RuntimeProperty SET value='" + updatedMenuJson
					+ "' WHERE Name='molgenis.menu'");

			jdbcTemplate.execute("DELETE FROM UserAuthority WHERE role LIKE 'ROLE_PLUGIN_READ_FORM%'");
			jdbcTemplate.execute("DELETE FROM UserAuthority WHERE role LIKE 'ROLE_PLUGIN_WRITE_FORM%'");
			jdbcTemplate.execute("DELETE FROM UserAuthority WHERE role LIKE 'ROLE_PLUGIN_COUNT_FORM%'");

			jdbcTemplate.execute("DELETE FROM GroupAuthority WHERE role LIKE 'ROLE_PLUGIN_READ_FORM%'");
			jdbcTemplate.execute("DELETE FROM GroupAuthority WHERE role LIKE 'ROLE_PLUGIN_WRITE_FORM%'");
			jdbcTemplate.execute("DELETE FROM GroupAuthority WHERE role LIKE 'ROLE_PLUGIN_COUNT_FORM%'");
			LOG.info("Removed form plugins from menu entries from menu");
		}

		return this;
	}

	private void removeFormPluginsFromMenuRec(MenuItem menu)
	{
		for (Iterator<MenuItem> it = menu.getItems().iterator(); it.hasNext();)
		{
			MenuItem menuItem = it.next();
			if (menuItem.getType() == MenuItemType.MENU)
			{
				removeFormPluginsFromMenuRec(menuItem);
				if (menuItem.getItems().isEmpty())
				{
					it.remove();
				}
			}
			else if (menuItem.getId().startsWith(FORM_ID_PREFIX))
			{
				it.remove();
			}
		}
	}
}
