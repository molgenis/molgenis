package org.molgenis.data.platform;

import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.util.LocalizedRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;

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

	private LocalizedRuntimeException translate(MolgenisDataAccessException molgenisDataAccessException)
	{
		// TODO handle exceptions
		return new LocalizedRuntimeException("to", "do")
		{
			@Override
			protected String createMessage()
			{
				return molgenisDataAccessException.getMessage();
			}

			@Override
			protected String createLocalizedMessage(ResourceBundle resourceBundle, Locale locale)
			{
				return molgenisDataAccessException.getMessage();
			}
		};
	}
}
