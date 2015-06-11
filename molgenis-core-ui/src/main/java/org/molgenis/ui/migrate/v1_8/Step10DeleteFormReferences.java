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

public class Step10DeleteFormReferences extends MolgenisUpgrade
{
	private static final Logger LOG = LoggerFactory.getLogger(Step10DeleteFormReferences.class);

	private static final String FORM_ID_PREFIX = "form.";

	private final Repository rtpRepo;
	private final Repository userAuthRepo;
	private final Repository groupAuthRepo;

	public Step10DeleteFormReferences(Repository rtpRepo, Repository userAuthRepo, Repository groupAuthRepo)
	{
		super(9, 10);
		this.rtpRepo = rtpRepo;
		this.userAuthRepo = userAuthRepo;
		this.groupAuthRepo = groupAuthRepo;
	}

	@Override
	public void upgrade()
	{
		runAsSystem(() -> upgradeAsSystem());
	}

	private Step10DeleteFormReferences upgradeAsSystem()
	{
		// update molgenis menu
		LOG.info("Removing form plugin menu entries from menu ...");
		Entity rtp = rtpRepo.findOne(new QueryImpl().eq(RuntimeProperty.NAME, "molgenis.menu"));
		Menu menu = new Gson().fromJson(rtp.getString(RuntimeProperty.VALUE), Menu.class);
		removeFormPluginsFromMenuRec(menu);
		rtp.set(RuntimeProperty.VALUE, new Gson().toJson(menu));
		rtpRepo.update(rtp);
		LOG.info("Removed form plugin menu entries from menu");

		// remove form permissions
		LOG.info("Removing form plugin permissions ...");
		removeFormPluginPermissions();
		LOG.info("Removed form plugin permissions");

		return this;
	}

	private void removeFormPluginPermissions()
	{
		// remove user permissions
		List<Entity> userAuths = new ArrayList<Entity>();
		for (Entity userAuth : userAuthRepo)
		{
			if (isFormPluginRole(userAuth.getString(UserAuthority.ROLE)))
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
			if (isFormPluginRole(groupAuth.getString(GroupAuthority.ROLE)))
			{
				groupAuths.add(groupAuth);
			}
		}
		if (!groupAuths.isEmpty())
		{
			groupAuthRepo.delete(groupAuths);
		}
	}

	private boolean isFormPluginRole(String role)
	{
		return role.startsWith("ROLE_PLUGIN_READ_FORM.") || role.startsWith("ROLE_PLUGIN_WRITE_FORM.")
				|| role.startsWith("ROLE_PLUGIN_COUNT_FORM.");
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
