package org.molgenis.data.vcf;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public enum VcfFileExtensions
{
	VCF("vcf"), VCF_GZ("vcf.gz"), VCF_ZIP("vcf.zip");

	private String name;

	VcfFileExtensions(String name)
	{
		this.name = name;
	}

	public static Set<String> getVCF()
	{
		return ImmutableSet.of(VCF.toString(), VCF_GZ.toString(), VCF_ZIP.toString());
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
