package org.molgenis.compute5.db.api;

import java.io.Closeable;

/**
 * Makes a request to the compute db api service
 * 
 * @author erwin
 * 
 */
public interface ComputeDbApiConnection extends Closeable
{
	<T extends ApiResponse> T doRequest(Object request, String uri, Class<T> returnType) throws ApiException;
}
