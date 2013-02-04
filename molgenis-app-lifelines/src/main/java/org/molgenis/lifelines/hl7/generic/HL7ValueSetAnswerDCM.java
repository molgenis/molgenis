package org.molgenis.lifelines.hl7.generic;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.molgenis.lifelines.hl7.HL7OntologyTerm;
import org.w3c.dom.Node;

public class HL7ValueSetAnswerDCM
{

	private static final String DISPLAYNAME = "@displayName";
	private String name = "";
	private HL7OntologyTerm ont;

	public HL7ValueSetAnswerDCM(Node node, XPath xpath) throws Exception
	{

		Node nameNode = (Node) xpath.compile(DISPLAYNAME).evaluate(node, XPathConstants.NODE);
		name = nameNode.getNodeValue().trim();
		ont = new HL7OntologyTerm(node, xpath);
	}

	public String getName()
	{
		return name;
	}

	public HL7OntologyTerm getOnt()
	{
		return ont;
	}
}
