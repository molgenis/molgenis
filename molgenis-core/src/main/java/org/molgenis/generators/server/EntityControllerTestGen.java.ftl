<#include "GeneratorHelper.ftl">
<#assign fields=allFields(entity)>
package org.molgenis.controller;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import org.mockito.Matchers;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
<#list fields as field>
	<#if !field.system && !field.hidden && field.name != "__Type">
		<#if field.type == "xref">
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
			<#break>			 
		</#if>
	</#if>
</#list>
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.molgenis.controller.${entity.name}ControllerTest.${entity.name}ControllerConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.QueryRule;
import ${entity.namespace}.${entity.name};
import org.molgenis.service.${entity.name}Service;
<#assign javaImports = ["${entity.name}"]>
<#list fields as field>
<#if !field.system && !field.hidden && field.name != "__Type">
	<#if field.type == "xref" || field.type == "mref">
		<#if !javaImports?seq_contains("${field.xrefEntity.name}")>
			<#if (!(field.xrefField??) || !field.xrefField.system) && (!(field.xrefEntity??) || !field.xrefEntity.system)>
import org.molgenis.service.${field.xrefEntity.name}Service;
				<#assign javaImports = javaImports + ["${field.xrefEntity.name}"]>
			</#if>
		</#if>
	</#if>
</#if>
</#list>
import org.molgenis.util.GsonHttpMessageConverter;
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
		mockMvc = MockMvcBuilders.standaloneSetup(${entity.name?uncap_first}Controller).setMessageConverters(new GsonHttpMessageConverter()).build();
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
	public void retrieveEntityJson() throws Exception
	{
		${entity.name} ${entity.name?uncap_first} = new ${entity.name}();
		when(${entity.name?uncap_first}Service.read(0)).thenReturn(${entity.name?uncap_first});
		this.mockMvc.perform(get("/api/v1/${entity.name?lower_case}/0?format=json").accept(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON))
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
		when(${entity.name?uncap_first}Service.read(2)).thenThrow(new MolgenisDataAccessException("Access denied"));
		this.mockMvc.perform(get("/api/v1/${entity.name?lower_case}/2").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}
	
	<#list fields as field>
	<#if !field.system && !field.hidden && field.name != "__Type">
		 <#if field.type == "xref">
	@Test
	public void retrieveEntityXref${field.name?cap_first}() throws Exception
	{
		${field.xrefEntity.namespace}.${field.xrefEntity.name} ${field.xrefEntity.name?uncap_first}Xref = new ${field.xrefEntity.namespace}.${field.xrefEntity.name}();
		${field.xrefEntity.name?uncap_first}Xref.set${field.xrefEntity.primaryKey.name?cap_first}(0);
		${entity.name} ${entity.name?uncap_first} = new ${entity.name}();
		${entity.name?uncap_first}.set${field.name?cap_first}(${field.xrefEntity.name?uncap_first}Xref);
		when(${entity.name?uncap_first}Service.read(0)).thenReturn(${entity.name?uncap_first});
		this.mockMvc.perform(get("/api/v1/${entity.name?lower_case}/0/${field.name?uncap_first}")).andExpect(redirectedUrl("/api/v1/${field.xrefEntity.name?lower_case}/0"));
	}
		 </#if>
	</#if>
	</#list>	
	@Test
	public void retrieveEntityCollection_forbidden() throws Exception
	{
		when(${entity.name?uncap_first}Service.readAll(Matchers.any(${type(entity.primaryKey)}.class), Matchers.any(${type(entity.primaryKey)}.class), Matchers.<List<QueryRule>>any())).thenThrow(new MolgenisDataAccessException("Access denied"));
		this.mockMvc.perform(get("/api/v1/${entity.name?lower_case}").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}
	
	@Test
	public void deleteEntity() throws Exception
	{
		this.mockMvc.perform(delete("/api/v1/${entity.name?lower_case}/3")).andExpect(status().isNoContent());
		verify(${entity.name?uncap_first}Service).deleteById(3);
	}

	@Test
	public void deleteEntity_unauthorized() throws Exception
	{
		doThrow(new MolgenisDataAccessException("Access denied")).when(${entity.name?uncap_first}Service).deleteById(5);
		this.mockMvc.perform(delete("/api/v1/${entity.name?lower_case}/5")).andExpect(status().isUnauthorized());
	}

	@Test
	public void deleteEntityPOST() throws Exception
	{
		this.mockMvc.perform(post("/api/v1/${entity.name?lower_case}/6?_method=DELETE")).andExpect(status().isNoContent());
		verify(${entity.name?uncap_first}Service).deleteById(6);
	}

	@Test
	public void deleteEntityPOST_unauthorized() throws Exception
	{
		doThrow(new MolgenisDataAccessException("Access denied")).when(${entity.name?uncap_first}Service).deleteById(8);
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
			<#if (!(field.xrefField??) || !field.xrefField.system) && (!(field.xrefEntity??) || !field.xrefEntity.system)>
		@Bean
		public ${field.xrefEntity.name}Service ${field.xrefEntity.name?uncap_first}Service()
		{
			return mock(${field.xrefEntity.name}Service.class);
		}
				<#assign javaImports = javaImports + ["${field.xrefEntity.name}"]>
			</#if>
		</#if>
		
	</#if>
</#if>
</#list>
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}
	}
}
