package org.molgenis.services.pubmed;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class MedlineCitation
{
	@XmlElement
	public String PMID;

	@XmlElement(name = "Article")
	public Article article;

	@XmlElementWrapper(name = "MeshHeadingList")
	@XmlElement(name = "MeshHeading")
	public List<MeshHeading> MeshHeadings = new ArrayList<MeshHeading>();

	public List<Author> authors = new ArrayList<Author>();

	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("pmid=").append(PMID);
		if (article != null)
		{
			strBuilder.append(", title=").append(article.ArticleTitle);

			if (article.Journal != null)
			{
				strBuilder.append(", journal=").append(article.Journal.Title);
				if (article.Journal.JournalIssue != null)
				{
					strBuilder.append(", volume=").append(article.Journal.JournalIssue.Volume);
					strBuilder.append(", issue=").append(article.Journal.JournalIssue.Issue);

					if (article.Journal.JournalIssue.PubDate != null)
					{
						strBuilder.append(", year=").append(article.Journal.JournalIssue.PubDate.Year);
						strBuilder.append(", month=").append(article.Journal.JournalIssue.PubDate.Month);
					}
				}

			}
			for (Author au : article.Authors)
			{
				strBuilder.append('\n').append(au.toString());
			}
			if (article.Abstract != null)
			{
				strBuilder.append("\nabstract=").append(article.Abstract.AbstractText);
			}
		}
		for (MeshHeading mesh : this.MeshHeadings)
		{
			strBuilder.append("\nmesh=").append(mesh.DescriptorName);
		}

		return strBuilder.toString();
	}
}
