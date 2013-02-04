<#include "GeneratorHelper.ftl">
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="1.0" 
             xmlns="http://java.sun.com/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd">
	<persistence-unit name="molgenis" transaction-type="RESOURCE_LOCAL">
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
          <property name="hibernate.c3p0.min_size" value="5"/>
          <property name="hibernate.c3p0.max_size" value="200"/>
          <property name="hibernate.c3p0.max_statements" value="200"/>
<!-- 
          <property name="hibernate.connection.datasource" value="java:/comp/env/jdbc/molgenisdb"/>
-->
          <property name="hibernate.dialect" value="org.hibernate.dialect.${options.hibernateDialect}"/>
          <property name="hibernate.show_sql" value="false"/>
          <property name="hibernate.format_sql" value="false"/>
          <property name="hibernate.query.substitutions" value="true=1, false=0"/>
          <property name="hibernate.jdbc.batch_size" value="50"/>
          <property name="hibernate.dynamic-insert" value="true"/>
          <property name="hibernate.dynamic-update" value="true"/>
          <property name="hibernate.order_inserts" value="true"/>
          <property name="hibernate.order_updates" value="true"/>
          <property name="hibernate.cache.use_query_cache" value="false"/>
          <property name="hibernate.cache.use_second_level_cache" value="false"/>
          <property name="hibernate.search.default.directory_provider" value="org.hibernate.search.store.RAMDirectoryProvider"/>
<!--
          <property name="hibernate.search.default.directory_provider" value="filesystem"/>
-->
          <property name="hibernate.search.default.indexBase" value="${options.hibernateSearchIndexBase}"/>
          
          <!--
          Automatically validates or exports schema DDL to the database when the SessionFactory is created. 
          With create-drop, the database schema will be dropped when the SessionFactory is closed explicitly.
		e.g. validate | update | create | create-drop
           -->
            <property name="hibernate.hbm2ddl.auto" value="validate"/>
           
	    </properties>
	</persistence-unit>
</persistence>