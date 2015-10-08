package org.molgenis.app.promise;

import java.io.IOException;

import javax.xml.stream.XMLStreamReader;

public interface ProMiseClient
{
	XMLStreamReader getDataForXml(String biobankId, String seqNr) throws IOException;
}
