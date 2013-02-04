package org.molgenis.lifelines.hl7.lra;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author roankanninga
 */
public class HL7StageLRA
{

	ArrayList<HL7OrganizerLRA> hl7organizer;
	ArrayList<HL7ObservationLRA> measurement;

	private static final String ORGANIZER = "urn:hl7-org:v3:component/urn:hl7-org:v3:organizer";

	public HL7StageLRA(Node parentNode, XPath xpath) throws Exception
	{
		System.out.println("STAGE LRA");
		ArrayList<Node> allOrganizerNodes = new ArrayList<Node>();
		NodeList nodes = (NodeList) xpath.compile(ORGANIZER).evaluate(parentNode, XPathConstants.NODESET);
		System.out.println(nodes.getLength());
		for (int i = 0; i < nodes.getLength(); i++)
		{
			allOrganizerNodes.add(nodes.item(i));
		}

		hl7organizer = new ArrayList<HL7OrganizerLRA>(allOrganizerNodes.size());

		for (Node y : allOrganizerNodes)
		{
			HL7OrganizerLRA hl7org = new HL7OrganizerLRA(y, xpath);
			hl7organizer.add(hl7org);
		}
	}

	public ArrayList<HL7OrganizerLRA> getHL7OrganizerLRA()
	{
		return hl7organizer;
	}

}
