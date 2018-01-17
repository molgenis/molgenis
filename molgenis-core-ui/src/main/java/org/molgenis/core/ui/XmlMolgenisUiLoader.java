package org.molgenis.core.ui;

import org.apache.commons.io.IOUtils;
import org.molgenis.core.util.SchemaLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import java.io.IOException;
import java.io.InputStream;

public class XmlMolgenisUiLoader
{
	private static final String UI_XSD = "/schemas/molgenis_ui.xsd";
	private static final String UI_XML = "/molgenis_ui.xml";

	public Molgenis load() throws IOException
	{
		InputStream xsdStream = this.getClass().getResourceAsStream(UI_XSD);
		if (xsdStream == null) throw new IOException("unable to find " + UI_XSD);
		try
		{
			InputStream xmlStream = this.getClass().getResourceAsStream(UI_XML);
			if (xmlStream == null) throw new IOException("unable to find " + UI_XML);
			try
			{
				SchemaLoader schemaLoader = new SchemaLoader(xsdStream);
				Schema molgenisUiSchema = schemaLoader.getSchema();

				JAXBContext jaxbContext = JAXBContext.newInstance(Molgenis.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				unmarshaller.setSchema(molgenisUiSchema);
				return unmarshaller.unmarshal(new StreamSource(xmlStream), Molgenis.class).getValue();
			}
			catch (JAXBException e)
			{
				throw new IOException(e);
			}
			finally
			{
				IOUtils.closeQuietly(xmlStream);
			}
		}
		finally
		{
			IOUtils.closeQuietly(xsdStream);
		}
	}
}
