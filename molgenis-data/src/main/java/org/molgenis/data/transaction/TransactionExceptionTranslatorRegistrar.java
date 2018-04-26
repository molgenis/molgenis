package org.molgenis.data.transaction;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Discovers {@link TransactionExceptionTranslator TransactionExceptionTranslators} beans and registers them with the
 * {@link TransactionExceptionTranslatorRegistry}.
 */
@Component
public class TransactionExceptionTranslatorRegistrar
{
	private final TransactionExceptionTranslatorRegistry transactionExceptionTranslatorRegistry;

	public TransactionExceptionTranslatorRegistrar(
			TransactionExceptionTranslatorRegistry transactionExceptionTranslatorRegistry)
	{
		this.transactionExceptionTranslatorRegistry = requireNonNull(transactionExceptionTranslatorRegistry);
	}

	/**
	 * Registers all {@link TransactionExceptionTranslator TransactionExceptionTranslators} in the given application
	 * context with the {@link TransactionExceptionTranslatorRegistry}.
	 *
	 * @param applicationContext application context
	 */
	public void register(ApplicationContext applicationContext)
	{
		applicationContext.getBeansOfType(TransactionExceptionTranslator.class)
						  .values()
						  .forEach(transactionExceptionTranslatorRegistry::register);
	}
}
