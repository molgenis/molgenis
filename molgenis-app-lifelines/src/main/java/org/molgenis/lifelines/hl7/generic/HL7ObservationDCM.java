package org.molgenis.lifelines.hl7.generic;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.molgenis.lifelines.hl7.HL7OntologyTerm;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author roankanninga
 */
public class HL7ObservationDCM
{

	private Node measurement;
	private XPath xpath;
	private String displayName = ""; // measurementName
	private String originalText = ""; // measurementDescription
	private String value = ""; // measurementDatatype
	private String id = ""; //
	private String repeatNumberHigh = ""; //
	private String repeatNumberLow = ""; //
	public ArrayList<HL7OntologyTerm> hl7OntologyTermObservation;

	private static final String OBSERVATION_NAME = "urn:hl7-org:v3:code/@displayName";
	private static final String OBSERVATION_ONTOLOGYCODE = "urn:hl7-org:v3:code";
	private static final String OBSERVATION_DESCRIPTION = "urn:hl7-org:v3:code/urn:hl7-org:v3:originalText/text()";
	private static final String OBSERVATION_DATATYPE = "urn:hl7-org:v3:value";
	private static final String OBSERVATION_ID = "urn:hl7-org:v3:id/@root";
	private static final String OBSERVATION_REPEATNUMBER_LOW = "urn:hl7-org:v3:repeatNumber/urn:hl7-org:v3:low/@value";
	private static final String OBSERVATION_REPEATNUMBER_HIGH = "urn:hl7-org:v3:repeatNumber/urn:hl7-org:v3:high/@value";

	public HL7ObservationDCM(Node measurement, XPath xpath) throws Exception
	{
		this.measurement = measurement;
		this.xpath = xpath;
		readOriginalText();
		readValue();
		readID();
		readDisplayName();
		readRepeatNumber();

		NodeList ontologyTermCode = (NodeList) xpath.evaluate(OBSERVATION_ONTOLOGYCODE, measurement,
				XPathConstants.NODESET);

		hl7OntologyTermObservation = new ArrayList<HL7OntologyTerm>();
		for (int i = 1; i < ontologyTermCode.getLength(); i++)
		{
			HL7OntologyTerm ot = new HL7OntologyTerm(ontologyTermCode.item(i), xpath);
			hl7OntologyTermObservation.add(ot);
		}

	}

	public void readDisplayName() throws Exception
	{

		Node nameNode = (Node) xpath.evaluate(OBSERVATION_NAME, measurement, XPathConstants.NODE);

		this.displayName = nameNode.getNodeValue();
	}

	public void readID() throws Exception
	{

		Node nameNode = (Node) xpath.evaluate(OBSERVATION_ID, measurement, XPathConstants.NODE);

		this.id = nameNode.getNodeValue();
	}

	public void readRepeatNumber() throws Exception
	{

		Node nameNode1 = (Node) xpath.evaluate(OBSERVATION_REPEATNUMBER_LOW, measurement, XPathConstants.NODE);
		Node nameNode2 = (Node) xpath.evaluate(OBSERVATION_REPEATNUMBER_HIGH, measurement, XPathConstants.NODE);
		if (nameNode1 != null)
		{
			this.repeatNumberLow = nameNode1.getNodeValue();
		}
		if (nameNode2 != null)
		{
			this.repeatNumberHigh = nameNode2.getNodeValue();
		}
	}

	public void readOriginalText() throws Exception
	{

		Node nameNode = (Node) xpath.evaluate(OBSERVATION_DESCRIPTION, measurement, XPathConstants.NODE);
		if (nameNode != null)
		{
			this.originalText = nameNode.getNodeValue();
		}
	}

	public void readValue() throws Exception
	{
		try
		{

			Node nameNode = (Node) xpath.evaluate(OBSERVATION_DATATYPE, measurement, XPathConstants.NODE);
			NamedNodeMap attr = nameNode.getAttributes();
			Node xsitype = attr.getNamedItem("xsi:type");
			this.value = xsitype.getNodeValue();
		}
		catch (Exception e)
		{
			this.value = "NO DATATYPE";
		}
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getOriginalText()
	{
		return originalText;
	}

	public String getValue()
	{
		return value;
	}

	public String getId()
	{
		return id;
	}

	public String getRepeatNumberHigh()
	{
		return repeatNumberHigh;
	}

	public String getRepeatNumberLow()
	{
		return repeatNumberLow;
	}

	public ArrayList<HL7OntologyTerm> getHl7OntologyTerms()
	{
		return hl7OntologyTermObservation;
	}

}
