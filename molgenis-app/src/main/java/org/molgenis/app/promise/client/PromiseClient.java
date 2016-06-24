package org.molgenis.app.promise.client;

import org.molgenis.data.Entity;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.function.Consumer;

public interface PromiseClient
{
	void getData(Entity project, String seqNr, Consumer<XMLStreamReader> consumer) throws IOException;
}
