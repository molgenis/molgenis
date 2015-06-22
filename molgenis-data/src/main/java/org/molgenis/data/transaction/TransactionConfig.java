package org.molgenis.data.transaction;

import javax.annotation.PostConstruct;

import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionConfig
{
	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisTransactionManager transactionManager;

	@Bean
	public TransactionLogService transactionLogService()
	{
		return new TransactionLogService(dataService);
	}

	@PostConstruct
	public void init()
	{
		transactionManager.addTransactionListener(transactionLogService());
	}
}
