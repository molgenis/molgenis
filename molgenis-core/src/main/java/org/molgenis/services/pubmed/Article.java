package org.molgenis.services.pubmed;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class Article
{
	public Journal Journal;
	public String ArticleTitle;
	public Abstract Abstract;

	@XmlElementWrapper(name = "AuthorList")
	@XmlElement(name = "Author")
	public List<Author> Authors = new ArrayList<Author>();

}
