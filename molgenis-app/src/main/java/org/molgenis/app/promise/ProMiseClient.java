package org.molgenis.app.promise;

import java.io.IOException;

import javax.xml.stream.XMLStreamReader;

import org.molgenis.data.Entity;

public interface ProMiseClient
{
	XMLStreamReader getDataForXml(Entity project, String seqNr) throws IOException;
}
