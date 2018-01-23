package org.molgenis.data.transaction;

import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Registry of {@link TransactionExceptionTranslator TransactionExceptionTranslators}
 */
@Component
public class TransactionExceptionTranslatorRegistry
{
	private final Set<TransactionExceptionTranslator> transactionExceptionTranslators;

	public TransactionExceptionTranslatorRegistry()
	{
		transactionExceptionTranslators = Sets.newLinkedHashSetWithExpectedSize(1);
	}

	public Collection<TransactionExceptionTranslator> getTransactionExceptionTranslators()
	{
		return Collections.unmodifiableSet(transactionExceptionTranslators);
	}

	public void register(TransactionExceptionTranslator transactionExceptionTranslator)
	{
		transactionExceptionTranslators.add(transactionExceptionTranslator);
	}

	@SuppressWarnings("unused")
	public void unregister(TransactionExceptionTranslator transactionExceptionTranslator)
	{
		transactionExceptionTranslators.remove(transactionExceptionTranslator);
	}
}
