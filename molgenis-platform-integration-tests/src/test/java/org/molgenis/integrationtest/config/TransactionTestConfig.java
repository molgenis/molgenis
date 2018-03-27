package org.molgenis.integrationtest.config;

import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.postgresql.transaction.PostgreSqlTransactionManager;
import org.molgenis.data.transaction.TransactionExceptionTranslatorRegistry;
import org.molgenis.data.transaction.TransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@Configuration
@Import({ TransactionExceptionTranslatorRegistry.class, IdGeneratorImpl.class })
public class TransactionTestConfig
{

	@Autowired
	private IdGenerator idGenerator;
	@Autowired
	private DataSource dataSource;

	@Bean
	public TransactionManager transactionManager()
	{
		TransactionExceptionTranslatorRegistry transactionExceptionTranslatorRegistry = new TransactionExceptionTranslatorRegistry();
		return new PostgreSqlTransactionManager(idGenerator, dataSource, transactionExceptionTranslatorRegistry);
	}
}
