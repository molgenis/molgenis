<#setting number_format="#"/>
<#include "GeneratorHelper.ftl">
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/*
 * Created by: ${generator}
 * Date: ${date}
 */

package ${package}.servlet;

import java.sql.Connection;
import java.util.LinkedHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseFactory;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.EntitiesImporterSingleton;
import org.molgenis.framework.db.EntitiesValidatorSingleton;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisFrontController;
import org.molgenis.framework.server.MolgenisService;
import org.molgenis.framework.security.Login;
import org.apache.commons.dbcp.BasicDataSource;
import ${package}.EntitiesImporterImpl;
import ${package}.EntitiesValidatorImpl;

<#if generate_BOT>
import java.io.IOException;
import ircbot.IRCHandler;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import generic.JavaCompiler;
import generic.JavaCompiler.CompileUnit;
</#if>

<#if db_mode != 'standalone' || databaseImp = 'jpa'>
import javax.servlet.ServletContext;
import org.molgenis.framework.db.jdbc.JndiDataSourceWrapper;
</#if>

public class FrontController extends MolgenisFrontController
{
	private static final long serialVersionUID = 3141439968743510237L;
	
	@Override
	public void init(javax.servlet.ServletConfig conf) throws javax.servlet.ServletException
	{
		//save options so they can be passed to superclass
		this.usedOptions = new UsedMolgenisOptions();
	
		//create fresh logger based on MolgenisOptions
		createLogger();
		logger = Logger.getLogger(FrontController.class);
		
		//first, we initialize so the ServletContext is created from the webserver
		super.init(conf);
		
		//now we can create the MolgenisContext with objects reusable over many requests
		context = new MolgenisContext(this.getServletConfig(), this.createDataSource(), new UsedMolgenisOptions(), "${model.name}");
		
		//finally, we store all mapped services, and pass them the context used for databasing, serving, etc.
		LinkedHashMap<String,MolgenisService> services = new LinkedHashMap<String,MolgenisService>();
		
		try
		{
			<#list services as service>
			services.put("${service?split('@')[1]}", new ${service?split('@')[0]}(context));
			</#list>
		}
		catch(Exception e)
		{
			System.err.println("FATAL EXCEPTION: failure in starting services in FrontController. Check your services and/or mapping and try again.");
			e.printStackTrace();
			System.exit(0);
		}
		
		this.services = services;
		
		// register instances in singletons (simulate autowiring)
		<#if generate_entitiesimport>
		EntitiesImporterSingleton.setInstance(new EntitiesImporterImpl());
		EntitiesValidatorSingleton.setInstance(new EntitiesValidatorImpl());
		</#if>
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
		<#if databaseImp = 'jpa'>
			DatabaseFactory.create(new ${package}.JpaDatabase());
		<#else>
			Connection conn = context.getDataSource().getConnection();
			DatabaseFactory.create(new ${package}.JDBCDatabase(conn));
		</#if>
			
			Login login = (Login) request.getSession().getAttribute("login");
			if (login == null)
			{
			<#if auth_redirect != ''>
				login = new ${loginclass}(DatabaseFactory.get(), "${auth_redirect}", context.getTokenFactory());
			<#else>
				login = new ${loginclass}(DatabaseFactory.get(), context.getTokenFactory());
			</#if>			
				request.getSession().setAttribute("login", login);
			}

			DatabaseFactory.get().setLogin(login);

			super.service(request, response);
		}
		catch (Exception e)
		{
			logger.error("Exception creating database", e);
		}
		finally
		{
			DatabaseFactory.destroy();
		}
	}
	
	@Override
	public DataSource createDataSource()
	{
	<#if databaseImp = 'jpa'>
		//JPA datasource is provided/managed by server or configured in persistence.xml
		//The application code is shielded from connection/datasource pool details! 
		return null;
	<#else>
		<#if db_mode != 'standalone'>
		BasicDataSource data_src = new BasicDataSource();
		data_src.setDriverClassName("${db_driver}");
		data_src.setUsername("${db_user}");
		data_src.setPassword("${db_password}");
		data_src.setUrl("${db_uri}");
		data_src.setMaxActive(8);
		data_src.setMaxIdle(4);
		DataSource dataSource = (DataSource)data_src;
		return dataSource;
		<#else>
		BasicDataSource data_src = new BasicDataSource();
		data_src.setDriverClassName("${db_driver}");
		data_src.setUsername("${db_user}");
		data_src.setPassword("${db_password}");
		data_src.setUrl("${db_uri}");
		//data_src.setMaxIdle(10);
		//data_src.setMaxWait(1000);
		data_src.setInitialSize(10);
		data_src.setTestOnBorrow(true);
		DataSource dataSource = (DataSource)data_src;
		return dataSource;
		</#if>
	</#if>
	}
	
}
