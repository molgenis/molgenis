package org.molgenis.omx;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.security.Login;
import org.molgenis.omx.auth.Authority;
import org.molgenis.omx.auth.MolgenisUser;
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

	private final Database database;

	@Value("${admin.password:@null}")
	private String adminPassword;
	@Value("${admin.email:molgenis+admin@gmail.com}")
	private String adminEmail;

	@Autowired
	public WebAppDatabasePopulatorServiceImpl(Database database)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
	}

	@Override
	@Transactional(rollbackFor = DatabaseException.class)
	public void populateDatabase() throws DatabaseException
	{
		if (adminPassword == null) throw new RuntimeException(
				"please configure the admin.password property in your molgenis-server.properties");

		MolgenisUser userAdmin = new MolgenisUser();
		userAdmin.setName(Login.USER_ADMIN_NAME);
		userAdmin.setIdentifier(UUID.randomUUID().toString());
		userAdmin.setPassword(new PasswordHasher().toMD5(adminPassword)); // FIXME add user through service class
		userAdmin.setEmail(adminEmail);
		userAdmin.setFirstName(Login.USER_ADMIN_NAME);
		userAdmin.setLastName(Login.USER_ADMIN_NAME);
		userAdmin.setActive(true);
		userAdmin.setSuperuser(true);

		Authority suAuthority = new Authority();
		suAuthority.setMolgenisUser(userAdmin);
		suAuthority.setRole("ROLE_SU");

		database.add(userAdmin);
		database.add(suAuthority);

		MolgenisUser user = new MolgenisUser();
		user.setName("user");
		user.setIdentifier(UUID.randomUUID().toString());
		user.setPassword(new PasswordHasher().toMD5("user")); // FIXME add user through service class
		user.setEmail("user@email.com");
		user.setFirstName("user");
		user.setLastName("user");
		user.setActive(true);
		user.setSuperuser(false);

		List<Authority> userAuthorities = new ArrayList<Authority>();
		Authority userAuthority = new Authority();
		userAuthority.setMolgenisUser(user);
		userAuthority.setRole("ROLE_USER");
		userAuthorities.add(userAuthority);

		Authority userVoidAuthority = new Authority();
		userVoidAuthority.setMolgenisUser(user);
		userVoidAuthority.setRole("ROLE_PLUGIN_VOID_READ_USER");
		userAuthorities.add(userVoidAuthority);

		Authority userHomeAuthority = new Authority();
		userHomeAuthority.setMolgenisUser(user);
		userHomeAuthority.setRole("ROLE_PLUGIN_HOME_READ_USER");
		userAuthorities.add(userHomeAuthority);

		database.add(user);
		database.add(userAuthorities);
	}

	@Override
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public boolean isDatabasePopulated() throws DatabaseException
	{
		return database.count(MolgenisUser.class) > 0;
	}
}