package org.molgenis.migrate.version.v1_15;

import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.i18n.LanguageMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.security.account.AccountService;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.core.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class Step25LanguagesPermissions extends MolgenisUpgrade implements ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(Step25LanguagesPermissions.class);
	private final DataService dataService;

	/**
	 * Whether or not this migrator is enabled
	 */
	private boolean enabled;

	@Autowired
	public Step25LanguagesPermissions(DataService dataService)
	{
		super(24, 25);
		this.dataService = dataService;
	}

	@Override
	public void upgrade()
	{
		LOG.info("Updating metadata from version 24 to 25");
		enabled = true;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		if (enabled)
		{
			RunAsSystemProxy.runAsSystem(() -> {
				// allow all users to read the app languages
					MolgenisGroup allUsersGroup = dataService.findOne(MolgenisGroup.ENTITY_NAME,
							QueryImpl.EQ(MolgenisGroup.NAME, AccountService.ALL_USER_GROUP), MolgenisGroup.class);

					if (allUsersGroup != null)
					{
						GroupAuthority usersGroupLanguagesAuthority = new GroupAuthority();
						usersGroupLanguagesAuthority.setMolgenisGroup(allUsersGroup);
						usersGroupLanguagesAuthority.setRole(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX
								+ LanguageMetaData.ENTITY_NAME.toUpperCase());
						dataService.add(GroupAuthority.ENTITY_NAME, usersGroupLanguagesAuthority);
					}
				});
		}
	}
}
