package org.molgenis.data.annotation.impl;

import org.molgenis.data.vcf.VcfRepository;

/**
 * For backwards compatibility
 **/
public class ThousandGenomesServiceAnnotator
{
	public static final String THGEN_MAF_LABEL = "1KGMAF";
	public static final String THGEN_MAF = VcfRepository.getInfoPrefix() + THGEN_MAF_LABEL;
	public static final String THGEN_DIRECTORY_LOCATION_PROPERTY = "1000G_location";
}
