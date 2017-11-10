package org.molgenis.data.platform;

import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.validation.UnknownEntityReferenceDataAccessException;
import org.molgenis.data.validation.UnknownEntityReferenceException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class DataAccessExceptionTranslatorImpl implements DataAccessExceptionTranslator
{
	@Override
	public RuntimeException translate(DataAccessException dataAccessException)
	{
		if (dataAccessException instanceof MolgenisDataAccessException)
		{
			MolgenisDataAccessException molgenisDataAccessException = (MolgenisDataAccessException) dataAccessException;
			return translate(molgenisDataAccessException);
		}
		else
		{
			// TODO return generic data access LocalizedRuntimeException
			return dataAccessException;
		}
	}

	private RuntimeException translate(MolgenisDataAccessException molgenisDataAccessException)
	{
		if (molgenisDataAccessException instanceof UnknownEntityReferenceDataAccessException)
		{
			UnknownEntityReferenceDataAccessException e = (UnknownEntityReferenceDataAccessException) molgenisDataAccessException;
			throw new UnknownEntityReferenceException(e.getEntityTypeId(), e.getAttributeName(), e.getValueAsString());
		}
		else
		{
			return molgenisDataAccessException;
		}
	}
}
