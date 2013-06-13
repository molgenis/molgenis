package org.molgenis.lifelines.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import org.molgenis.hl7.POQMMT000001UVQualityMeasureDocument;

public class GenericLayerDataBinder
{
	private final Schema eMeasureSchema;

	public GenericLayerDataBinder(Schema eMeasureSchema)
	{
		if (eMeasureSchema == null) throw new IllegalArgumentException("eMeasureSchema is null");
		this.eMeasureSchema = eMeasureSchema;
	}

	public Marshaller createQualityMeasureDocumentMarshaller() throws JAXBException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(POQMMT000001UVQualityMeasureDocument.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setSchema(eMeasureSchema);
		return jaxbMarshaller;
	}

	public Unmarshaller createQualityMeasureDocumentUnmarshaller() throws JAXBException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(POQMMT000001UVQualityMeasureDocument.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		jaxbUnmarshaller.setSchema(eMeasureSchema);
		return jaxbUnmarshaller;
	}
}
