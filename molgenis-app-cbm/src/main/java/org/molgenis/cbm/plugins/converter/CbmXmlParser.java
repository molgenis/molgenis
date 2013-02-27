package org.molgenis.cbm.plugins.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.molgenis.cbm.jaxb.CbmNode;
import org.xml.sax.SAXException;

public class CbmXmlParser
{

	public CbmNode load(File xmlFile, File xsdFile) throws JAXBException, SAXException, FileNotFoundException,
			XMLStreamException
	{
		// set up JAXB

		// Create a JAXB context passing in the class of the object we want to
		// marshal/unmarshal
		JAXBContext jaxbContext = JAXBContext.newInstance(CbmNode.class.getPackage().getName());

		// marshalling Object to XML
		// Create the unmarshaller, this is the nifty little thing that will
		// actually transform the XML back into an object
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(xsdFile);
		unmarshaller.setSchema(schema);

		XMLInputFactory factory = XMLInputFactory.newInstance();

		XMLStreamReader reader = factory.createXMLStreamReader(new InputStreamReader(new FileInputStream(xmlFile),
				Charset.forName("UTF-8")));

		try
		{
			// Unmarshal the XML in the stringWriter back into an object
			JAXBElement<CbmNode> result = unmarshaller.unmarshal(reader, CbmNode.class);
			return result.getValue();
		}
		finally
		{
			reader.close();
		}

	}
}
