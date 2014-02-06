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

import org.molgenis.data.DataService;
import org.molgenis.data.jpa.JpaRepository;
import org.springframework.stereotype.Component;
import org.molgenis.data.validation.EntityValidator;
import org.springframework.beans.factory.annotation.Autowired;

@Component("${JavaName(entity)}Repository")
public class ${JavaName(entity)}Repository extends JpaRepository
{	
	@Autowired(required = false)
	public void setDataService(DataService dataService)
	{
		dataService.addRepository(this);
	}
	
	@Autowired
	public ${JavaName(entity)}Repository(EntityValidator entityValidator)
	{
		super(${JavaName(entity)}.class, new ${JavaName(entity)}MetaData(), entityValidator);
	}
	
}