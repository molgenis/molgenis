package org.molgenis.compute5.db.api;

/**
 * Makes a request to the compute db api service
 * 
 * @author erwin
 * 
 */
public interface ComputeDbApiConnection
{
	<T extends ApiResponse> T doRequest(Object request, String uri, Class<T> returnType) throws ApiException;
}
