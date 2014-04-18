package org.molgenis.security;

import org.molgenis.data.DataService;
import org.molgenis.omx.auth.GroupAuthority;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;
import org.molgenis.security.account.AccountService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.runas.RunAsSystem;
import org.molgenis.security.user.UserAccountController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MolgenisSecurityWebAppDatabasePopulatorServiceImpl
{
    private static final String USERNAME_ADMIN = "admin";

    @Value("${admin.password:@null}")
    private String adminPassword;
    @Value("${admin.email:molgenis+admin@gmail.com}")
    private String adminEmail;
    @Value("${anonymous.email:molgenis+anonymous@gmail.com}")
    private String anonymousEmail;

	@Transactional
	@RunAsSystem
	public void populateDatabase(DataService dataService, String homeControllerId)
    {
        if (adminPassword == null) throw new RuntimeException(
                "please configure the admin.password property in your molgenis-server.properties");

        MolgenisUser userAdmin = new MolgenisUser();
        userAdmin.setUsername(USERNAME_ADMIN);
        userAdmin.setPassword(new BCryptPasswordEncoder().encode(adminPassword));
        userAdmin.setEmail(adminEmail);
        userAdmin.setActive(true);
        userAdmin.setSuperuser(true);
        dataService.add(MolgenisUser.ENTITY_NAME, userAdmin);

        MolgenisUser anonymousUser = new MolgenisUser();
        anonymousUser.setUsername(SecurityUtils.ANONYMOUS_USERNAME);
        anonymousUser.setPassword(new BCryptPasswordEncoder().encode(SecurityUtils.ANONYMOUS_USERNAME));
        anonymousUser.setEmail(anonymousEmail);
        anonymousUser.setActive(true);
        anonymousUser.setSuperuser(false);
        dataService.add(MolgenisUser.ENTITY_NAME, anonymousUser);

        UserAuthority anonymousAuthority = new UserAuthority();
        anonymousAuthority.setMolgenisUser(anonymousUser);
        anonymousAuthority.setRole(SecurityUtils.AUTHORITY_ANONYMOUS);
        dataService.add(UserAuthority.ENTITY_NAME, anonymousAuthority);

        MolgenisGroup usersGroup = new MolgenisGroup();
        usersGroup.setName(AccountService.ALL_USER_GROUP);
        dataService.add(MolgenisGroup.ENTITY_NAME, usersGroup);
        usersGroup.setName(AccountService.ALL_USER_GROUP);

        GroupAuthority usersGroupHomeAuthority = new GroupAuthority();
        usersGroupHomeAuthority.setMolgenisGroup(usersGroup);
		usersGroupHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + homeControllerId.toUpperCase());
        dataService.add(GroupAuthority.ENTITY_NAME, usersGroupHomeAuthority);

        GroupAuthority usersGroupUserAccountAuthority = new GroupAuthority();
        usersGroupUserAccountAuthority.setMolgenisGroup(usersGroup);
        usersGroupUserAccountAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX
                + UserAccountController.ID.toUpperCase());
        dataService.add(GroupAuthority.ENTITY_NAME, usersGroupUserAccountAuthority);

        for (String entityName : dataService.getEntityNames())
        {
            GroupAuthority entityAuthority = new GroupAuthority();
            entityAuthority.setMolgenisGroup(usersGroup);
            entityAuthority.setRole(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + entityName.toUpperCase());
            dataService.add(GroupAuthority.ENTITY_NAME, entityAuthority);
        }
    }
}