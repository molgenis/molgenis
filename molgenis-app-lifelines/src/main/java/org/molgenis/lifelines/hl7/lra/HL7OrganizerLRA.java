package org.molgenis.lifelines.hl7.lra;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author roankanninga
 */
public class HL7OrganizerLRA
{

	//

	private Node organizer;
	private XPath xpath;
	private static final String OBSERVATION = "urn:hl7-org:v3:component/urn:hl7-org:v3:observation";
	private static final String ORGANIZER_NAME = "urn:hl7-org:v3:code/@code";
	public ArrayList<Node> allMeasurementNodes = new ArrayList<Node>();
	public String organizerName;
	public ArrayList<HL7ObservationLRA> measurements;

	public HL7OrganizerLRA(Node organizer, XPath xpath) throws Exception
	{
		this.organizer = organizer;
		this.xpath = xpath;
		readOrganizerName();

		NodeList nodes = (NodeList) xpath.compile(OBSERVATION).evaluate(organizer, XPathConstants.NODESET);

		this.measurements = new ArrayList<HL7ObservationLRA>();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			HL7ObservationLRA meas = new HL7ObservationLRA(nodes.item(i), xpath);
			measurements.add(meas);
		}
	}

	public void readOrganizerName() throws XPathExpressionException
	{

		Node nameNode = (Node) xpath.evaluate(ORGANIZER_NAME, organizer, XPathConstants.NODE);
		this.organizerName = nameNode.getNodeValue();

	}

	public String getHL7OrganizerNameLRA()
	{
		return organizerName;
	}

}
