package org.molgenis.lifelines.hl7.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Entry
{
	private String typeCode;
	private List<Observation> observations;

	@XmlAttribute
	public String getTypeCode()
	{
		return typeCode;
	}

	public void setTypeCode(String typeCode)
	{
		this.typeCode = typeCode;
	}

	@XmlElement(name = "observation")
	public List<Observation> getObservations()
	{
		return observations;
	}

	public void setObservations(List<Observation> observations)
	{
		this.observations = observations;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Entry [typeCode=").append(typeCode).append(", observations=").append(observations).append("]");
		return builder.toString();
	}
}
