<#include "GeneratorHelper.ftl">
package ${package};

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

<#if metaData>
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisPermission;
import org.molgenis.auth.MolgenisRoleGroupLink;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.core.MolgenisEntity;
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
        System.out.println("fillMetadata start");

		Login login = db.getLogin();
        if(useLogin)
        {
            if(login == null) {
                System.out.println("login == null --> no meta data added");           
                return;
            } else if (login instanceof SimpleLogin) {
            	System.out.println("login instanceof SimpleLogin --> no meta data added");
            	return;
            }
        } else {
    		db.setLogin(null); // so we don't run into trouble with the Security Decorators
        }


<#if databaseImpl == 'JPA'>
            EntityManager em = db.getEntityManager();
            em.getTransaction().begin();
</#if>


		MolgenisUser user1 = new MolgenisUser();
		user1.setName("admin");
		user1.setPassword("md5_21232f297a57a5a743894a0e4a801fc3");
		user1.setEmail("");
		user1.setFirstName("admin");
		user1.setLastName("admin");
		user1.setActive(true);
		user1.setSuperuser(true);
		
		MolgenisUser user2 = new MolgenisUser();
		user2.setName("anonymous");
		user2.setPassword("md5_294de3557d9d00b3d2d8a1e6aab028cf");
		user2.setEmail("");
		user2.setFirstName("anonymous");
		user2.setLastName("anonymous");
		user2.setActive(true);

		MolgenisGroup group1 = new MolgenisGroup();
		group1.setName("system");
		MolgenisGroup group2 = new MolgenisGroup();
		group2.setName("AllUsers");

<#if databaseImpl == 'JPA'>
        em.persist(user1);
        em.persist(user2);
        em.persist(group1);
        em.persist(group2);

        em.getTransaction().commit();
     
        login.login(db, "admin", "admin");

        db.beginTx();
<#else>
        db.beginTx();
		//doesn't work fix:
		db.add(user1);
		db.add(user2);
		db.add(group1);
		db.add(group2);	
</#if>


<#list model.getUserinterface().getAllUniqueGroups() as group>
		{
			MolgenisGroup group = new MolgenisGroup();
			group.setName("${group}");
			db.add(group);
		}
</#list>

		MolgenisRoleGroupLink mrgl1 = new MolgenisRoleGroupLink();
		mrgl1.setGroup_Id(group1.getId());
		mrgl1.setRole(user1.getId());

		MolgenisRoleGroupLink mrgl2 = new MolgenisRoleGroupLink();
		mrgl2.setGroup_Id(group2.getId());
		mrgl2.setRole(user1.getId());		

		MolgenisRoleGroupLink mrgl3 = new MolgenisRoleGroupLink();
		mrgl3.setGroup_Id(group1.getId());
		mrgl3.setRole(user2.getId());

		MolgenisRoleGroupLink mrgl4 = new MolgenisRoleGroupLink();
		mrgl4.setGroup_Id(group2.getId());
		mrgl4.setRole(user2.getId());		
		
		db.add(mrgl1);
		db.add(mrgl2);
		db.add(mrgl3);
		db.add(mrgl4);
		
		{
			List<MolgenisEntity> entites = createEntities(ENTITY_VALUES);
			db.add(entites);
		}
		{
			List<MolgenisEntity> entites = createEntities(UI_VALUES);
			db.add(entites);
		}

<#assign schema = model.getUserinterface()>		
<#list schema.getAllChildren() as screen>
	<#if screen.getGroup()?exists || screen.getGroupRead()?exists>
		<#if screen.getType() == "FORM">
		{
			MolgenisGroup role = MolgenisGroup.findByName(db, "<#if screen.getGroup()?exists>${screen.getGroup()}<#else>${screen.getGroupRead()}</#if>");	
			MolgenisEntity entity = db.find(MolgenisEntity.class, new QueryRule("name", Operator.EQUALS, "${screen.getName()}${screen.getType()?lower_case?cap_first}Controller")).get(0);
			
			MolgenisPermission mp = new MolgenisPermission();
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
			mp.setRole(role.getId());
			mp.setEntity(entity.getId());
			mp.setPermission("<#if screen.getGroup()?exists>write<#else>read</#if>");
			db.add(mp);
		}			
		</#if>
	</#if>
</#list>
		{
			//INSERT INTO MolgenisPermission (role_, entity, permission) SELECT 3, id, 'read' FROM MolgenisEntity WHERE MolgenisEntity.name = 'UserLoginPlugin';
			MolgenisEntity insertEntities = db.find(MolgenisEntity.class, new QueryRule("name", Operator.EQUALS, loginPluginName)).get(0);		
			MolgenisPermission mp = new MolgenisPermission();
			mp.setRole(user2.getId());
			mp.setEntity(insertEntities.getId());
			mp.setPermission("read");
			db.add(mp);
		}
		
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
