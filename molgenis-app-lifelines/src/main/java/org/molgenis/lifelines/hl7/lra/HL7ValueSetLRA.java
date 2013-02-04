package org.molgenis.lifelines.hl7.lra;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HL7ValueSetLRA
{

	private String valueSetsName = "";
	private static final String CODE = "urn:hl7-org:v3:code";
	private List<HL7ValueSetAnswerLRA> listOFAnswers = null;

	public HL7ValueSetLRA(Node node, XPath xpath) throws Exception
	{

		listOFAnswers = new ArrayList<HL7ValueSetAnswerLRA>();
		valueSetsName = node.getAttributes().getNamedItem("name").getNodeValue().trim();
		NodeList nodesFile = (NodeList) xpath.compile(CODE).evaluate(node, XPathConstants.NODESET);
		for (int i = 0; i < nodesFile.getLength(); i++)
		{
			HL7ValueSetAnswerLRA answer = new HL7ValueSetAnswerLRA(nodesFile.item(i), xpath);
			listOFAnswers.add(answer);
		}

	}

	public List<HL7ValueSetAnswerLRA> getListOFAnswers()
	{
		return listOFAnswers;
	}

	public String getValueSetsName()
	{
		return valueSetsName;
	}

}
