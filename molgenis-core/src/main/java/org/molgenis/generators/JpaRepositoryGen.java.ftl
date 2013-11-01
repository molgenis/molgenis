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

import org.molgenis.data.jpa.AbstractJpaRepository;

import org.springframework.stereotype.Component;

@Component
public class ${JavaName(entity)}Repository extends AbstractJpaRepository<${JavaName(entity)}>
{	
	@Override
	public ${JavaName(entity)}MetaData getEntityMetaData()
	{
		return new ${JavaName(entity)}MetaData();
	}
}