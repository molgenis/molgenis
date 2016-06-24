package org.molgenis.pathways.service;

import java.rmi.RemoteException;
import java.util.function.Function;

/**
 * {@link FunctionalInterface} for a {@link Function} that can throw a {@link RemoteException}. These will be caught and
 * rethrown as {@link RuntimeException}s.
 */
@FunctionalInterface
public interface RemoteFunction<T, R> extends Function<T, R>
{

	@Override
	default R apply(final T elem)
	{
		try
		{
			return applyThrows(elem);
		}
		catch (final RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	R applyThrows(T elem) throws RemoteException;
}