<#--#####################################################################
Generate Table Data Gateway
* One table per concrete class
* One table per class hierarchy root (ensures id's and types)
* Associations map onto the hierarchy root
#####################################################################-->
<#include "GeneratorHelper.ftl">
<#assign entity=JavaName(form.getRecord())>
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* File:        ${Name(model)}/screen/${form.getName()}.java
 * Copyright:   GBIC 2000-${year?c}, all rights reserved
 * Date:        ${date}
 * 
 * generator:   ${generator} ${version}
 *
 * 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
package ${package};

// jdk
import java.util.Vector;
import java.util.ArrayList;

// molgenis
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.FormController;

import org.molgenis.framework.ui.html.*;
import org.molgenis.framework.db.QueryRule.Operator;  
import org.molgenis.framework.db.DatabaseException;

import app.EntitiesImporterImpl;

${imports(model, model.getEntity(entity), "")}
${imports(model, model.getEntity(entity), "ui", "Form")}

<#if parent_form?exists>
//imports parent forms
<#assign xrefentity = parent_form.getRecord()>
import ${xrefentity.getNamespace()}.${JavaName(xrefentity)};
</#if>

/**
 *
 */
public class ${JavaName(form.className)}FormController extends FormController<${entity}>
{
	private static final long serialVersionUID = 1L;
	
	public ${JavaName(form.className)}FormController()
	{
		this(null);
	}
	
	public ${JavaName(form.className)}FormController(ScreenController<?> parent)
	{
		super( "${form.getVelocityName()}", parent );
		getModel().setLabel("${form.label}");
		getModel().setLimit(${form.limit});
		<#if form.header?exists>getModel().setHeader("${form.header}");</#if>
		<#if form.description?exists>getModel().setDescription("${form.description}");</#if>

		<#if form.sortby?exists>
		//sort is a bit hacky awaiting redesign of the Form classes
		try
		{
			((FormController)this).getPager().setOrderByField("${form.sortby}".toLowerCase());
			((FormController)this).getPager().setOrderByOperator(Operator.SORT${form.sortorder});
			this.getModel().setSort("${form.sortby}");
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}
		</#if>	
		getModel().setMode(FormModel.Mode.${form.viewType});
		getModel().setCsvEntityImporter(new EntitiesImporterImpl());
		getModel().setEntityClass(${entity}.class);

<#-- parent form filtering -->
<#assign parent_xref = false>		
<#if parent_form?exists>
<#assign xrefentity = Name(parent_form.getRecord())>		
<#list form.getRecord().getAllFields() as field>
	<#--if subform entity refers to parent form entity: show only records that point to parent record-->
	<#--if multiple references exist, then use union, so 'OR' in query rule-->
	<#if field.getType() == "xref" || field.getType() == "mref">
		<#list superclasses(parent_form.getRecord()) as parent_entity>
			<#if parent_entity.getName() == field.xrefEntityName>
		//filter on <#if field.getType() == "mref">ANY </#if>subform_entity.${name(field)} == parentform_entity.${name(field.xrefField)}
				<#if field.xrefEntity.primaryKey.name == SqlName(field.xrefField)>
		getModel().getParentFilters().add(new org.molgenis.framework.ui.FormModel.ParentFilter("${parent_form.name}","${SqlName(field.xrefField)}",java.util.Arrays.asList("${csv(field.xrefLabelNames)}".split(",")),"${SqlName(name(field))}"));
				<#else>
		getModel().getParentFilters().add(new org.molgenis.framework.ui.FormModel.ParentFilter("${parent_form.name}","${SqlName(field.xrefField)}_${field.xrefEntity.primaryKey.name}",java.util.Arrays.asList("${csv(field.xrefLabelNames)}".split(",")),"${SqlName(name(field))}"));
				</#if>
			</#if>
		</#list>
	</#if>
</#list>
<#--parent to subform xrefs-->		
<#list parent_form.getRecord().getAllFields() as field>
	<#--if parent entity refers to subform form entity: show only records that are pointed to by parent record-->
	<#--if multiple references exist, then use union, so 'OR' in query rule-->
	<#if field.getType() == "xref" || field.getType() == "mref">
		<#list superclasses(form.getRecord()) as subform_entity>
			<#if subform_entity.getName() == field.xrefEntityName>
		//filter on subform_entity.${name(field.xrefField)} == <#if field.getType() == "mref">ANY </#if> parentform_entity.${name(field)}
				<#if parent_form.getRecord().getPrimaryKey().getName() == SqlName(name(field))>
		getModel().getParentFilters().add(new org.molgenis.framework.ui.FormModel.ParentFilter("${parent_form.name}","${SqlName(name(field))}",java.util.Arrays.asList("${csv(field.xrefLabelNames)}".split(",")),"${SqlName(field.xrefField)}"));		
				<#else>
		getModel().getParentFilters().add(new org.molgenis.framework.ui.FormModel.ParentFilter("${parent_form.name}","${SqlName(name(field))}_${parent_form.getRecord().getPrimaryKey().getName()}",java.util.Arrays.asList("${csv(field.xrefLabelNames)}".split(",")),"${SqlName(field.xrefField)}"));		
				</#if>
			</#if>
		</#list>
	</#if>
</#list>
</#if>	
<#list form.commands as command>
		getModel().addCommand(new ${command}(this));
</#list>		
<#if form.readOnly>
		getModel().setReadonly(true);
</#if>

<#list form.getChildren() as subscreen>
		<#assign screentype = Name(subscreen.getType().toString()?lower_case) />
		<#if screentype == "Form"><#assign screentype = "FormController"/></#if>
		new ${package}.${JavaName(subscreen)}${screentype}(this);
</#list>	

<#if form.hideFields?size &gt; 0>
		getModel().setUserHiddenColumns(java.util.Arrays.asList(new String[]{${csvQuotedEntity(entity, form.hideFields)}}));
</#if>	

<#list form.getRecord().getAllFields() as field>
	<#if field.getType() == "xref" || field.getType() == "mref">
		getModel().addCommand(new org.molgenis.framework.ui.commands.AddXrefCommand("${entity}_${field.getName()}", this, new ${JavaName(field.getXrefEntityName())}(), new ${JavaName(field.getXrefEntityName())}Form()));
	</#if>
</#list>
	}
	
	@Override
	public HtmlForm getInputs(${entity} entity, boolean newrecord)
	{
	
		${JavaName(entity)}Form form = new ${JavaName(entity)}Form(entity);
		form.setNewRecord(newrecord);
		form.setReadonly(getModel().isReadonly());
		form.setHiddenColumns(getModel().getUserHiddenColumns());
		<#if form.compactView?size &gt; 0>form.setCompactView(java.util.Arrays.asList(new String[]{${csvQuoted(form.compactView)}}));</#if>
		return form;
	}
	
	public void resetSystemHiddenColumns()
	{
		Vector<String> systemHiddenColumns = new Vector<String>();
<#list form.getRecord().getAllFields() as field>
	<#if field.isHidden() || field.hidden>
		systemHiddenColumns.add("${name(field)}");
	</#if>
</#list>
        getModel().setSystemHiddenColumns(systemHiddenColumns);
	}

	@Override	
	public String getSearchField(String fieldName)
	{
<#list form.getRecord().getAllFields() as field>
	<#if field.type="xref" || field.type="mref">
		<#list field.xrefLabelNames?reverse as label>
		if(fieldName.equals("${field.name}")) return "${field.name}_${label}";
		</#list>
	</#if>
</#list>	
		return fieldName;
	}	

	@Override	
	public String getField(String searchFieldName)
	{
<#list form.getRecord().getAllFields() as field>
	<#if field.type="xref" || field.type="mref">
		<#list field.xrefLabelNames?reverse as label>
		if(searchFieldName.equals("${field.name}_${label}")) return "${field.name}";
		</#list>
	</#if>
</#list>	
		return searchFieldName;
	}
		
	@Override
	public void resetCompactView()
	{
		ArrayList<String> compactView = new ArrayList<String>();
<#list form.getCompactView() as field_name>
		compactView.add("${field_name}");
</#list>	
        getModel().setCompactView(compactView);
	}
	
	@Override
	public Class<${entity}> getEntityClass()
	{
		return new ${JavaName(entity)}Form().getEntityClass();
	}
	
	@Override
	public Vector<String> getHeaders()
	{
		return new ${JavaName(entity)}Form().getHeaders();
	}
}