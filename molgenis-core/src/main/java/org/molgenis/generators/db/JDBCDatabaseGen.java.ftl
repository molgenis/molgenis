<#include "GeneratorHelper.ftl">
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* File:        ${model.getName()}/model/JDBCDatabase
 * Copyright:   GBIC 2000-${year?c}, all rights reserved
 * Date:        ${date}
 * 
 * generator:   ${generator} ${version}
 *
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */

package ${package};

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import javax.sql.DataSource;
import java.sql.Connection;
import org.molgenis.MolgenisOptions;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.jdbc.DataSourceWrapper;
import org.molgenis.framework.db.jdbc.SimpleDataSourceWrapper;
import org.molgenis.model.elements.Model;
<#if decorator_overriders != ''>
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.apache.log4j.Logger;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import org.molgenis.util.JarClass;
</#if>
import org.apache.commons.dbcp.BasicDataSource;


public class JDBCDatabase extends org.molgenis.framework.db.jdbc.JDBCDatabase
{
	private JDBCMetaDatabase metaData = null;
	<#if decorator_overriders != ''>private Logger logger = Logger.getLogger(JDBCDatabase.class.getSimpleName());</#if>

	public JDBCDatabase(Connection conn) throws DatabaseException
	{
		super(conn);
		this.setup();
		<#if decorator_overriders != ''>this.overrideDecorators();</#if>
	}

	public JDBCDatabase(DataSource data_src, File file_source) throws DatabaseException
	{
		this(new SimpleDataSourceWrapper(data_src), file_source);
	}

	public JDBCDatabase(DataSourceWrapper data_src, File file_src) throws DatabaseException
	{
		super(data_src, file_src);
		this.setup();
		<#if decorator_overriders != ''>this.overrideDecorators();</#if>
	}

	public JDBCDatabase(Properties p) throws DatabaseException
	{
		super(p);
		this.setup();
		<#if decorator_overriders != ''>this.overrideDecorators();</#if>
	}
	
	public JDBCDatabase(MolgenisOptions options) throws DatabaseException
	{
		super(options);
		this.setup();
		<#if decorator_overriders != ''>this.overrideDecorators();</#if>
	}
	
	@Deprecated
	public JDBCDatabase() throws DatabaseException
	{
		super((DataSource)JDBCDatabase.createDataSource(), new File("${db_filepath}"));
		this.setup();
		<#if decorator_overriders != ''>this.overrideDecorators();</#if>
	}

	@Deprecated
	private static DataSource createDataSource() {
		BasicDataSource data_src = new BasicDataSource();
		data_src.setDriverClassName("${db_driver}");
		data_src.setUsername("${db_user}");
		data_src.setPassword("${db_password}");
		data_src.setUrl("${db_uri}"); // a path within the src folder?
		data_src.setMaxIdle(10);
		data_src.setMaxWait(1000);
		return (DataSource)data_src;	
	}

	public JDBCDatabase(String propertiesFilePath) throws FileNotFoundException, IOException, DatabaseException
	{
		super(propertiesFilePath);
		this.setup();
		<#if decorator_overriders != ''>this.overrideDecorators();</#if>
	}
	
	private void setup()
	{
		<#list entities as entity><#if !entity.isAbstract()>
			<#if disable_decorators>
		this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}Mapper(this));				
			<#elseif entity.decorator?exists>
				<#if auth_loginclass?ends_with("SimpleLogin")>
		this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.decorator}(new ${entity.namespace}.db.${JavaName(entity)}Mapper(this)));
				<#else>
		this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.decorator}(new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator(new ${entity.namespace}.db.${JavaName(entity)}Mapper(this))));
				</#if>	
			<#else>
				<#if auth_loginclass?ends_with("SimpleLogin")>
		this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}Mapper(this));
				<#else>
		this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator(new ${entity.namespace}.db.${JavaName(entity)}Mapper(this)));
				</#if>
			</#if>
		</#if></#list>
	}
	
	<#if decorator_overriders != ''>/**
	 * Dynamically overrides decorators. Uses the 'decorator_overriders' molgenis option
	 * to get a build folder, and then maps the names of those classes to the names of
	 * existing decorators. When there is a match, put the overriding decorator in the
	 * mapper, replacing the old one.
	 */
	private void overrideDecorators() throws DatabaseException
	{
	
		URL decoratorOverrideURL = this.getClass().getResource("../${decorator_overriders?replace('.','/')}");
		
		File decoratorOverrideFolder = null;
		
		if(decoratorOverrideURL != null){
			decoratorOverrideFolder = new File(decoratorOverrideURL.getFile().replace("%20", " "));
		}else{
			logger.error("Decorator override location '${decorator_overriders}' could not be loaded. Skipping override..");
			//Were in a jar
			try {
				ArrayList<String> c = JarClass.getClassesFromJARFile("Application.jar","org/molgenis/xgap/decoratoroverriders");
				for(String s : c){
					s = s.substring(s.lastIndexOf(".")+1);
					<#list model.entities as entity><#if !entity.isAbstract()><#if entity.decorator?exists>
					if("${entity.decorator}".substring("${entity.decorator}".lastIndexOf(".")+1).equals(s)){
						//logger.info("${entity.decorator} overwritten for ${JavaName(entity)} entity.");
						try{
							Constructor constr = Class.forName("${decorator_overriders}." + s).getDeclaredConstructor(Mapper.class);
							MapperDecorator mapdec = (MapperDecorator) constr.newInstance(new ${entity.namespace}.db.${JavaName(entity)}Mapper(this));
							<#if auth_loginclass?ends_with("SimpleLogin")>
							this.putMapper(${entity.namespace}.${JavaName(entity)}.class, mapdec);
							<#else>
							this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator(mapdec));
							</#if>	
						}catch(Exception e){
							e.printStackTrace();
							throw new DatabaseException(e);
						}
					}
					</#if></#if></#list>
				}
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(!decoratorOverrideFolder.exists()){
			logger.error("Decorator override folder '${decorator_overriders}' does not exist. Skipping override..");
			return;
		}else{
		 	logger.info("Decorator override folder '${decorator_overriders}' found.");
		}
		
		for(File classFile : decoratorOverrideFolder.listFiles()){
			if(classFile.isFile()){
				String overrideDecName = classFile.getName().substring(0, classFile.getName().length()-6);
				<#list model.entities as entity><#if !entity.isAbstract()><#if entity.decorator?exists>
				if("${entity.decorator}".substring("${entity.decorator}".lastIndexOf(".")+1).equals(overrideDecName)){
					//logger.info("Overriding decorator: ${entity.decorator} with ${decorator_overriders}." + overrideDecName);
					try{
						Constructor constr = Class.forName("${decorator_overriders}." + overrideDecName).getDeclaredConstructor(Mapper.class);
						MapperDecorator mapdec = (MapperDecorator) constr.newInstance(new ${entity.namespace}.db.${JavaName(entity)}Mapper(this));
						<#if auth_loginclass?ends_with("SimpleLogin")>
						this.putMapper(${entity.namespace}.${JavaName(entity)}.class, mapdec);
						<#else>
						this.putMapper(${entity.namespace}.${JavaName(entity)}.class, new ${entity.namespace}.db.${JavaName(entity)}SecurityDecorator(mapdec));
						</#if>	
					}catch(Exception e){
						e.printStackTrace();
						throw new DatabaseException(e);
					}
				}
				</#if></#if></#list>
			}
		}
	}</#if>
	
	@Override
	public Model getMetaData() throws DatabaseException
	{
		//load on demand.
		//nb: the JDBCMetaDatabase must be made much faster which is done in the generator
		//because now it is still validating which it shouldn't
		if(metaData == null)
			metaData = new JDBCMetaDatabase();
		return metaData;
	}
	
<#--	@Override
	public java.util.List<Class<? extends org.molgenis.util.Entity>> getEntityDependencyOrder()
	{
		java.util.List<Class<? extends org.molgenis.util.Entity>> result = new java.util.ArrayList<Class<? extends org.molgenis.util.Entity>>();
<#list entities as entity><#if !entity.abstract>
		result.add(${entity.namespace}.${JavaName(entity)}.class);
</#if></#list>		
		return result;		
	}-->
}