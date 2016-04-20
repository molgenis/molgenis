package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractMetaDataIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlMetaDataIT.MetaDataPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = MetaDataPostgreSqlTestConfig.class)
public class PostgreSqlMetaDataIT extends AbstractMetaDataIT
{
	@Configuration
	public static class MetaDataPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt()
	{
		super.testIt();
	}
}
