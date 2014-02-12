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

import org.molgenis.data.jpa.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("${JavaName(entity)}Repository")
public class ${JavaName(entity)}Repository extends JpaRepository
{	
	public ${JavaName(entity)}Repository()
	{
		super(${JavaName(entity)}.class, new ${JavaName(entity)}MetaData());
	}
	
}