package org.molgenis.app.promise.client;

import java.io.IOException;

import org.molgenis.data.Entity;

public interface PromiseClient
{
	String getDataForXml(Entity project, String seqNr) throws IOException;
}
