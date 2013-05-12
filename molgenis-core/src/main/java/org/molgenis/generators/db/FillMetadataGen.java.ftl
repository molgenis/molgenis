<#include "GeneratorHelper.ftl">
package ${package};

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

<#if metaData>
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisPermission;
import org.molgenis.omx.auth.MolgenisRoleGroupLink;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.core.MolgenisEntity;
import org.molgenis.omx.core.RuntimeProperty;
</#if>

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import java.text.ParseException;
<#if databaseImpl == 'JPA'>
import javax.persistence.EntityManager;
</#if>
import org.molgenis.framework.security.Login;
import org.molgenis.framework.security.SimpleLogin;

public class FillMetadata {
	protected static final Logger logger = Logger.getLogger(FillMetadata.class);
<#if !metaData>
	public static void fillMetadata(Database db) throws Exception {
		logger.info("fillMetadata is Empty!");
	}
	public static void fillMetadata(Database db, boolean useLogin) throws Exception {
		logger.info("fillMetadata is Empty!");
	}
<#else>
	public static void fillMetadata(Database db) throws Exception {
		fillMetadata(db, true);
	}
	
	public static void fillMetadata(Database db, boolean useLogin) throws Exception{
		fillMetadata(db, useLogin, "UserLoginPlugin");
	}
	
	public static void fillMetadata(Database db, boolean useLogin, String loginPluginName) throws Exception {
        logger.debug("fillMetadata start");

		Login login = db.getLogin();
        if(useLogin)
        {
            if(login == null) {
                logger.debug("login == null --> no meta data added");           
                return;
            } else if (login instanceof SimpleLogin) {
            	logger.debug("login instanceof SimpleLogin --> no meta data added");
            	return;
            }
        } else {
    		db.setLogin(null); // so we don't run into trouble with the Security Decorators
        }

<#if databaseImpl == 'JPA'>
		EntityManager em = db.getEntityManager();
		em.getTransaction().begin();
		
</#if>
		MolgenisUser userAdmin = new MolgenisUser();
		userAdmin.setName(Login.USER_ADMIN_NAME);
		userAdmin.setIdentifier(UUID.randomUUID().toString());
		userAdmin.setPassword("md5_21232f297a57a5a743894a0e4a801fc3");
		userAdmin.setEmail("molgenis@gmail.com");
		userAdmin.setFirstName(Login.USER_ADMIN_NAME);
		userAdmin.setLastName(Login.USER_ADMIN_NAME);
		userAdmin.setActive(true);
		userAdmin.setSuperuser(true);
		
		MolgenisUser userAnonymous = new MolgenisUser();
		userAnonymous.setName(Login.USER_ANONYMOUS_NAME);
		userAnonymous.setIdentifier(UUID.randomUUID().toString());
		userAnonymous.setPassword("md5_294de3557d9d00b3d2d8a1e6aab028cf");
		userAnonymous.setEmail(Login.USER_ANONYMOUS_NAME);
		userAnonymous.setFirstName(Login.USER_ANONYMOUS_NAME);
		userAnonymous.setLastName(Login.USER_ANONYMOUS_NAME);
		userAnonymous.setActive(true);

		MolgenisGroup systemUsersGroup = new MolgenisGroup();
		systemUsersGroup.setName(Login.GROUP_SYSTEM_NAME);
		systemUsersGroup.setIdentifier(UUID.randomUUID().toString());
		
		MolgenisGroup allUsersGroup = new MolgenisGroup();
		allUsersGroup.setName(Login.GROUP_USERS_NAME);
		allUsersGroup.setIdentifier(UUID.randomUUID().toString());

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
				
<#if databaseImpl == 'JPA'>
        em.persist(userAdmin);
        em.persist(userAnonymous);
        em.persist(systemUsersGroup);
        em.persist(allUsersGroup);
		em.persist(adminToSystemLink);
		em.persist(adminToAllUsersLink);
        em.getTransaction().commit();
     
        login.login(db, Login.USER_ADMIN_NAME, Login.USER_ADMIN_NAME);

        db.beginTx();
<#else>
        db.beginTx();
		//FIXME doesn't work fix:
		db.add(userAdmin);
		db.add(userAnonymous);
		db.add(systemUsersGroup);
		db.add(allUsersGroup);
		db.add(adminToSystemLink);
		db.add(adminToAllUsersLink);
</#if>


<#list model.getUserinterface().getAllUniqueGroups() as group>
		{
			MolgenisGroup group = new MolgenisGroup();
			group.setName("${group}");
			db.add(group);
		}
</#list>
		
		db.add(createEntities(ENTITY_VALUES));
		db.add(createEntities(UI_VALUES));

<#assign schema = model.getUserinterface()>		
<#list schema.getAllChildren() as screen>
	<#if screen.getGroup()?exists || screen.getGroupRead()?exists>
		<#if screen.getType() == "FORM">
		{
			MolgenisGroup role = MolgenisGroup.findByName(db, "<#if screen.getGroup()?exists>${screen.getGroup()}<#else>${screen.getGroupRead()}</#if>");	
			MolgenisEntity entity = db.find(MolgenisEntity.class, new QueryRule("name", Operator.EQUALS, "${screen.getName()}${screen.getType()?lower_case?cap_first}Controller")).get(0);
			
			MolgenisPermission mp = new MolgenisPermission();
			mp.setName(role.getName());
			mp.setIdentifier(UUID.randomUUID().toString());
			mp.setRole(role.getId());
			mp.setEntity(entity.getId());
			mp.setPermission("<#if screen.getGroup()?exists>write<#else>read</#if>");
			db.add(mp);
		}		
		{
			MolgenisEntity id = db.find(MolgenisEntity.class, new QueryRule("className", Operator.EQUALS, "${screen.getEntity().namespace}.${screen.getEntity().name}")).get(0);
			MolgenisGroup role = MolgenisGroup.findByName(db, "<#if screen.getGroup()?exists>${screen.getGroup()}<#else>${screen.getGroupRead()}</#if>");
			MolgenisEntity entity = db.find(MolgenisEntity.class, new QueryRule("id", Operator.EQUALS, id.getId())).get(0);
			
			MolgenisPermission mp = new MolgenisPermission();
			mp.setName(role.getName());
			mp.setIdentifier(UUID.randomUUID().toString());
			mp.setRole(role.getId());
			mp.setEntity(entity.getId());
			mp.setPermission("<#if screen.getGroup()?exists>write<#else>read</#if>");
			db.add(mp);
		}
		<#else>
		{
			MolgenisGroup role = MolgenisGroup.findByName(db,"<#if screen.getGroup()?exists>${screen.getGroup()}<#else>${screen.getGroupRead()}</#if>");		
			MolgenisEntity entity = db.find(MolgenisEntity.class, new QueryRule("name", Operator.EQUALS, "${screen.getName()}${screen.getType()?lower_case?cap_first}")).get(0);
			
			MolgenisPermission mp = new MolgenisPermission();
			mp.setName(role.getName());
			mp.setIdentifier(UUID.randomUUID().toString());
			mp.setRole(role.getId());
			mp.setEntity(entity.getId());
			mp.setPermission("<#if screen.getGroup()?exists>write<#else>read</#if>");
			db.add(mp);
		}			
		</#if>
	</#if>
</#list>
<#-- permit user 'anonymous' to read a login plugin -->
		MolgenisEntity loginPluginEntity = db.find(MolgenisEntity.class, new QueryRule(MolgenisEntity.NAME, Operator.EQUALS, loginPluginName)).get(0);		
		MolgenisPermission loginPluginPermission = new MolgenisPermission();
		loginPluginPermission.setName(userAnonymous.getName());
		loginPluginPermission.setIdentifier(UUID.randomUUID().toString());
		loginPluginPermission.setRole(userAnonymous.getId());
		loginPluginPermission.setEntity(loginPluginEntity.getId());
		loginPluginPermission.setPermission("read");
		db.add(loginPluginPermission);
			
<#-- permit user 'anonymous' to read RuntimeProperty instances -->
		MolgenisEntity runtimePropertyEntity = db.find(MolgenisEntity.class, new QueryRule(MolgenisEntity.NAME, Operator.EQUALS, RuntimeProperty.class.getSimpleName())).get(0);		
		MolgenisPermission runtimePropertyPermission = new MolgenisPermission();
		runtimePropertyPermission.setName(userAnonymous.getName());
		runtimePropertyPermission.setIdentifier(UUID.randomUUID().toString());
		runtimePropertyPermission.setRole(userAnonymous.getId());
		runtimePropertyPermission.setEntity(runtimePropertyEntity.getId());
		runtimePropertyPermission.setPermission("read");
		db.add(runtimePropertyPermission);
		
		db.commitTx();
		
		db.setLogin(login); // restore login
		
		logger.info("fillMetadata end");
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
		new String[] {"${screen.getName()}${screen.getType()?lower_case?cap_first}Controller", "${screen.getType()}", "app.ui.${screen.getName()}${screen.getType()?lower_case?cap_first}Controller"}<#if screen_has_next>,</#if>
	<#else>
		new String[] {"${screen.getName()}${screen.getType()?lower_case?cap_first}", "${screen.getType()}", "app.ui.${screen.getName()}${screen.getType()?lower_case?cap_first}"}<#if screen_has_next>,</#if>
	</#if>
</#list>	
	}; 
</#if>
}
