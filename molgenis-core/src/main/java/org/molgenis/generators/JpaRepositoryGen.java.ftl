<#include "GeneratorHelper.ftl">
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* File:        ${file}
 * Generator:   ${generator} ${version}
 *
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
package ${package};

import javax.persistence.EntityManager;

import org.molgenis.data.jpa.JpaRepository;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.data.support.QueryResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * <#if JavaName(entity) == "RuntimeProperty">@deprecated replaced by setting classes that derive from {@link org.molgenis.data.settings.DefaultSettingsEntity}</#if>
 */
<#if JavaName(entity) == "RuntimeProperty">@Deprecated</#if>
@Repository("${JavaName(entity)}Repository")
public class ${JavaName(entity)}Repository extends JpaRepository
{	
	@Autowired
	public ${JavaName(entity)}Repository(QueryResolver queryResolver)
	{
		super(new ${JavaName(entity)}MetaData(), queryResolver);
	}
	
    /**
	 * For testing purposes
	 */
	public ${JavaName(entity)}Repository(EntityManager entityManager, QueryResolver queryResolver)
	{
		super(entityManager, new ${JavaName(entity)}MetaData(), queryResolver);
	}
}