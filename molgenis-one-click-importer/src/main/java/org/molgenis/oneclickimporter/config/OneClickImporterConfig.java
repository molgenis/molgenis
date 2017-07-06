package org.molgenis.metadata.manager.config;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// FIXME org.molgenis.data.MolgenisDataException: ; nested exception is org.springframework.jdbc.BadSqlGrammarException: PreparedStatementCallback; bad SQL grammar [SELECT this."id", this."msgid", this."namespace", this."description", this."en", this."nl", this."de", this."es", this."it", this."pt", this."fr", this."xx" FROM "sys_L10nString#95a21e09" AS this WHERE this."msgid" IN () AND this."namespace" = ?  LIMIT 1000];
// @Configuration
public class OneClickImporterConfig
{
	// @Bean
	public PropertiesMessageSource oneClickImporterMessageSource()
	{
		return new PropertiesMessageSource("one-click-importer");
	}
}
