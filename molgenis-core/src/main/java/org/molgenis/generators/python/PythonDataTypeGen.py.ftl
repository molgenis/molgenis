<#include "GeneratorHelper.ftl">

# File:        ${model.getName()}/model/${entity.getName()}.py
# Copyright:   GBIC 2000-${year?c}, all rights reserved
# Date:        ${date}
# Generator:   ${generator} ${version}
#
# THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!

class ${JavaName(entity)}(<#if entity.hasAncestor()>${JavaName(entity.getAncestor())}<#else></#if><#if entity.hasImplements()>,<#list entity.getImplements() as i> ${JavaName(i)}<#if i_has_next>,</#if></#list><#else></#if>):

	def __init__(self):
		pass

<#list entity.getFields() as f>
	def get${JavaName(f)}(self):
		return self.${JavaName(f)}
		
	def set${JavaName(f)}(self, ${JavaName(f)}):
		self.${JavaName(f)} = ${JavaName(f)}
		
</#list>