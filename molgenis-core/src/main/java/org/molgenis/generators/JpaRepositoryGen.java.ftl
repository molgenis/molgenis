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
import org.molgenis.data.support.EntityMetaDataCache;
import org.springframework.stereotype.Component;

@Component
public class ${JavaName(entity)}Repository extends AbstractJpaRepository<${JavaName(entity)}>
{	
	public ${JavaName(entity)}Repository()
	{
		EntityMetaDataCache.add(new ${JavaName(entity)}MetaData());
	}
	
	@Override
	public ${JavaName(entity)}MetaData getEntityMetaData()
	{
		return (${JavaName(entity)}MetaData)EntityMetaDataCache.get("${JavaName(entity)}");
	}
}