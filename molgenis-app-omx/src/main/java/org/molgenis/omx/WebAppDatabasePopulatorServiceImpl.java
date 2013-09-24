package org.molgenis.omx;

import java.util.Vector;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.model.elements.Entity;
import org.molgenis.omx.auth.GroupAuthority;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisGroupMember;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;
import org.molgenis.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebAppDatabasePopulatorServiceImpl implements WebAppDatabasePopulatorService
{
	private static final String USERNAME_ADMIN = "admin";
	private static final String USERNAME_USER = "user";

	@PersistenceContext
	private EntityManager em;

	private final Database unsecuredDatabase;

	@Value("${admin.password:@null}")
	private String adminPassword;
	@Value("${admin.email:molgenis+admin@gmail.com}")
	private String adminEmail;
	@Value("${user.password:@null}")
	private String userPassword;
	@Value("${user.email:molgenis+user@gmail.com}")
	private String userEmail;

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
		if (userPassword == null) throw new RuntimeException(
				"please configure the user.password property in your molgenis-server.properties");

		// FIXME create users and groups through service class
		MolgenisUser userAdmin = new MolgenisUser();
		userAdmin.setUsername(USERNAME_ADMIN);
		userAdmin.setPassword(new BCryptPasswordEncoder().encode(adminPassword));
		userAdmin.setEmail(adminEmail);
		userAdmin.setActive(true);
		userAdmin.setSuperuser(true);
		unsecuredDatabase.add(userAdmin);

		UserAuthority suAuthority = new UserAuthority();
		suAuthority.setMolgenisUser(userAdmin);
		suAuthority.setRole("ROLE_SU");
		unsecuredDatabase.add(suAuthority);

		MolgenisUser userUser = new MolgenisUser();
		userUser.setUsername(USERNAME_USER);
		userUser.setPassword(new BCryptPasswordEncoder().encode(userPassword));
		userUser.setEmail(userEmail);
		userUser.setActive(true);
		userUser.setSuperuser(false);
		unsecuredDatabase.add(userUser);

		MolgenisGroup usersGroup = new MolgenisGroup();
		usersGroup.setName("All Users");
		unsecuredDatabase.add(usersGroup);

		MolgenisGroupMember molgenisGroupMember1 = new MolgenisGroupMember();
		molgenisGroupMember1.setMolgenisGroup(usersGroup);
		molgenisGroupMember1.setMolgenisUser(userUser);
		unsecuredDatabase.add(molgenisGroupMember1);

		Vector<Entity> entities = unsecuredDatabase.getMetaData().getEntities();
		for (Entity entity : entities)
		{
			GroupAuthority entityAuthority = new GroupAuthority();
			entityAuthority.setMolgenisGroup(usersGroup);
			entityAuthority.setRole(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + entity.getName().toUpperCase());
			unsecuredDatabase.add(entityAuthority);
		}
	}

	@Override
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public boolean isDatabasePopulated() throws DatabaseException
	{
		return unsecuredDatabase.count(MolgenisUser.class) > 0;
	}
}