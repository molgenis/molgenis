package org.molgenis.omx;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.security.Login;
import org.molgenis.omx.auth.Authority;
import org.molgenis.omx.auth.GroupAuthority;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisGroupMember;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;
import org.molgenis.omx.auth.util.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebAppDatabasePopulatorServiceImpl implements WebAppDatabasePopulatorService
{
	@PersistenceContext
	private EntityManager em;

	private final Database unsecuredDatabase;

	@Value("${admin.password:@null}")
	private String adminPassword;
	@Value("${admin.email:molgenis+admin@gmail.com}")
	private String adminEmail;

	@Autowired
	public WebAppDatabasePopulatorServiceImpl(Database unsecuredDatabase)
	{
		if (unsecuredDatabase == null) throw new IllegalArgumentException("Unsecured database is null");
		this.unsecuredDatabase = unsecuredDatabase;
	}

	@Override
	@Transactional(rollbackFor = DatabaseException.class)
	public void populateDatabase() throws DatabaseException
	{
		if (adminPassword == null) throw new RuntimeException(
				"please configure the admin.password property in your molgenis-server.properties");

		MolgenisUser userAdmin = new MolgenisUser();
		userAdmin.setUsername(Login.USER_ADMIN_NAME);
		userAdmin.setPassword(new PasswordHasher().toMD5(adminPassword)); // FIXME add user through service class
		userAdmin.setEmail(adminEmail);
		userAdmin.setFirstName(Login.USER_ADMIN_NAME);
		userAdmin.setLastName(Login.USER_ADMIN_NAME);
		userAdmin.setActive(true);
		userAdmin.setSuperuser(true);

		UserAuthority suAuthority = new UserAuthority();
		suAuthority.setMolgenisUser(userAdmin);
		suAuthority.setRole("ROLE_SU");

		unsecuredDatabase.add(userAdmin);
		unsecuredDatabase.add(suAuthority);

		MolgenisUser user1 = new MolgenisUser();
		user1.setUsername("user1");
		user1.setPassword(new PasswordHasher().toMD5("user")); // FIXME add user through service class
		user1.setEmail("user1@email.com");
		user1.setFirstName("user");
		user1.setLastName("user");
		user1.setActive(true);
		user1.setSuperuser(false);

		List<Authority> user1Authorities = new ArrayList<Authority>();
		UserAuthority user1Authority = new UserAuthority();
		user1Authority.setMolgenisUser(user1);
		user1Authority.setRole("ROLE_USER");
		user1Authorities.add(user1Authority);

		UserAuthority user1VoidAuthority = new UserAuthority();
		user1VoidAuthority.setMolgenisUser(user1);
		user1VoidAuthority.setRole("ROLE_PLUGIN_VOID_READ_USER");
		user1Authorities.add(user1VoidAuthority);

		UserAuthority user1HomeAuthority = new UserAuthority();
		user1HomeAuthority.setMolgenisUser(user1);
		user1HomeAuthority.setRole("ROLE_PLUGIN_HOME_WRITE_USER");
		user1Authorities.add(user1HomeAuthority);

		MolgenisUser user2 = new MolgenisUser();
		user2.setUsername("user2");
		user2.setPassword(new PasswordHasher().toMD5("user")); // FIXME add user through service class
		user2.setEmail("user2@email.com");
		user2.setFirstName("user");
		user2.setLastName("user");
		user2.setActive(true);
		user2.setSuperuser(false);

		List<Authority> user2Authorities = new ArrayList<Authority>();
		UserAuthority user2Authority = new UserAuthority();
		user2Authority.setMolgenisUser(user1);
		user2Authority.setRole("ROLE_USER");
		user2Authorities.add(user2Authority);

		UserAuthority user2VoidAuthority = new UserAuthority();
		user2VoidAuthority.setMolgenisUser(user2);
		user2VoidAuthority.setRole("ROLE_PLUGIN_PROTOCOLVIEWER_WRITE_USER");
		user2Authorities.add(user2VoidAuthority);

		UserAuthority user2HomeAuthority = new UserAuthority();
		user2HomeAuthority.setMolgenisUser(user2);
		user2HomeAuthority.setRole("ROLE_PLUGIN_HOME_NONE_USER");
		user2Authorities.add(user2HomeAuthority);

		MolgenisGroup usersGroup = new MolgenisGroup();
		usersGroup.setName("All Users");

		MolgenisGroupMember molgenisGroupMember1 = new MolgenisGroupMember();
		molgenisGroupMember1.setMolgenisGroup(usersGroup);
		molgenisGroupMember1.setMolgenisUser(user1);

		MolgenisGroupMember molgenisGroupMember2 = new MolgenisGroupMember();
		molgenisGroupMember2.setMolgenisGroup(usersGroup);
		molgenisGroupMember2.setMolgenisUser(user2);

		GroupAuthority usersGroupAuthority = new GroupAuthority();
		usersGroupAuthority.setMolgenisGroup(usersGroup);
		usersGroupAuthority.setRole("ROLE_PLUGIN_PROTOCOLVIEWER_READ_USER");

		unsecuredDatabase.add(user1);
		unsecuredDatabase.add(user1Authorities);
		unsecuredDatabase.add(user2);
		unsecuredDatabase.add(user2Authorities);
		unsecuredDatabase.add(usersGroup);
		unsecuredDatabase.add(usersGroupAuthority);
		unsecuredDatabase.add(molgenisGroupMember1);
		unsecuredDatabase.add(molgenisGroupMember2);
	}

	@Override
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public boolean isDatabasePopulated() throws DatabaseException
	{
		return unsecuredDatabase.count(MolgenisUser.class) > 0;
	}
}