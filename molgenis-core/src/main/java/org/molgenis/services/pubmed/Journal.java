package org.molgenis.services.pubmed;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

//<ISSN IssnType="Electronic">1477-4054</ISSN>
//<JournalIssue CitedMedium="Internet">
//    <Volume>9</Volume>
//    <Issue>6</Issue>
//    <PubDate>
//        <Year>2008</Year>
//
//        <Month>Nov</Month>
//    </PubDate>
//</JournalIssue>
//<Title>Briefings in bioinformatics</Title>
//<ISOAbbreviation>Brief. Bioinformatics</ISOAbbreviation>

@XmlAccessorType(XmlAccessType.FIELD)
public class Journal
{
	public String Title;
	public JournalIssue JournalIssue;
	public String ISOAbbreviation;
}
