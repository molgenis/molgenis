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
          <property name="javax.persistence.jdbc.url" value="${options.dbUri}"/>
          <property name="javax.persistence.jdbc.password" value="${options.dbPassword}"/>
          <property name="javax.persistence.jdbc.driver" value="${options.dbDriver}"/>
          <property name="javax.persistence.jdbc.user" value="${options.dbUser}"/>
          <property name="javax.persistence.validation.mode" value="none"/>
          
          <property name="eclipselink.target-database" value="MySQL" />
          <property name="eclipselink.jdbc.batch-writing" value="JDBC"/>
          <property name="eclipselink.jdbc.batch-writing.size" value="1000"/>
          <property name="eclipselink.ddl-generation" value="create-or-extend-tables"/>
		  <property name="eclipselink.ddl-generation.output-mode" value="database"/>
		  <property name="eclipselink.logging.level" value="OFF"/>
		  <property name="eclipselink.cache.shared.default" value="false"/>
		  <property name="eclipselink.ddl-generation.index-foreign-keys" value="true"/>
	    </properties>
	</persistence-unit>
</persistence>