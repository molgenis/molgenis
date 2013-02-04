package org.molgenis.lifelines.hl7;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.molgenis.lifelines.hl7.generic.HL7GenericDCM;
import org.molgenis.lifelines.hl7.generic.HL7ObservationDCM;
import org.molgenis.lifelines.hl7.generic.HL7OrganizerDCM;
import org.molgenis.lifelines.hl7.generic.HL7ValueSetAnswerDCM;
import org.molgenis.lifelines.hl7.generic.HL7ValueSetDCM;
import org.molgenis.lifelines.hl7.lra.HL7OrganizerLRA;
import org.molgenis.lifelines.hl7.lra.HL7StageLRA;
import org.molgenis.lifelines.hl7.lra.HL7ValueSetAnswerLRA;
import org.molgenis.lifelines.hl7.lra.HL7ValueSetLRA;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author roankanninga based on 3 files from LifeLines
 */
public class HL7LLData implements HL7Data
{

	private static final String ORGANIZER = "/urn:hl7-org:v3:catalog/urn:hl7-org:v3:component/urn:hl7-org:v3:organizer/urn:hl7-org:v3:code";
	private static final String VALUESET = "/urn:hl7-org:v3:valueSets/urn:hl7-org:v3:valueSet";
	private HashMap<String, HL7ValueSetLRA> hashValueSetLRA = new HashMap<String, HL7ValueSetLRA>();
	private HashMap<String, HL7ValueSetDCM> hashValueSetDCM = new HashMap<String, HL7ValueSetDCM>();

	XPath xpath;
	public HL7GenericDCM hl7GenericDCM = null;

	public HL7StageLRA hl7StageLRA = null;

	private NodeList readFile(String file, XPath xpath, String xpathExpres) throws Exception
	{

		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(file);
		NodeList nodesFile = (NodeList) xpath.compile(xpathExpres).evaluate(doc, XPathConstants.NODESET);
		return nodesFile;
	}

	public HL7LLData(String file1, String file2) throws Exception
	{
		ArrayList<Node> allOrganizerNodes = new ArrayList<Node>();
		XPathFactory factory = XPathFactory.newInstance();
		this.xpath = factory.newXPath();

		// Normal xml file
		NodeList nodesFile1 = readFile(file1, xpath, ORGANIZER);

		for (int i = 0; i < nodesFile1.getLength(); i++)
		{

			if (nodesFile1.item(i).getAttributes().getNamedItem("code").getNodeValue().equals("Generic"))
			{
				hl7GenericDCM = new HL7GenericDCM(nodesFile1.item(i).getParentNode(), xpath);

			}
			else if (nodesFile1.item(i).getAttributes().getNamedItem("code").getNodeValue().equals("LRA"))
			{
				hl7StageLRA = new HL7StageLRA(nodesFile1.item(i).getParentNode(), xpath);
			}
			else
			{
				System.out.println("Error");
			}
			allOrganizerNodes.add(nodesFile1.item(i));
		}

		ArrayList<String> listOfDCMObservation = new ArrayList<String>();

		for (HL7OrganizerDCM dcm : hl7GenericDCM.getHL7OrganizerDCM())
		{
			for (HL7ObservationDCM l : dcm.measurements)
			{
				listOfDCMObservation.add(l.getDisplayName());
			}
		}

		// Valuesets xml file
		NodeList nodesFile2 = readFile(file2, xpath, VALUESET);

		for (int i = 0; i < nodesFile2.getLength(); i++)
		{

			if (listOfDCMObservation.contains(nodesFile2.item(i).getAttributes().getNamedItem("name").getNodeValue()))
			{
				HL7ValueSetDCM valueSetDCM = new HL7ValueSetDCM(nodesFile2.item(i), xpath);
				hashValueSetDCM.put(valueSetDCM.getValueSetsName(), valueSetDCM);
				System.out.println("protocol " + i + ": " + valueSetDCM.getValueSetsName());
				for (HL7ValueSetAnswerDCM r : valueSetDCM.getListOFAnswers())
				{
					System.out.println("ValuesetAnswerDCM: " + r.getName());
				}
			}
			else
			{

				HL7ValueSetLRA valueSetLRA = new HL7ValueSetLRA(nodesFile2.item(i), xpath);
				hashValueSetLRA.put(valueSetLRA.getValueSetsName(), valueSetLRA);
				System.out.println("protocol " + i + ": " + valueSetLRA.getValueSetsName());
				for (HL7ValueSetAnswerLRA r : valueSetLRA.getListOFAnswers())
				{
					// System.out.println("ValuesetAnswerLRA: " +
					// r.getName()+"\t"+r.getCodeValue());
				}

			}
		}

		System.out.println("Damn it!------------->" + hashValueSetLRA.size());
	}

	public HashMap<String, HL7ValueSetLRA> getHashValueSetLRA()
	{
		return hashValueSetLRA;
	}

	public HashMap<String, HL7ValueSetDCM> getHashValueSetDCM()
	{
		return hashValueSetDCM;
	}

	public ArrayList<HL7OrganizerLRA> getHL7OrganizerLRA()
	{

		return hl7StageLRA.getHL7OrganizerLRA();
	}

	public ArrayList<HL7OrganizerDCM> getHL7OrganizerDCM()
	{

		return hl7GenericDCM.getHL7OrganizerDCM();
	}

	public HL7GenericDCM getHl7GenericDCM()
	{
		return hl7GenericDCM;
	}

	public HL7StageLRA getHl7StageLRA()
	{
		return hl7StageLRA;
	}

}
