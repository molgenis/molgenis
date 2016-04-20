package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractStringDatatypeIT;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = PostgeSqlStringDatatypeIT.StringMySqlTestConfig.class)
public class PostgeSqlStringDatatypeIT extends AbstractStringDatatypeIT
{
	@Configuration
	public static class StringMySqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
