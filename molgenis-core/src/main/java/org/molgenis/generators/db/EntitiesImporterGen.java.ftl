<#--helper functions-->
<#include "GeneratorHelper.ftl">

<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* 
 * 
 * generator:   ${generator} ${version}
 *
 * 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
package ${package};

import java.util.Set;

import org.molgenis.framework.db.AbstractEntitiesImporter;

import org.molgenis.data.importer.EntityImportService;
import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@Component
public class EntitiesImporterImpl extends AbstractEntitiesImporter
{
	/** importable entity names (lowercase) */
	private static final Set<String> ENTITIES_IMPORTABLE;
	
	static {
		// entities added in import order
		ENTITIES_IMPORTABLE = Sets.newLinkedHashSet();
	<#list entities as entity>
		<#if !entity.abstract && !entity.system>
		ENTITIES_IMPORTABLE.add("${entity.name?lower_case}");
		</#if>
	</#list>
	}
	
	@Autowired
	public EntitiesImporterImpl(DataService dataService, EntityImportService entityImportService)
	{
		super(dataService, entityImportService);
	}

	protected Set<String> getEntitiesImportable()
	{
		return ENTITIES_IMPORTABLE;
	}
}