package org.molgenis.framework.server.async;

import java.io.IOException;
import java.text.ParseException;
import java.util.UUID;

import org.molgenis.framework.db.DatabaseException;

public interface AsyncMolgenisService
{

	public void handleRequest(AsyncMolgenisRequest request, AsyncMolgenisResponse response) throws ParseException,
			DatabaseException, IOException;

	public void handleAsyncRequest(AsyncMolgenisRequest request, UUID id);
}
