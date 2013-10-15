package org.molgenis.omx.biobankconnect.mesh;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DescriptorRecordSet")
public class DescriptorRecordSet
{
	List<DescriptorRecord> descriptorRecords;

	public List<DescriptorRecord> getDescriptorRecords()
	{
		return descriptorRecords;
	}

	@XmlElement(name = "DescriptorRecord")
	public void setDescriptorRecords(List<DescriptorRecord> descriptorRecords)
	{
		this.descriptorRecords = descriptorRecords;
	}
}
