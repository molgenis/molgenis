package org.molgenis.services.pubmed;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class PubmedArticle
{
	@XmlElement(name = "MedlineCitation")
	public MedlineCitation MedlineCitation;
}
