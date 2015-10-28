package org.molgenis.app.promise.client;

import java.io.IOException;

import javax.xml.stream.XMLStreamReader;

import org.molgenis.data.Entity;

public interface PromiseClient
{
	XMLStreamReader getDataForXml(Entity project, String seqNr) throws IOException;
}
