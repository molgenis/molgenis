<#include "GeneratorHelper.ftl">
package ${package}.servlet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sql.DataSource;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;

<#list model.entities as entity>
<#if !entity.isAbstract() && !entity.system>
import ${entity.namespace}.${JavaName(entity)};
</#if>
</#list>

/*
Needed

CRUD methods (json/xml/text)
Single item, and in batch
Create    ---->    PUT (difficulty is the autoid so we don't know path!)
Read      ---->    GET
Update    ---->    POST
Delete    ---->    DELETE

QUERY methods (json/xml/text)
List (incl pagination, filters, sort)

SHOW methods (html, precursor for AJAX services)
Show -> show a nice html presentation
New  -> show an edit box for a new record
Edit -> show an edit box for a selected record
List -> show a pagination box for the entity
The interactive methods can build on the CRUD stuff.

*/

@Path("/")
public class RestApi
{
	@Context 
    private ServletContext sc;
    
<#list entities as entity><#if !entity.abstract && !entity.system>	
	@GET
	@Path("/json/${name(entity)}")
	@Produces("application/json")
	public ${JavaName(entity)}List find${JavaName(entity)}Json(String query) throws DatabaseException, NamingException, IOException
	{
		return new ${JavaName(entity)}List(getDatabase().find(${JavaName(entity)}.class));
	}
	
	@XmlRootElement(name="${name(entity)}")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ${JavaName(entity)}List
	{
		public ${JavaName(entity)}List()
		{
		}
		public ${JavaName(entity)}List(List<${JavaName(entity)}> list)
		{
			this.list = list;
		}
		@XmlElement(name="${name(entity)}")
		public List<${JavaName(entity)}> list;
	}

	@POST
	@Path("/json/${name(entity)}")
	@Produces("application/json")
	public ${JavaName(entity)} add${JavaName(entity)}Json(@FormParam("") ${JavaName(entity)} object) throws DatabaseException, NamingException, IOException
	{
		if(getDatabase().add(object) > 0)
			return object;
		return null;
	}
	
	@GET
	@Path("/json/${name(entity)}/{${pkeyField(entity).name}}")
	@Produces("application/json")
	public ${JavaName(entity)} get${JavaName(entity)}Json(@PathParam("${pkeyField(entity).name}") ${pkeyJavaType(entity)} ${pkeyField(entity).name}) throws DatabaseException, NamingException
	{
		return getDatabase().findById(${JavaName(entity)}.class, ${pkeyField(entity).name});
	}
	
	@PUT
	@Path("/json/${name(entity)}/{${pkeyField(entity).name}}")
	@Produces("application/json")
	public ${JavaName(entity)} update${JavaName(entity)}Json(@PathParam("${pkeyField(entity).name}") ${pkeyJavaType(entity)} ${pkeyField(entity).name}, @FormParam("") ${JavaName(entity)} object)  throws DatabaseException, NamingException, IOException
	{
		if(getDatabase().update(object)>0) return object;
		return null;	
	}
	
	@DELETE
	@Path("/json/${name(entity)}/{${pkeyField(entity).name}}")
	@Produces("application/json")
	public void remove${JavaName(entity)}Json(@PathParam("${pkeyField(entity).name}") ${pkeyJavaType(entity)} ${pkeyField(entity).name}) throws DatabaseException, NamingException, IOException
	{
		getDatabase().remove(getDatabase().findById(${JavaName(entity)}.class, ${pkeyField(entity).name}));	
	}

</#if></#list>

	private Database getDatabase() throws DatabaseException, NamingException
	{
		<#if databaseImp = 'jpa'>
		return ${package}.DatabaseFactory.createInsecure();
		<#else>
		//The datasource is created by the servletcontext!				
		DataSource dataSource = (DataSource)sc.getAttribute("DataSource");
		return ${package}.DatabaseFactory.createInsecure(dataSource, new File("${db_filepath}"));
		</#if>
	}
}
