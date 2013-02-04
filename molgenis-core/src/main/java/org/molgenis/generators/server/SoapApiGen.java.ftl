<#include "GeneratorHelper.ftl">
package ${package};

import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.naming.NamingException;

<#list model.entities as entity>
<#if !entity.isAbstract()>
import ${entity.namespace}.${JavaName(entity)};
</#if>
</#list>

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.io.csv.CsvWriter;

@WebService()
@SOAPBinding(style = Style.DOCUMENT)
public class SoapApi
{
// GET SERVICES (for each entity)
<#list entities as entity><#if !entity.abstract && !entity.system>
	@WebMethod()
	@WebResult(name = "${name(entity)}List")
	public ${JavaName(entity)} get${JavaName(entity)}(@WebParam(name = "${SqlName(pkey(entity))}")${pkeyJavaType(entity)} pkey)
	{
		try
		{
			Database database = getDatabase();
			List<${JavaName(entity)}> _result = database.query(${JavaName(entity)}.class).equals("${SqlName(pkey(entity))}", pkey).find();
			if(_result.size() == 1)
				return _result.get(0);//.toString()+"\n";
			return null;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
</#if></#list>

//FIND SERVICES (for each entity)
<#list entities as entity><#if !entity.isAbstract()>
	@WebMethod()
	@WebResult(name = "${name(entity)}List")
	public List<${JavaName(entity)}> find${JavaName(entity)}(<#list allFields(entity) as f><#assign type_label = f.getType().toString()>	 
		@WebParam(name = "${name(f)}") ${type(f)} ${name(f)}<#if (type_label == "xref" || type_label == "mref") && f.xrefLabelNames[0] != f.xrefFieldName >,
		<#list f.xrefLabelNames as label><#if label_index &gt; 0>,
		</#if>@WebParam(name = "${name(f)}_${label}") ${type(f)} ${name(f)}_${label}</#list></#if><#if f_has_next>,</#if></#list>)
	{
		try
		{
			Query<${JavaName(entity)}> q = getDatabase().query(${JavaName(entity)}.class);
			<#list allFields(entity) as f>
			if(${name(f)} != null) q.equals("${name(f)}", ${name(f)});
			</#list>
			return q.limit(1000).find(); //safety net of 1000
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	@WebMethod()
	@WebResult(name = "${name(entity)}Csv")
	public String find${JavaName(entity)}Csv(<#list allFields(entity) as f><#assign type_label = f.getType().toString()>	 
		@WebParam(name = "${name(f)}") ${type(f)} ${name(f)}<#if (type_label == "user" || type_label == "xref" || type_label == "mref" ) && f.xrefLabelNames[0] != f.xrefFieldName >,
		<#list f.xrefLabelNames as label><#if label_index &gt; 0>,
		</#if>@WebParam(name = "${name(f)}_${label}") ${type(f)} ${name(f)}_${label}</#list></#if><#if f_has_next>,</#if></#list>)
	{
		try
		{
			ByteArrayOutputStream _result = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(_result);
			Query<${JavaName(entity)}> q = getDatabase().query(${JavaName(entity)}.class);
			<#list allFields(entity) as f>
			if(${name(f)} != null) q.equals("${name(f)}", ${name(f)});
			</#list>
			CsvWriter csvWriter = new CsvWriter(out, '\t');
			try
			{
				q.limit(1000).find(csvWriter); // safety net of 1000
			}
			finally
			{
				csvWriter.close();
			}
			return _result.toString();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}	
</#if></#list>
	
//CUSTOM SERVICES (defined by 'method' entries in MOLGENIS xml)
<#list methods as method><#-- [WARNING]: javaType is a weird function now! -->
	@WebMethod()
	@WebResult(name = "")
	public List<${method_returntype(method)}> ${method.getName()}(<#list method.getParameters() as param>
		@WebParam(name = "${param.getName()}")${javaType(param.getType())} ${param.getName()}<#if param_has_next>,</#if></#list>)
		 throws Exception
	{
		if(<#list method.getQuery().getRules() as rule>${rule.getParameter()}== null<#if rule_has_next>||</#if></#list>)
			throw new Exception("one or more parameters are missing");
		try
		{
			return getDatabase().find(${method_returntype(method)}.class
					<#list method.getQuery().getRules() as rule>
					, new QueryRule("${rule.getField()}", QueryRule.Operator.${helper.parseQueryOperator(rule.getOperator())}, ${rule.getParameter()})
					</#list>
				);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
</#list>

	public SoapApi(Database database)
	{
		this.database = database;
	}
	
	// data
	private Database getDatabase() throws DatabaseException, NamingException
	{
		return this.database;
	}
	
	
	private Database database = null;
}
