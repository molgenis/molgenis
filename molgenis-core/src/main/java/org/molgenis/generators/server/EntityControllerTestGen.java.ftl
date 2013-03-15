<#include "GeneratorHelper.ftl">
<#assign fields=allFields(entity)>
package org.molgenis.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.molgenis.controller.${entity.name}ControllerTest.${entity.name}ControllerConfig;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseAccessException;
import ${entity.namespace}.${entity.name};
import org.molgenis.service.${entity.name}Service;
<#assign javaImports = ["${entity.name}"]>
<#list fields as field>
<#if !field.system && !field.hidden && field.name != "__Type">
	<#if field.type == "xref" || field.type == "mref">
		<#if !javaImports?seq_contains("${field.xrefEntity.name}")>
import org.molgenis.service.${field.xrefEntity.name}Service;
			<#assign javaImports = javaImports + ["${field.xrefEntity.name}"]>
		</#if>
	</#if>
</#if>
</#list>
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = ${entity.name}ControllerConfig.class)
public class ${entity.name}ControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private ${entity.name}Controller ${entity.name?uncap_first}Controller;
	@Autowired
	private ${entity.name}Service ${entity.name?uncap_first}Service;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(${entity.name?uncap_first}Controller).build();
	}

	@Test
	public void retrieveEntity() throws Exception
	{
		${entity.name} ${entity.name?uncap_first} = new ${entity.name}();
		when(${entity.name?uncap_first}Service.read(0)).thenReturn(${entity.name?uncap_first});
		this.mockMvc.perform(get("/api/v1/${entity.name?lower_case}/0").accept(MediaType.APPLICATION_JSON))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void retrieveEntity_notFound() throws Exception
	{
		when(${entity.name?uncap_first}Service.read(1)).thenReturn(null);
		this.mockMvc.perform(get("/api/v1/${entity.name?lower_case}/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
	}

	@Test
	public void retrieveEntity_forbidden() throws Exception
	{
		when(${entity.name?uncap_first}Service.read(2)).thenThrow(new DatabaseAccessException("Access denied"));
		this.mockMvc.perform(get("/api/v1/${entity.name?lower_case}/2").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}
	
	@Test
	public void retrieveEntityCollection_forbidden() throws Exception
	{
		when(${entity.name?uncap_first}Service.readAll(any(${type(entity.primaryKey)}.class), any(${type(entity.primaryKey)}.class))).thenThrow(new DatabaseAccessException("Access denied"));
		this.mockMvc.perform(get("/api/v1/${entity.name?lower_case}").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}
	
	@Test
	public void deleteEntity() throws Exception
	{
		when(${entity.name?uncap_first}Service.deleteById(3)).thenReturn(true);
		this.mockMvc.perform(delete("/api/v1/${entity.name?lower_case}/3")).andExpect(status().isNoContent());
	}

	@Test
	public void deleteEntity_notFound() throws Exception
	{
		when(${entity.name?uncap_first}Service.deleteById(4)).thenReturn(false);
		this.mockMvc.perform(delete("/api/v1/${entity.name?lower_case}/4")).andExpect(status().isNotFound());
	}

	@Test
	public void deleteEntity_unauthorized() throws Exception
	{
		when(${entity.name?uncap_first}Service.deleteById(5)).thenThrow(new DatabaseAccessException("Access denied"));
		this.mockMvc.perform(delete("/api/v1/${entity.name?lower_case}/5")).andExpect(status().isUnauthorized());
	}

	@Test
	public void deleteEntityPOST() throws Exception
	{
		when(${entity.name?uncap_first}Service.deleteById(6)).thenReturn(true);
		this.mockMvc.perform(post("/api/v1/${entity.name?lower_case}/6?_method=DELETE")).andExpect(status().isNoContent());
	}

	@Test
	public void deleteEntityPOST_notFound() throws Exception
	{
		when(${entity.name?uncap_first}Service.deleteById(7)).thenReturn(false);
		this.mockMvc.perform(post("/api/v1/${entity.name?lower_case}/7?_method=DELETE")).andExpect(status().isNotFound());
	}

	@Test
	public void deleteEntityPOST_unauthorized() throws Exception
	{
		when(${entity.name?uncap_first}Service.deleteById(8)).thenThrow(new DatabaseAccessException("Access denied"));
		this.mockMvc.perform(post("/api/v1/${entity.name?lower_case}/8?_method=DELETE")).andExpect(status().isUnauthorized());
	}
			
	@Configuration
	public static class ${entity.name}ControllerConfig extends WebMvcConfigurerAdapter
	{
		@Bean
		public ${entity.name}Controller ${entity.name?uncap_first}Controller()
		{
			return new ${entity.name}Controller();
		}

		@Bean
		public ${entity.name}Service ${entity.name?uncap_first}Service()
		{
			return mock(${entity.name}Service.class);
		}

<#assign javaImports = ["${entity.name}"]>
<#list fields as field>
<#if !field.system && !field.hidden && field.name != "__Type">
	<#if field.type == "xref" || field.type == "mref">
		<#if !javaImports?seq_contains("${field.xrefEntity.name}")>
		@Bean
		public ${field.xrefEntity.name}Service ${field.xrefEntity.name?uncap_first}Service()
		{
			return mock(${field.xrefEntity.name}Service.class);
		}
			<#assign javaImports = javaImports + ["${field.xrefEntity.name}"]>
		</#if>
		
	</#if>
</#if>
</#list>
		@Bean
		public Database database()
		{
			return mock(Database.class);
		}
	}
}
