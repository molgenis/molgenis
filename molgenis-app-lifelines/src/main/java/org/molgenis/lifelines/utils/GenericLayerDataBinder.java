package org.molgenis.lifelines.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import org.molgenis.hl7.POQMMT000001UVQualityMeasureDocument;

public class GenericLayerDataBinder
{
	private static final JAXBContext JAXB_CONTEXT_QUALITY_MEASURE_DOCUMENT;

	static
	{
		try
		{
			JAXB_CONTEXT_QUALITY_MEASURE_DOCUMENT = JAXBContext.newInstance(POQMMT000001UVQualityMeasureDocument.class);
		}
		catch (JAXBException e)
		{
			throw new RuntimeException(e);
		}
	}

	private final Schema eMeasureSchema;

	public GenericLayerDataBinder(Schema eMeasureSchema)
	{
		if (eMeasureSchema == null) throw new IllegalArgumentException("eMeasureSchema is null");
		this.eMeasureSchema = eMeasureSchema;
	}

	public Marshaller createQualityMeasureDocumentMarshaller() throws JAXBException
	{
		Marshaller jaxbMarshaller = JAXB_CONTEXT_QUALITY_MEASURE_DOCUMENT.createMarshaller();
		jaxbMarshaller.setSchema(eMeasureSchema);
		return jaxbMarshaller;
	}

	public Unmarshaller createQualityMeasureDocumentUnmarshaller() throws JAXBException
	{
		Unmarshaller jaxbUnmarshaller = JAXB_CONTEXT_QUALITY_MEASURE_DOCUMENT.createUnmarshaller();
		jaxbUnmarshaller.setSchema(eMeasureSchema);
		return jaxbUnmarshaller;
	}
}
