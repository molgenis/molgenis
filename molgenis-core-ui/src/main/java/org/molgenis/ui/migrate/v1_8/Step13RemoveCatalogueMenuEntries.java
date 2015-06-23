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

public class Step13RemoveCatalogueMenuEntries extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step13RemoveCatalogueMenuEntries.class);

	private static final String PLUGIN_ID = "catalogue";

	private final Repository rtpRepo;
	private final Repository userAuthRepo;
	private final Repository groupAuthRepo;

	public Step13RemoveCatalogueMenuEntries(Repository rtpRepo, Repository userAuthRepo, Repository groupAuthRepo)
	{
		super(12, 13);
		this.rtpRepo = rtpRepo;
		this.userAuthRepo = userAuthRepo;
		this.groupAuthRepo = groupAuthRepo;
	}

	@Override
	public void upgrade()
	{
		runAsSystem(() -> upgradeAsSystem());
	}

	private Step13RemoveCatalogueMenuEntries upgradeAsSystem()
	{
		// update molgenis menu
		LOG.info("Removing form plugin menu entries from menu ...");
		Entity rtp = rtpRepo.findOne(new QueryImpl().eq(RuntimeProperty.NAME, "molgenis.menu"));
		Menu menu = new Gson().fromJson(rtp.getString(RuntimeProperty.VALUE), Menu.class);
		removePluginEntriesFromMenuRec(menu, PLUGIN_ID);
		rtp.set(RuntimeProperty.VALUE, new Gson().toJson(menu));
		rtpRepo.update(rtp);
		LOG.info("Removed form plugin menu entries from menu");

		// remove form permissions
		LOG.info("Removing form plugin permissions ...");
		removePluginPermissions(PLUGIN_ID);
		LOG.info("Removed form plugin permissions");

		return this;
	}

	private void removePluginPermissions(String pluginId)
	{
		// remove user permissions
		List<Entity> userAuths = new ArrayList<Entity>();
		for (Entity userAuth : userAuthRepo)
		{
			if (isPluginRole(userAuth.getString(UserAuthority.ROLE), pluginId))
			{
				userAuths.add(userAuth);
			}
		}
		if (!userAuths.isEmpty())
		{
			userAuthRepo.delete(userAuths);
		}

		// remove group permissions
		List<Entity> groupAuths = new ArrayList<Entity>();
		for (Entity groupAuth : groupAuthRepo)
		{
			if (isPluginRole(groupAuth.getString(GroupAuthority.ROLE), pluginId))
			{
				groupAuths.add(groupAuth);
			}
		}
		if (!groupAuths.isEmpty())
		{
			groupAuthRepo.delete(groupAuths);
		}
	}

	private boolean isPluginRole(String role, String pluginId)
	{
		return role.equals("ROLE_PLUGIN_READ_" + pluginId.toUpperCase())
				|| role.equals("ROLE_PLUGIN_WRITE_" + pluginId.toUpperCase())
				|| role.equals("ROLE_PLUGIN_COUNT_" + pluginId.toUpperCase());
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
