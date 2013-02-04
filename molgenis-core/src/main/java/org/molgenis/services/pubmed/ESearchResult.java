package org.molgenis.services.pubmed;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlRootElement(name = "eSearchResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class ESearchResult
{
	@XmlElement(name = "Id")
	@XmlElementWrapper(name = "IdList")
	public List<Integer> idList = new ArrayList<Integer>();
}
