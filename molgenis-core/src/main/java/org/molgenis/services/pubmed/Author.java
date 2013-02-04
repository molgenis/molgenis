package org.molgenis.services.pubmed;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
//
//<Author ValidYN="Y">
//<LastName>van Ham</LastName>
//<ForeName>Tjakko J</ForeName>
//
//<Initials>TJ</Initials>
//</Author>

@XmlAccessorType(XmlAccessType.FIELD)
public class Author
{
	String LastName;
	String ForeName;
	String Initials;

	public String toInitials()
	{
		return String.format("%s, %s", LastName, Initials);
	}

	@Override
	public String toString()
	{
		return String.format("author: lastname=%s, forename=%s, initials=%s", LastName, ForeName, Initials);
	}
}
