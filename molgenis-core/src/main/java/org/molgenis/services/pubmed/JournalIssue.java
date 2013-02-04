package org.molgenis.services.pubmed;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

//<JournalIssue CitedMedium="Internet">
//<Volume>9</Volume>
//<Issue>6</Issue>
//<PubDate>
//    <Year>2008</Year>
//
//    <Month>Nov</Month>
//</PubDate>
//</JournalIssue>
@XmlAccessorType(XmlAccessType.FIELD)
public class JournalIssue
{
	public String Volume;
	public String Issue;
	public PubDate PubDate;
}
