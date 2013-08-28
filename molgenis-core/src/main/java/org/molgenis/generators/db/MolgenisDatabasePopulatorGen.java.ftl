<#include "GeneratorHelper.ftl">
package ${package};

import org.molgenis.framework.db.Database;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import java.security.NoSuchAlgorithmException;
<#if metaData>
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import org.molgenis.omx.auth.util.PasswordHasher;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisRoleGroupLink;
import org.molgenis.omx.auth.MolgenisRole;
import org.molgenis.omx.auth.MolgenisPermission;
import org.molgenis.omx.core.MolgenisEntity;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
</#if>

public abstract class MolgenisDatabasePopulator implements ApplicationListener<ContextRefreshedEvent> {
<#if metaData>	
	private static final Logger logger = Logger.getLogger(MolgenisDatabasePopulator.class);
	
	// FIXME close database
	@Autowired
	@Qualifier("unauthorizedPrototypeDatabase")
	private Database database;
	
	@Value(${r'"${admin.password:@null}"'})
	private String adminPassword;
	@Value(${r'"${admin.email:molgenis+admin@gmail.com}"'})
	private String adminEmail;
	
</#if>
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
<#if metaData>
		if(!isApplicationDatabaseInitialized())
		{
			try
			{
				logger.info("initializing application database defaults");
				initializeDefaultApplicationDatabase(database);
				logger.info("initialized application database defaults");
				logger.info("initializing application database");
				initializeApplicationDatabase(database);
				logger.info("initialized application database");
			}
			catch(Exception e)
			{
				throw new RuntimeException(e);
			}
		}
<#else>
		// noop
</#if>
	}
	
	protected abstract void initializeApplicationDatabase(Database database) throws Exception;
<#if metaData>
	
	private boolean isApplicationDatabaseInitialized()
	{
		try
		{
			// the database is not initialized if it doesn't contain any users 
			return database.count(MolgenisUser.class) > 0;
		}
		catch(DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void initializeDefaultApplicationDatabase(Database database) throws Exception
	{
		if(adminPassword == null) throw new RuntimeException("please configure the admin.password property in your molgenis-server.properties");	
		
		Login login = database.getLogin();
    	database.setLogin(null); // so we don't run into trouble with the Security Decorators

		// add admin and anonymous users 
		MolgenisUser userAdmin = new MolgenisUser();
		userAdmin.setName(Login.USER_ADMIN_NAME);
		userAdmin.setIdentifier(UUID.randomUUID().toString());
		userAdmin.setPassword(new PasswordHasher().toMD5(adminPassword));
		userAdmin.setEmail(adminEmail);
		userAdmin.setFirstName(Login.USER_ADMIN_NAME);
		userAdmin.setLastName(Login.USER_ADMIN_NAME);
		userAdmin.setActive(true);
		userAdmin.setSuperuser(true);
		
		MolgenisUser userAnonymous = new MolgenisUser();
		userAnonymous.setName(Login.USER_ANONYMOUS_NAME);
		userAnonymous.setIdentifier(UUID.randomUUID().toString());
		userAnonymous.setPassword(new PasswordHasher().toMD5("anonymous")); 
		userAnonymous.setEmail("molgenis+anonymous@gmail.com");
		userAnonymous.setFirstName(Login.USER_ANONYMOUS_NAME);
		userAnonymous.setLastName(Login.USER_ANONYMOUS_NAME);
		userAnonymous.setActive(true);

		// add user groups for admins, normal users and the anonymous user
		MolgenisGroup systemUsersGroup = new MolgenisGroup();
		systemUsersGroup.setName(Login.GROUP_SYSTEM_NAME);
		systemUsersGroup.setIdentifier(UUID.randomUUID().toString());
				
		MolgenisGroup allUsersGroup = new MolgenisGroup();
		allUsersGroup.setName(Login.GROUP_USERS_NAME);
		allUsersGroup.setIdentifier(UUID.randomUUID().toString());

		// add links between users and groups
		MolgenisRoleGroupLink adminToSystemLink = new MolgenisRoleGroupLink();
		adminToSystemLink.setIdentifier(UUID.randomUUID().toString());
		adminToSystemLink.setName(systemUsersGroup.getName() + '-' + userAdmin.getName());
		adminToSystemLink.setGroup(systemUsersGroup);
		adminToSystemLink.setRole(userAdmin);
		
		MolgenisRoleGroupLink adminToAllUsersLink = new MolgenisRoleGroupLink();
		adminToAllUsersLink.setName(allUsersGroup.getName() + '-' + userAdmin.getName());
		adminToAllUsersLink.setIdentifier(UUID.randomUUID().toString());
		adminToAllUsersLink.setGroup(allUsersGroup);
		adminToAllUsersLink.setRole(userAdmin);
				
		// add entities to database
		EntityManager em = database.getEntityManager();
		em.getTransaction().begin();
		try
		{
	        em.persist(userAdmin);
	        em.persist(userAnonymous);
	        em.persist(systemUsersGroup);
	        em.persist(allUsersGroup);
			em.persist(adminToSystemLink);
			em.persist(adminToAllUsersLink);
	        em.getTransaction().commit();
		}
		catch(Exception e)
		{
			em.getTransaction().rollback();
			throw e;
		}
		
		login.login(database, Login.USER_ADMIN_NAME, adminPassword);

		database.beginTx();
		try
		{
	<#-- note that getAllUniqueGroups() excludes admin/anonymous user and system/AllUsers group -->
	<#list model.getUserinterface().getAllUniqueGroups() as group>
			{
				// create group '${group}' referenced in molgenis-ui.xml
				MolgenisGroup group = new MolgenisGroup();
				group.setName("${group}");
				database.add(group);
			}
	</#list>
			// add MolgenisEntity entities to database so we can assign permissions
			database.add(createEntities(ENTITY_VALUES));
			database.add(createEntities(UI_VALUES));
			database.add(createPluginEntities(MolgenisPluginRegistry.getInstance()));
			
			// set permissions for UI components as specified in molgenis-ui.xml
	<#assign schema = model.userinterface>		
	<#list schema.allChildren as screen>
		<#if screen.group?exists || screen.groupRead?exists>
			<#if screen.group?exists>
				<#assign roles=screen.group>
				<#assign permission="write">
			<#else>
				<#assign roles=screen.groupRead>
				<#assign permission="read">
			</#if>
			<#list roles?split(",") as role>
				<#if screen.type == "FORM">
			{
				org.molgenis.omx.auth.MolgenisRole role = org.molgenis.omx.auth.MolgenisRole.findByName(database, "${role}");	
				MolgenisEntity entity = database.find(MolgenisEntity.class, new org.molgenis.framework.db.QueryRule(MolgenisEntity.NAME, org.molgenis.framework.db.QueryRule.Operator.EQUALS, "${screen.getName()}${screen.getType()?lower_case?cap_first}Controller")).get(0);
				
				org.molgenis.omx.auth.MolgenisPermission mp = new org.molgenis.omx.auth.MolgenisPermission();
				mp.setName(role.getName());
				mp.setIdentifier(UUID.randomUUID().toString());
				mp.setRole(role.getId());
				mp.setEntity(entity.getId());
				mp.setPermission("${permission}");
				database.add(mp);
			}		
			{
				MolgenisEntity id = database.find(MolgenisEntity.class, new org.molgenis.framework.db.QueryRule(MolgenisEntity.CLASSNAME, org.molgenis.framework.db.QueryRule.Operator.EQUALS, "${screen.getEntity().namespace}.${screen.getEntity().name}")).get(0);
				org.molgenis.omx.auth.MolgenisRole role = org.molgenis.omx.auth.MolgenisRole.findByName(database, "${role}");
				MolgenisEntity entity = database.find(MolgenisEntity.class, new org.molgenis.framework.db.QueryRule(MolgenisEntity.ID, org.molgenis.framework.db.QueryRule.Operator.EQUALS, id.getId())).get(0);
				
				org.molgenis.omx.auth.MolgenisPermission mp = new org.molgenis.omx.auth.MolgenisPermission();
				mp.setName(role.getName());
				mp.setIdentifier(UUID.randomUUID().toString());
				mp.setRole(role.getId());
				mp.setEntity(entity.getId());
				mp.setPermission("${permission}");
				database.add(mp);
			}
				<#else>
			{
				org.molgenis.omx.auth.MolgenisRole role = org.molgenis.omx.auth.MolgenisRole.findByName(database, "${role}");		
				MolgenisEntity entity = database.find(MolgenisEntity.class, new org.molgenis.framework.db.QueryRule(MolgenisEntity.NAME, org.molgenis.framework.db.QueryRule.Operator.EQUALS, "${screen.getName()}${screen.getType()?lower_case?cap_first}")).get(0);
				
				org.molgenis.omx.auth.MolgenisPermission mp = new org.molgenis.omx.auth.MolgenisPermission();
				mp.setName(role.getName());
				mp.setIdentifier(UUID.randomUUID().toString());
				mp.setRole(role.getId());
				mp.setEntity(entity.getId());
				mp.setPermission("${permission}");
				database.add(mp);
			}		
				</#if>
			</#list>
		</#if>
	</#list>
			database.commitTx();
		}
		catch(Exception e)
		{
			database.rollbackTx();
			throw e;
		}
		
		database.setLogin(login); // restore login
	}

	public MolgenisUser createUser(Database database, String userName, String firstName, String lastName, String email,
			String password, boolean superUser) throws DatabaseException
	{
		database.beginTx();
		MolgenisUser user = new MolgenisUser();
		user.setName(userName);
		user.setIdentifier(UUID.randomUUID().toString());
		try
		{
			user.setPassword(new PasswordHasher().toMD5(password));
		}
		catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setActive(true);
		user.setSuperuser(superUser);
		database.add(user);
		database.commitTx();
		return user;
	}

	public MolgenisGroup createGroup(Database database, String groupName) throws DatabaseException
	{
		database.beginTx();
		MolgenisGroup group = new MolgenisGroup();
		group.setName(groupName);
		group.setIdentifier(UUID.randomUUID().toString());
		database.add(group);
		database.commitTx();
		return group;
		
	}

	private static List<MolgenisEntity> createPluginEntities(MolgenisPluginRegistry pluginRegistry) {
		List<MolgenisEntity> entities = new ArrayList<MolgenisEntity>();
		for(Class<? extends MolgenisPlugin> pluginClazz : pluginRegistry.getPluginClasses()) {
			MolgenisEntity entity = new MolgenisEntity();
			entity.setName(pluginClazz.getSimpleName());
			entity.setClassName(pluginClazz.getName());
			entity.setType("PLUGIN");
			entities.add(entity); 
		}
		return entities;
	}
		
	public static List<MolgenisEntity> createEntities(String[][] entityValues) {
		List<MolgenisEntity> result = new ArrayList<MolgenisEntity>(entityValues.length);
		for(String[] values : entityValues) {
			MolgenisEntity entity = new MolgenisEntity();
			entity.setName(values[0]);
			entity.setType(values[1]);
			entity.setClassName(values[2]);
			result.add(entity);      
		}		
		return result;		
	}

	private static final String[][] ENTITY_VALUES = new String[][] {
	<#list model.getConcreteEntities() as entity>
		new String[] {"${JavaName(entity)}", "ENTITY", "${entity.namespace}.${JavaName(entity)}"}<#if entity_has_next>,</#if>
	</#list>
	};

	private static final String[][] UI_VALUES = new String[][] {
	<#assign schema = model.getUserinterface()>
	<#list schema.getAllChildren() as screen>
		<#if screen.getType() == "FORM">
		new String[] {"${screen.getName()}${screen.getType()?lower_case?cap_first}Controller", "${screen.getType()}", "${package}.ui.${screen.getName()}${screen.getType()?lower_case?cap_first}Controller"}<#if screen_has_next>,</#if>
		<#else>
		new String[] {"${screen.getName()}${screen.getType()?lower_case?cap_first}", "${screen.getType()}", "${package}.ui.${screen.getName()}${screen.getType()?lower_case?cap_first}"}<#if screen_has_next>,</#if>
		</#if>
	</#list>	
	};
</#if>
}
