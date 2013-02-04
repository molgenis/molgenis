package org.molgenis.lifelines.hl7.generic;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author roankanninga
 */
public class HL7GenericDCM
{

	ArrayList<HL7OrganizerDCM> hl7organizer;

	// private static final String ORGANIZER =
	// "urn:hl7-org:v3:component/urn:hl7-org:v3:organizer/urn:hl7-org:v3:component";
	private static final String ORGANIZER = "urn:hl7-org:v3:component/urn:hl7-org:v3:organizer";

	public HL7GenericDCM(Node parentNode, XPath xpath) throws Exception
	{
		ArrayList<Node> allOrganizerNodes = new ArrayList<Node>();
		NodeList nodes = (NodeList) xpath.compile(ORGANIZER).evaluate(parentNode, XPathConstants.NODESET);

		for (int i = 0; i < nodes.getLength(); i++)
		{
			allOrganizerNodes.add(nodes.item(i));
		}

		hl7organizer = new ArrayList<HL7OrganizerDCM>(allOrganizerNodes.size());

		for (Node y : allOrganizerNodes)
		{
			HL7OrganizerDCM hl7org = new HL7OrganizerDCM(y, xpath);
			hl7organizer.add(hl7org);
		}

	}

	public ArrayList<HL7OrganizerDCM> getHL7OrganizerDCM()
	{
		return hl7organizer;
	}

}
