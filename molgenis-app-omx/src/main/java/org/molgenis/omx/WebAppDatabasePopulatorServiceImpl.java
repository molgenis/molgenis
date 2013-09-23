package org.molgenis.omx;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.model.elements.Entity;
import org.molgenis.omx.auth.Authority;
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

		MolgenisUser userAdmin = new MolgenisUser();
		userAdmin.setUsername(USERNAME_ADMIN);
		userAdmin.setPassword(new BCryptPasswordEncoder().encode(adminPassword)); // FIXME add user through service
																					// class
		userAdmin.setEmail(adminEmail);
		userAdmin.setActive(true);
		userAdmin.setSuperuser(true);

		UserAuthority suAuthority = new UserAuthority();
		suAuthority.setMolgenisUser(userAdmin);
		suAuthority.setRole("ROLE_SU");

		unsecuredDatabase.add(userAdmin);
		unsecuredDatabase.add(suAuthority);

		MolgenisUser user1 = new MolgenisUser();
		user1.setUsername(USERNAME_USER);
		user1.setPassword(new BCryptPasswordEncoder().encode(userPassword)); // FIXME add user through service class
		user1.setEmail(userEmail);
		user1.setActive(true);
		user1.setSuperuser(false);

		List<Authority> user2Authorities = new ArrayList<Authority>();
		UserAuthority user2Authority = new UserAuthority();
		user2Authority.setMolgenisUser(user1);
		user2Authority.setRole("ROLE_USER");
		user2Authorities.add(user2Authority);

		unsecuredDatabase.add(user1);

		MolgenisGroup usersGroup = new MolgenisGroup();
		usersGroup.setName("All Users");

		MolgenisGroupMember molgenisGroupMember1 = new MolgenisGroupMember();
		molgenisGroupMember1.setMolgenisGroup(usersGroup);
		molgenisGroupMember1.setMolgenisUser(user1);

		unsecuredDatabase.add(usersGroup);
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