<#include "GeneratorHelper.ftl">
<#assign fields=allFields(entity)>
package org.molgenis.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.Valid;

import ${entity.namespace}.${JavaName(entity)};
import org.molgenis.framework.server.EntityCollectionRequest;
import org.molgenis.framework.server.EntityCollectionResponse;
import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.EntityNotFoundException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.service.${entity.name}Service;
<#assign javaImports = ["${entity.name}"]>
<#list fields as field>
<#if !field.system && !field.hidden && field.name != "__Type">
	<#if field.type == "xref" || field.type == "mref">
		<#if !javaImports?seq_contains("${field.xrefEntity.name}")>
import ${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)};
import org.molgenis.controller.${JavaName(field.xrefEntity)}Controller.${JavaName(field.xrefEntity)}Response;
import org.molgenis.service.${field.xrefEntity.name}Service;
			<#assign javaImports = javaImports + ["${field.xrefEntity.name}"]>
		</#if>
	</#if>
</#if>
</#list>
import org.molgenis.util.EntityPager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@SuppressWarnings("unused")
@Controller
@RequestMapping("/api/v1/${entity.name?lower_case}")
public class ${entity.name}Controller
{
	@Autowired
	private ${entity.name}Service ${entity.name?uncap_first}Service;

<#assign javaFields = ["${entity.name}"]>
<#list fields as field>
<#if !field.system && !field.hidden && field.name != "__Type">
	<#if field.type == "xref" || field.type == "mref">
		<#if !javaFields?seq_contains("${field.xrefEntity.name}")>
	@Autowired
	private ${field.xrefEntity.name}Service ${field.xrefEntity.name?uncap_first}Service;
	
			<#assign javaFields = javaFields + ["${field.xrefEntity.name}"]>
		</#if>
	</#if>
</#if>
</#list>
	<#-- Entity instance CRUD operations -->
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<${entity.name}Response> create${entity.name}(@Valid @RequestBody ${entity.name}Request ${entity.name?uncap_first}Request)
			throws DatabaseException
	{
		return _create${entity.name}(${entity.name?uncap_first}Request);
	}

	// Spring's FormHttpMessageConverter cannot bind target classes (as ModelAttribute can)
	@RequestMapping(method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseBody
	public ResponseEntity<${entity.name}Response> create${entity.name}FromForm(@Valid @ModelAttribute ${entity.name}Request ${entity.name?uncap_first}Request)
			throws DatabaseException
	{
		return _create${entity.name}(${entity.name?uncap_first}Request);
	}

	private ResponseEntity<${entity.name}Response> _create${entity.name}(${entity.name}Request ${entity.name?uncap_first}Request) throws DatabaseException
	{
		${entity.name} ${entity.name?uncap_first} = ${entity.name?uncap_first}Service.create(${entity.name?uncap_first}Request.to${entity.name}());
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Location", "/api/v1/${entity.name?lower_case}/" + ${entity.name?uncap_first}.getId());
		return new ResponseEntity<${entity.name}Response>(responseHeaders, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ${entity.name}Response retrieve${entity.name}(@PathVariable ${type(entity.primaryKey)} id, @RequestParam(value="expand", required=false) String... expandFields) throws DatabaseException
	{
		return _retrieve${entity.name}(id, expandFields);
	}
		
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, params = "format=json", produces = "application/json")
	@ResponseBody
	public ${entity.name}Response retrieve${entity.name}Json(@PathVariable ${type(entity.primaryKey)} id, @RequestParam(value="expand", required=false) String... expandFields) throws DatabaseException
	{
		return _retrieve${entity.name}(id, expandFields);
	}

	private ${entity.name}Response _retrieve${entity.name}(${type(entity.primaryKey)} id, String... expandFieldsStr) throws DatabaseException
	{
		${entity.name} ${entity.name?uncap_first} = ${entity.name?uncap_first}Service.read(id);
		if (${entity.name?uncap_first} == null) throw new EntityNotFoundException("${entity.name} " + id.toString() + " not found");
		Set<String> expandFields = expandFieldsStr != null ? new HashSet<String>(Arrays.asList(expandFieldsStr)) : null;
		return new ${entity.name}Response(${entity.name?uncap_first}, expandFields);
	}
			
	<#list fields as field>
	<#if !field.system && !field.hidden && field.name != "__Type">
		 <#if field.type == "xref">
	@RequestMapping(value = "/{id}/${field.name?uncap_first}", method = RequestMethod.GET)
	public String retrieve${entity.name}Xref${field.name?cap_first}(@PathVariable ${type(entity.primaryKey)} id, @RequestParam(value="expand", required=false) String... expandFields) throws DatabaseException
	{
		return _retrieve${entity.name}Xref${field.name?cap_first}(id, null, expandFields);
	}
	
	@RequestMapping(value = "/{id}/${field.name?uncap_first}", method = RequestMethod.GET, params = "format=json", produces = "application/json")
	public String retrieve${entity.name}Xref${field.name?cap_first}Json(@PathVariable ${type(entity.primaryKey)} id, @RequestParam(value="expand", required=false) String... expandFields) throws DatabaseException
	{
		return _retrieve${entity.name}Xref${field.name?cap_first}(id, "json", expandFields);
	}
	
	private String _retrieve${entity.name}Xref${field.name?cap_first}(${type(entity.primaryKey)} id, String format, String... expandFieldsStr) throws DatabaseException
	{
		${entity.name} ${entity.name?uncap_first} = ${entity.name?uncap_first}Service.read(id);
		if (${entity.name?uncap_first} == null) throw new EntityNotFoundException("${entity.name} " + id.toString() + " not found");
		${type(entity.primaryKey)} ${field.xrefEntity.name?uncap_first}Id = ${entity.name?uncap_first}.get${field.name?cap_first}_${field.xrefEntity.primaryKey.name?cap_first}();
		String forwardUri = "forward:/api/v1/${field.xrefEntity.name?lower_case}/" + ${field.xrefEntity.name?uncap_first}Id.toString();
		StringBuilder qsBuilder = new StringBuilder();
		if(format != null) qsBuilder.append(qsBuilder.length() == 0 ? '?' : '&').append("format=").append(format);
		if(expandFieldsStr != null) qsBuilder.append(qsBuilder.length() == 0 ? '?' : '&').append("expand=").append(Joiner.on(',').join(expandFieldsStr));
		return qsBuilder.length() == 0 ? forwardUri : forwardUri + qsBuilder.toString();
	}
	
		</#if>
	</#if>
	</#list>

	<#list fields as field>
	<#if !field.system && !field.hidden && field.name != "__Type">
		 <#if field.type == "mref">
	@RequestMapping(value = "/{id}/${field.name?uncap_first}", method = RequestMethod.GET)
	@ResponseBody
	public EntityCollectionResponse<${field.xrefEntity.name}Response> retrieve${entity.name}Mref${field.name?cap_first}(@PathVariable ${type(entity.primaryKey)} id, @Valid EntityCollectionRequest entityCollectionRequest, @RequestParam(value="expand", required=false) String... expandFields) throws DatabaseException
	{
		return _retrieve${entity.name}Mref${field.name?cap_first}(id, entityCollectionRequest, expandFields);
	}
	
	@RequestMapping(value = "/{id}/${field.name?uncap_first}", method = RequestMethod.GET, params = "format=json", produces = "application/json")
	@ResponseBody
	public EntityCollectionResponse<${field.xrefEntity.name}Response> retrieve${entity.name}Mref${field.name?cap_first}Json(@PathVariable ${type(entity.primaryKey)} id, @Valid EntityCollectionRequest entityCollectionRequest, @RequestParam(value="expand", required=false) String... expandFields) throws DatabaseException
	{
		return _retrieve${entity.name}Mref${field.name?cap_first}(id, entityCollectionRequest, expandFields);
	}
	
	public EntityCollectionResponse<${field.xrefEntity.name}Response> _retrieve${entity.name}Mref${field.name?cap_first}(${type(entity.primaryKey)} id, EntityCollectionRequest entityCollectionRequest, String... expandFieldsStr) throws DatabaseException
	{
		final Set<String> expandFields = expandFieldsStr != null ? new HashSet<String>(Arrays.asList(expandFieldsStr)) : null;
		${entity.name} ${entity.name?uncap_first} = ${entity.name?uncap_first}Service.read(id);
		if (${entity.name?uncap_first} == null) throw new EntityNotFoundException("${entity.name} " + id.toString() + " not found");
		java.util.List<${field.xrefEntity.name}> ${field.xrefEntity.name?uncap_first}Collection = ${entity.name?uncap_first}.get${field.name?cap_first}();
		EntityPager<${field.xrefEntity.name}> ${field.xrefEntity.name?uncap_first}Pager = new EntityPager<${field.xrefEntity.name}>(entityCollectionRequest.getStart(), entityCollectionRequest.getNum(), ${field.xrefEntity.name?uncap_first}Collection.size(), ${field.xrefEntity.name?uncap_first}Collection);
		return new EntityCollectionResponse<${field.xrefEntity.name}Response>(${field.xrefEntity.name?uncap_first}Pager, Lists.newArrayList(Iterables.transform(${field.xrefEntity.name?uncap_first}Collection,
				new Function<${field.xrefEntity.name}, ${field.xrefEntity.name}Response>()
				{
					@Override
					@Nullable
					public ${field.xrefEntity.name}Response apply(@Nullable ${field.xrefEntity.name} ${field.xrefEntity.name?uncap_first})
					{
						return ${field.xrefEntity.name?uncap_first} != null ? new ${field.xrefEntity.name}Response(${field.xrefEntity.name?uncap_first}, expandFields) : null;
					}
				})), "/api/v1/${field.xrefEntity.name?lower_case}");
	}
		</#if>
	</#if>
	</#list>

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	public void update${entity.name}(@PathVariable ${type(entity.primaryKey)} id, @Valid @RequestBody ${entity.name}Request ${entity.name?uncap_first}Request)
			throws DatabaseException
	{
		_update${entity.name}(id, ${entity.name?uncap_first}Request);
	}

	// Spring's FormHttpMessageConverter cannot bind target classes (as ModelAttribute can)
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseBody
	public ResponseEntity<${entity.name}Response> update${entity.name}FromForm(@PathVariable ${type(entity.primaryKey)} id, @PathVariable String _method,
			@Valid @ModelAttribute ${entity.name}Request ${entity.name?uncap_first}Request) throws DatabaseException
	{
		return _create${entity.name}(${entity.name?uncap_first}Request);
	}

	// Tunnel PUT through POST
	@RequestMapping(value = "/{id}", method = RequestMethod.POST, params = "_method=PUT")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void update${entity.name}Post(@PathVariable ${type(entity.primaryKey)} id, @Valid @RequestBody ${entity.name}Request ${entity.name?uncap_first}Request)
			throws DatabaseException
	{
		_update${entity.name}(id, ${entity.name?uncap_first}Request);
	}

	// Tunnel PUT through POST
	@RequestMapping(value = "/{id}", method = RequestMethod.POST, params = "_method=PUT", headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void update${entity.name}FromFormPost(@PathVariable ${type(entity.primaryKey)} id, @Valid @ModelAttribute ${entity.name}Request ${entity.name?uncap_first}Request)
			throws DatabaseException
	{
		_update${entity.name}(id, ${entity.name?uncap_first}Request);
	}

	private void _update${entity.name}(${type(entity.primaryKey)} id, ${entity.name}Request ${entity.name?uncap_first}Request) throws DatabaseException
	{
		${entity.name} ${entity.name?uncap_first} = ${entity.name?uncap_first}Request.to${entity.name}();
		${entity.name?uncap_first}.setId(id);
		${entity.name?uncap_first}Service.update(${entity.name?uncap_first});
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete${entity.name}(@PathVariable ${type(entity.primaryKey)} id) throws DatabaseException
	{
		_delete${entity.name}(id);
	}

	// Tunnel DELETE through POST
	@RequestMapping(value = "/{id}", method = RequestMethod.POST, params = "_method=DELETE")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete${entity.name}Post(@PathVariable ${type(entity.primaryKey)} id) throws DatabaseException
	{
		_delete${entity.name}(id);
	}

	private void _delete${entity.name}(${type(entity.primaryKey)} id) throws DatabaseException
	{
		boolean isDeleted = ${entity.name?uncap_first}Service.deleteById(id);
		if(!isDeleted) throw new EntityNotFoundException("${entity.name} " + id.toString() + " not found");
	}
	
	<#-- Entity collection GET operations -->
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public EntityCollectionResponse<${entity.name}Response> retrieve${entity.name}Collection(@Valid EntityCollectionRequest ${entity.name?uncap_first}CollectionRequest, @RequestParam(value="expand", required=false) String... expandFields) throws DatabaseException
	{
		return _retrieve${entity.name}Collection(${entity.name?uncap_first}CollectionRequest, expandFields);
	}

	@RequestMapping(method = RequestMethod.GET, params = "format=json", produces = "application/json")
	@ResponseBody
	public EntityCollectionResponse<${entity.name}Response> retrieve${entity.name}CollectionJson(@Valid EntityCollectionRequest ${entity.name?uncap_first}CollectionRequest, @RequestParam(value="expand", required=false) String... expandFields) throws DatabaseException
	{
		return _retrieve${entity.name}Collection(${entity.name?uncap_first}CollectionRequest, expandFields);
	}

	// Tunnel GET with body through POST
	@RequestMapping(method = RequestMethod.POST, params = "_method=GET")
	@ResponseBody
	public EntityCollectionResponse<${entity.name}Response> retrieve${entity.name}CollectionPost(@Valid @RequestBody EntityCollectionRequest ${entity.name?uncap_first}CollectionRequest, @RequestParam(value="expand", required=false) String... expandFields) throws DatabaseException
	{
		return _retrieve${entity.name}Collection(${entity.name?uncap_first}CollectionRequest, expandFields);
	}

	// Tunnel GET with body through POST
	@RequestMapping(method = RequestMethod.POST, params = {"_method=GET", "format=json"}, produces = "application/json")
	@ResponseBody
	public EntityCollectionResponse<${entity.name}Response> retrieve${entity.name}CollectionJsonPost(@Valid @RequestBody EntityCollectionRequest ${entity.name?uncap_first}CollectionRequest, @RequestParam(value="expand", required=false) String... expandFields) throws DatabaseException
	{
		return _retrieve${entity.name}Collection(${entity.name?uncap_first}CollectionRequest, expandFields);
	}
	
	private EntityCollectionResponse<${entity.name}Response> _retrieve${entity.name}Collection(EntityCollectionRequest entityCollectionRequest, String... expandFieldsStr) throws DatabaseException
	{
		EntityPager<${entity.name}> ${entity.name?uncap_first}Pager = ${entity.name?uncap_first}Service.readAll(entityCollectionRequest.getStart(), entityCollectionRequest.getNum(), entityCollectionRequest.getQ());
		final Set<String> expandFields = expandFieldsStr != null ? new HashSet<String>(Arrays.asList(expandFieldsStr)) : null;
		return new EntityCollectionResponse<${entity.name}Response>(${entity.name?uncap_first}Pager, Lists.newArrayList(Iterables.transform(
				${entity.name?uncap_first}Pager.getIterable(), new Function<${entity.name}, ${entity.name}Response>()
				{
					@Override
					@Nullable
					public ${entity.name}Response apply(@Nullable ${entity.name} ${entity.name?uncap_first})
					{
						return ${entity.name?uncap_first} != null ? new ${entity.name}Response(${entity.name?uncap_first}, expandFields) : null;
					}
				})), "/api/v1/${entity.name?lower_case}");
	}

	<#-- Entity request class -->
	private static class ${entity.name}Request
	{
	<#list fields as field>
	<#if !field.system && !field.hidden && field.name != "__Type">
		<#if field.type == "xref">
		private ${type(field.xrefEntity.primaryKey)} ${field.name?uncap_first};
		<#elseif field.type == "mref">
		private java.util.List<${type(field.xrefEntity.primaryKey)}> ${field.name?uncap_first};
		<#else>
		private ${type(field)} ${field.name?uncap_first};
		</#if>
	</#if>
	</#list>
	
		public ${entity.name} to${entity.name}()
		{
			${entity.name} ${entity.name?uncap_first} = new ${entity.name}();
		<#list fields as field>
		<#if !field.system && !field.hidden && field.name != "__Type">
			<#if field.type == "xref" || field.type == "mref">
			${entity.name?uncap_first}.set${field.name?cap_first}_${field.xrefEntity.primaryKey.name?cap_first}(${field.name?uncap_first});
			<#else>
			${entity.name?uncap_first}.set${field.name?cap_first}(${field.name?uncap_first});
			</#if>
		</#if>
		</#list>
			return ${entity.name?uncap_first};
		}
		
	<#list fields as field>
	<#if !field.system && !field.hidden && field.name != "__Type">
		<#if field.type == "xref">
		public void set${field.name?cap_first}(${type(field.xrefEntity.primaryKey)} ${field.name?uncap_first})
		{
			this.${field.name?uncap_first} = ${field.name?uncap_first};
		}
		<#elseif field.type == "mref">
		public void set${field.name?cap_first}(java.util.List<${type(field.xrefEntity.primaryKey)}> ${field.name?uncap_first})
		{
			this.${field.name?uncap_first} = ${field.name?uncap_first};
		}
		<#else>
		public void set${field.name?cap_first}(${type(field)} ${field.name?uncap_first})
		{
			this.${field.name?uncap_first} = ${field.name?uncap_first};
		}
		</#if>
		
	</#if>
	</#list>
	}

	<#-- Entity response class -->
	static class ${entity.name}Response
	{
	<#list fields as field>
	<#if field.equals(entity.primaryKey)>
		private final String href;
	<#elseif !field.system && !field.hidden && field.name != "__Type">
		<#if field.type == "xref" || field.type == "mref">
		<#-- java field type depends on field expansion -->
		private final Object ${field.name?uncap_first};
		<#else>
		private final ${type(field)} ${field.name?uncap_first};
		</#if>
	</#if>
	</#list>
	
		public ${entity.name}Response(${entity.name} ${entity.name?uncap_first}, final Set<String> expandFields)
		{
		<#list fields as field>
		<#if field.equals(entity.primaryKey)>
			this.href = "/api/v1/${entity.name?lower_case}/" + ${entity.name?uncap_first}.get${field.name?cap_first}();
		<#elseif !field.system && !field.hidden && field.name != "__Type">
			<#if field.type == "xref">
			if (expandFields != null && expandFields.contains("${field.name?uncap_first}")) this.${field.name?uncap_first} = <#if field.nillable>${entity.name?uncap_first}.get${field.name?cap_first}() == null ? null : </#if>new ${field.xrefEntity.name}Response(${entity.name?uncap_first}.get${field.name?cap_first}(), null);
			else this.${field.name?uncap_first} = <#if field.nillable>${entity.name?uncap_first}.get${field.name?cap_first}() == null ? null : </#if>java.util.Collections.singletonMap("href", "/api/v1/${entity.name?lower_case}/" + ${entity.name?uncap_first}.get${entity.primaryKey.name?cap_first}() + "/${field.name?uncap_first}");	
			<#elseif field.type == "mref">
			java.util.List<${field.xrefEntity.name}> ${field.xrefEntity.name?uncap_first}Collection = ${entity.name?uncap_first}.get${field.name?cap_first}();
			if (expandFields != null && expandFields.contains("${field.name?uncap_first}")) this.${field.name?uncap_first} = <#if field.nillable>${field.xrefEntity.name?uncap_first}Collection == null ? null : </#if>Lists.transform(${field.xrefEntity.name?uncap_first}Collection, new Function<${field.xrefEntity.name}, ${field.xrefEntity.name}Response>()
					{
						@Override
						@Nullable
						public ${field.xrefEntity.name}Response apply(@Nullable ${field.xrefEntity.name} ${field.xrefEntity.name?uncap_first})
						{
							return ${field.xrefEntity.name?uncap_first} == null ? null : new ${field.xrefEntity.name}Response(${field.xrefEntity.name?uncap_first}, expandFields);
						}
					});
			else this.${field.name?uncap_first} = <#if field.nillable>${field.xrefEntity.name?uncap_first}Collection == null ? null : </#if>java.util.Collections.singletonMap("href", "/api/v1/${entity.name?lower_case}/" + ${entity.name?uncap_first}.get${entity.primaryKey.name?cap_first}() + "/${field.name?uncap_first}"); //FIXME compile error	
			<#else>
			this.${field.name?uncap_first} = ${entity.name?uncap_first}.get${field.name?cap_first}();
			</#if>
		</#if>
		</#list>
		}
	
	<#list fields as field>
	<#if field.equals(entity.primaryKey)>
		public String getHref()
		{
			return href;
		}
	<#elseif !field.system && !field.hidden && field.name != "__Type">
		<#if field.type == "xref" || field.type == "mref">
		public Object get${field.name?cap_first}()
		{
			return ${field.name?uncap_first};
		}
		<#else>
		public ${type(field)} get${field.name?cap_first}()
		{
			return ${field.name?uncap_first};
		}
		</#if>
	</#if>
	
	</#list>
	}
	
	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public void handleEntityNotFoundException(EntityNotFoundException e)
	{
	}
	
	@ExceptionHandler(DatabaseAccessException.class)
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	public void handleDatabaseAccessException(DatabaseAccessException e)
	{
	}
}