<#include "GeneratorHelper.ftl">

package app;

public class JpaDatabase extends org.molgenis.framework.db.jpa.JpaDatabase
{
	public void initMappers(JpaDatabase db)
	{
		<#list model.entities as entity><#if !entity.isAbstract()>
			<#if disable_decorators>
				this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(db));			
			<#elseif entity.decorator?exists>
				<#if auth_loginclass?ends_with("SimpleLogin")>
		this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.decorator}(new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(db)));
				<#else>
		this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.decorator}(new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator(new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(db))));
				</#if>	
			<#else>
				<#if auth_loginclass?ends_with("SimpleLogin")>
		this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(db));
				<#else>
		this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator(new ${entity.namespace}.db.${JavaName(entity)}JpaMapper(db)));
				</#if>
			</#if>
		</#if></#list>	
	}
	
	//TODO: Does not function - Connection conn should be an EntityManager instance or so?
	//TODO: What about decorator overriders?
	public JpaDatabase(java.sql.Connection conn) throws org.molgenis.framework.db.DatabaseException
	{
		super(EMFactory.createEntityManager(), new JDBCMetaDatabase());
		initMappers(this);
	}

    public JpaDatabase() throws org.molgenis.framework.db.DatabaseException {
        super(EMFactory.createEntityManager(), new JDBCMetaDatabase());
        initMappers(this);
    }

    public JpaDatabase(String persistenceUnitName) throws org.molgenis.framework.db.DatabaseException {
        super(EMFactory.createEntityManager(persistenceUnitName), new JDBCMetaDatabase());
        initMappers(this);
    }

	public JpaDatabase(boolean testDatabase) throws org.molgenis.framework.db.DatabaseException {
		super(EMFactory.createEntityManager(), new JDBCMetaDatabase());
        throw new UnsupportedOperationException();
    }

	public JpaDatabase(java.util.Map<String, Object> configOverwrites) throws org.molgenis.framework.db.DatabaseException {
		super(EMFactory.createEntityManager("molgenis", configOverwrites), new JDBCMetaDatabase());  
		initMappers(this);
	}

    <#--public javax.persistence.EntityManagerFactory getEntityManagerFactory() {
        return EMFactory.getEntityManagerFactoryByName(persistenceUnitName);
    }

    public static javax.persistence.EntityManagerFactory getEntityManagerFactoryByName(String name) {
        return EMFactory.getEntityManagerFactoryByName(name);
    }

    public static javax.persistence.EntityManagerFactory getEntityManagerFactory(boolean testDatabase) {
        if(testDatabase) {
            return EMFactory.getEntityManagerFactoryByName("molgenis_test");    
        } else {
            return EMFactory.getEntityManagerFactoryByName("molgenis");    
        }            
    }-->
}
