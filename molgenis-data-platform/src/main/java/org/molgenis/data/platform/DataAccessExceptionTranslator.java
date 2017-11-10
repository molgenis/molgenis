package org.molgenis.data.platform;

import org.springframework.dao.DataAccessException;

/**
 * Translate {@link DataAccessException} to {@link org.molgenis.util.LocalizedRuntimeException}.
 */
public interface DataAccessExceptionTranslator
{
	RuntimeException translate(DataAccessException dataAccessException);
}
