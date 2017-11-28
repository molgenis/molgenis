package org.molgenis.data.transaction;

import org.molgenis.data.DataAccessException;
import org.springframework.transaction.TransactionException;

/**
 * Spring transaction exception translator
 */
public interface TransactionExceptionTranslator
{
	/**
	 * Translates a Spring transaction exception to a MOLGENIS transaction exception
	 *
	 * @param transactionException Spring transaction exception
	 * @return translated transaction exception or <code>null</code> if transaction could not be translated
	 */
	DataAccessException doTranslate(TransactionException transactionException);
}
