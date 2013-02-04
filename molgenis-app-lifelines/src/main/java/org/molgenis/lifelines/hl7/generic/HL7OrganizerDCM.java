package org.molgenis.lifelines.hl7.generic;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.molgenis.lifelines.hl7.HL7OntologyTerm;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author roankanninga
 */
public class HL7OrganizerDCM
{

	private Node organizer;
	private XPath xpath;
	String id = "";
	String originalText = "";
	String codeOntology = "";

	private static final String OBSERVATION = "urn:hl7-org:v3:component/urn:hl7-org:v3:observation";
	private static final String ORGANIZER_NAME = "urn:hl7-org:v3:code/@displayName";
	private static final String ORIGINALTEXT = "urn:hl7-org:v3:code/urn:hl7-org:v3:originalText/text()";
	private static final String ID = "urn:hl7-org:v3:id/@root";
	private static final String CODE = "urn:hl7-org:v3:code";

	public ArrayList<Node> allMeasurementNodes = new ArrayList<Node>();
	public String organizerName;
	public ArrayList<HL7ObservationDCM> measurements;
	public ArrayList<HL7OntologyTerm> hl7OntologyTermOrganizer;

	public HL7OrganizerDCM(Node organizer, XPath xpath) throws Exception
	{
		this.organizer = organizer;
		this.xpath = xpath;
		readOrganizerName();
		readOriginalText();
		readID();

		NodeList ontologyTermCode = (NodeList) xpath.evaluate(CODE, organizer, XPathConstants.NODESET);

		hl7OntologyTermOrganizer = new ArrayList<HL7OntologyTerm>();

		for (int i = 1; i < ontologyTermCode.getLength(); i++)
		{
			HL7OntologyTerm ot = new HL7OntologyTerm(ontologyTermCode.item(i), xpath);
			hl7OntologyTermOrganizer.add(ot);
		}

		NodeList nodes = (NodeList) xpath.compile(OBSERVATION).evaluate(organizer, XPathConstants.NODESET);
		this.measurements = new ArrayList<HL7ObservationDCM>();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			HL7ObservationDCM meas = new HL7ObservationDCM(nodes.item(i), xpath);
			measurements.add(meas);
		}
	}

	public void readOrganizerName() throws XPathExpressionException
	{

		Node nameNode = (Node) xpath.evaluate(ORGANIZER_NAME, organizer, XPathConstants.NODE);
		this.organizerName = nameNode.getNodeValue();

	}

	public String getHL7OrganizerNameDCM()
	{
		return organizerName;
	}

	public void readOriginalText() throws XPathExpressionException
	{

		Node nameNode = (Node) xpath.evaluate(ORIGINALTEXT, organizer, XPathConstants.NODE);
		this.originalText = nameNode.getNodeValue();
	}

	public String getOriginalText()
	{
		return originalText;
	}

	public void readID() throws XPathExpressionException
	{

		Node nameNode = (Node) xpath.evaluate(ID, organizer, XPathConstants.NODE);
		this.id = nameNode.getNodeValue();

	}

	public String getId()
	{
		return id;
	}

	public ArrayList<HL7OntologyTerm> getHl7OntologyTerms()
	{
		return hl7OntologyTermOrganizer;
	}

}
