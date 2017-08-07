package org.molgenis.security.twofactor.services;

import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.security.twofactor.RecoveryCodeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import static org.mockito.Mockito.mock;

/**
 * <p>Used for generating and revoering recovery codes</p>
 */
@ContextConfiguration(classes = { RecoveryServiceImplTest.Config.class })
public class RecoveryServiceImplTest extends AbstractTestNGSpringContextTests
{

	@Configuration
	static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataServiceImpl.class);
		}

		@Bean
		public IdGenerator idGenerator()
		{
			return new IdGeneratorImpl();
		}

		@Bean
		public RecoveryCodeFactory recoveryCodeFactory()
		{
			return mock(RecoveryCodeFactory.class);
		}

	}

}
