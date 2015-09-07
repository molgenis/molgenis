package org.molgenis.data.transaction;

import javax.annotation.PostConstruct;

import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionConfig
{
	@Autowired
	private DataService dataService;

	@Autowired
	private MolgenisTransactionManager transactionManager;

	// This cannot be a app setting because apps settings are not loaded yet when we use it
	@Value("${use.transaction.log:true}")
	private boolean useTransactionLog;

	@Bean
	public TransactionLogService transactionLogService()
	{
		return new TransactionLogService(dataService, useTransactionLog);
	}

	@PostConstruct
	public void init()
	{
		transactionManager.addTransactionListener(transactionLogService());
	}
}
