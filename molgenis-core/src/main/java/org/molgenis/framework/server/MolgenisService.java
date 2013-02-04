package org.molgenis.framework.server;

import java.io.IOException;
import java.text.ParseException;

import org.molgenis.framework.db.DatabaseException;

/**
 * MolgenisService defines the generic 'handleRequest' framework that builds on
 * top of the Tuple and Database interfaces to ease the creation of new
 * services. New services can be registered with MOLGENIS on the fly.
 * 
 * Classes should implement this interface if they are expecting calls that
 * alter state.
 * 
 * BIG DISCUSSION: should this be generalized to JAX-RS and JAX-WS based
 * implementation?
 */
public interface MolgenisService
{
	/**
	 * HandleRequest is the standard way how MOLGENIS treats requests that come
	 * in from a user interface (web, script). Typically these requests come
	 * from the web. s
	 * 
	 * @param db
	 *            a MOLGENIS database that can be used by the service
	 * @param request
	 *            a Tuple that wraps the service request
	 * @param outputStream
	 *            a stream where results can be written to; the treatment of
	 *            this output stream is defined by the return
	 * @return flag how the output should be treated, i.e., is it a download,
	 *         json, dialog box, etc.
	 * @throws ParseException
	 *             because of problems with the request
	 * @throws DatabaseException
	 *             because of problems with the database
	 * @throws IOException
	 *             because of problems with file uploads in the request or
	 *             outputStream
	 */
	public void handleRequest(MolgenisRequest request, MolgenisResponse response) throws ParseException,
			DatabaseException, IOException;
}
