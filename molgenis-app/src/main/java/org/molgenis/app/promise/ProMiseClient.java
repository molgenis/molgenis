package org.molgenis.app.promise;

import java.io.IOException;

import javax.xml.stream.XMLStreamReader;

public interface ProMiseClient
{
	XMLStreamReader helloWorld() throws IOException;

	XMLStreamReader getDataForXml(String project, String pws, String seqNr, String securityCode, String username,
			String password) throws IOException;
}
