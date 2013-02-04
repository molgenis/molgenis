package org.molgenis.services.pubmed;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PubmedArticleSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class PubmedArticleSet
{
	@XmlElement(name = "PubmedArticle")
	public List<PubmedArticle> articles = new ArrayList<PubmedArticle>();
}
