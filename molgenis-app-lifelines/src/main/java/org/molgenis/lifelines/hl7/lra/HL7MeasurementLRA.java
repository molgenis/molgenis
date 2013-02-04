package org.molgenis.lifelines.hl7.lra;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;

/**
 * 
 * @author roankanninga
 */
public class HL7MeasurementLRA
{

	private Node measurement;
	private XPath xpath;
	private String measurementDisplayName;
	private String measurementName;
	private String measurementDescription;
	private String measurementDataType;
	private static final String OBSERVATION_NAME = "urn:hl7-org:v3:code/@code";
	private static final String OBSERVATION_LABEL = "urn:hl7-org:v3:code/@displayName";
	private static final String OBSERVATION_DATATYPE = "urn:hl7-org:v3:value/@*[local-name()='type']";
	private static final String OBSERVATION_DESCRIPTION = "urn:hl7-org:v3:code/urn:hl7-org:v3:originalText/text()";

	public HL7MeasurementLRA(Node measurement, XPath xpath) throws Exception
	{
		this.measurement = measurement;
		this.xpath = xpath;
		readMeasurementName();
		readMeasurementDescription();
		readMeasurementDataType();
		readMeasurementLabel();
	}

	private void readMeasurementName() throws Exception
	{

		Node nameNode = (Node) xpath.evaluate(OBSERVATION_NAME, measurement, XPathConstants.NODE);

		this.measurementName = nameNode.getNodeValue();
	}

	private void readMeasurementLabel() throws Exception
	{

		Node nameNode = (Node) xpath.evaluate(OBSERVATION_LABEL, measurement, XPathConstants.NODE);

		this.measurementDisplayName = nameNode.getNodeValue();
	}

	private void readMeasurementDescription() throws Exception
	{
		try
		{
			Node nameNode = (Node) xpath.evaluate(OBSERVATION_DESCRIPTION, measurement, XPathConstants.NODE);

			this.measurementDescription = nameNode.getNodeValue();
		}
		catch (Exception e)
		{
			this.measurementDescription = "NO DESCRIPTION";
		}
	}

	private void readMeasurementDataType() throws Exception
	{
		try
		{
			Node nameNode = (Node) xpath.evaluate(OBSERVATION_DATATYPE, measurement, XPathConstants.NODE);

			this.measurementDataType = nameNode.getNodeValue();
		}
		catch (Exception e)
		{
			this.measurementDataType = "NO DATATYPE";
		}
	}

	public String getMeasurementName()
	{
		return measurementName;
	}

	public String getMeasurementDescription()
	{
		return measurementDescription;
	}

	public String getMeasurementDataType()
	{
		return measurementDataType;
	}

	public String getMeasurementDisplayName()
	{
		return measurementDisplayName;
	}

}
