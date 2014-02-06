<#include "GeneratorHelper.ftl">
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="1.0" 
             xmlns="http://java.sun.com/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd">
	<persistence-unit name="molgenis" transaction-type="RESOURCE_LOCAL">
		<provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
<#list model.entities as entity>
	<#if !entity.isAbstract()>
		<class>${entity.namespace}.${JavaName(entity)}</class>
	</#if>
</#list>
	    <properties>
          <property name="javax.persistence.validation.mode" value="none"/>
          <property name="eclipselink.target-database" value="MySQL" />
          <property name="eclipselink.jdbc.batch-writing" value="jdbc"/>
          <property name="eclipselink.jdbc.batch-writing.size" value="100"/>
          <property name="eclipselink.persistence-context.close-on-commit" value="true"/>
          <property name="eclipselink.persistence-context.persist-on-commit" value="false"/>
          <property name="eclipselink.ddl-generation" value="create-or-extend-tables"/>
		  <property name="eclipselink.ddl-generation.output-mode" value="database"/>
		  <property name="eclipselink.ddl-generation.table-creation-suffix" value="engine=InnoDB"/>
		  <property name="eclipselink.logging.level" value="OFF"/>
		  <property name="eclipselink.cache.shared.default" value="false"/>
		  <property name="eclipselink.ddl-generation.index-foreign-keys" value="true"/>
	    </properties>
	</persistence-unit>
</persistence>