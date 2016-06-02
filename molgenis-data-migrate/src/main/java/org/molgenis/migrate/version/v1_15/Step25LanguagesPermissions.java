package org.molgenis.migrate.version.v1_15;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.auth.MolgenisGroupMetaData.MOLGENIS_GROUP;
import static org.molgenis.data.i18n.LanguageMetaData.LANGUAGE;

import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.GroupAuthorityFactory;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMetaData;
import org.molgenis.data.DataService;
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
	private final GroupAuthorityFactory groupAuthorityFactory;

	/**
	 * Whether or not this migrator is enabled
	 */
	private boolean enabled;

	@Autowired
	public Step25LanguagesPermissions(DataService dataService, GroupAuthorityFactory groupAuthorityFactory)
	{
		super(24, 25);
		this.dataService = dataService;
		this.groupAuthorityFactory = requireNonNull(groupAuthorityFactory);
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
				MolgenisGroup allUsersGroup = dataService.query(MOLGENIS_GROUP, MolgenisGroup.class)
						.eq(MolgenisGroupMetaData.NAME, AccountService.ALL_USER_GROUP).findOne();

				if (allUsersGroup != null)
				{
					GroupAuthority usersGroupLanguagesAuthority = groupAuthorityFactory.create();
					usersGroupLanguagesAuthority.setMolgenisGroup(allUsersGroup);
					usersGroupLanguagesAuthority
							.setRole(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + LANGUAGE.toUpperCase());
					dataService.add(GROUP_AUTHORITY, usersGroupLanguagesAuthority);
				}
			});
		}
	}
}
