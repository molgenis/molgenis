package org.molgenis.data.support;

import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@Component
public class GenomicDataSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;

	private static final String ID = "genomicdata";

	public GenomicDataSettings()
	{
		super(ID);
	}

	public AttributeMetaData getAttributeMetadataForAttributeNameArray(String propertyName, EntityMetaData metadata)
	{
		String attrNamesStr = getString(propertyName);
		if (attrNamesStr != null)
		{
			Set<String> attrNames = Sets.newHashSet(attrNamesStr.split(","));
			for (AttributeMetaData attr : metadata.getAttributes())
			{
				if (attrNames.contains(attr.getName()))
				{
					return attr;
				}
			}
		}
		return null;
	}

	public String getAttributeNameForAttributeNameArray(String propertyName, EntityMetaData metadata)
	{
		AttributeMetaData attribute = getAttributeMetadataForAttributeNameArray(propertyName, metadata);
		if (attribute != null)
		{
			return attribute.getName();
		}
		return "";
	}

	@Component
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String ATTRS_POS = "start";
		public static final String ATTRS_CHROM = "chromosome";
		public static final String ATTRS_REF = "ref";
		public static final String ATTRS_ALT = "alt";
		public static final String ATTRS_IDENTIFIER = "identifier";
		public static final String ATTRS_STOP = "stop";
		public static final String ATTRS_DESCRIPTION = "description";
		public static final String ATTRS_PATIENT_ID = "patient_id";
		public static final String ATTRS_NAME = "name";
		public static final String ATTRS_LINKOUT = "linkout";

		private static final String DEFAULT_ATTRS_POS = "POS,start_nucleotide";
		private static final String DEFAULT_ATTRS_CHROM = "CHROM,#CHROM,chromosome";
		private static final String DEFAULT_ATTRS_REF = "REF";
		private static final String DEFAULT_ATTRS_ALT = "ALT";
		private static final String DEFAULT_ATTRS_ID = "ID,Mutation_id";
		private static final String DEFAULT_ATTRS_STOP = "stop_pos,stop_nucleotide,end_nucleotide";
		private static final String DEFAULT_ATTRS_DESCRIPTION = "INFO";
		private static final String DEFAULT_ATTRS_PATIENT_ID = "patient_id";

		public Meta()
		{
			super(ID);
			setLabel("Genomic data settings");
			setDescription("Settings for genomic data sets.");

			addAttribute(ATTRS_POS).setNillable(false).setDefaultValue(DEFAULT_ATTRS_POS).setLabel("Start nucleotide")
					.setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_CHROM).setNillable(false).setDefaultValue(DEFAULT_ATTRS_CHROM).setLabel("Chromosome")
					.setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_REF).setNillable(false).setDefaultValue(DEFAULT_ATTRS_REF).setLabel("Reference base(s)")
					.setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_ALT).setNillable(false).setDefaultValue(DEFAULT_ATTRS_ALT).setLabel("Alternate base(s)")
					.setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_IDENTIFIER).setNillable(false).setDefaultValue(DEFAULT_ATTRS_ID).setLabel("Identifier")
					.setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_STOP).setNillable(false).setDefaultValue(DEFAULT_ATTRS_STOP).setLabel("End nucleotide")
					.setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_DESCRIPTION).setNillable(false).setDefaultValue(DEFAULT_ATTRS_DESCRIPTION)
					.setLabel("Description").setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_PATIENT_ID).setNillable(false).setDefaultValue(DEFAULT_ATTRS_PATIENT_ID)
					.setLabel("Patient identifier").setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_NAME).setNillable(true).setLabel("Name")
					.setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_LINKOUT).setNillable(true).setLabel("Link out")
					.setDescription("Comma-separated attribute names");
		}

		@Override
		protected Entity getDefaultSettings()
		{
			// FIXME workaround for https://github.com/molgenis/molgenis/issues/1810
			MapEntity defaultSettings = new MapEntity(this);
			defaultSettings.set(ATTRS_POS, DEFAULT_ATTRS_POS);
			defaultSettings.set(ATTRS_CHROM, DEFAULT_ATTRS_CHROM);
			defaultSettings.set(ATTRS_REF, DEFAULT_ATTRS_REF);
			defaultSettings.set(ATTRS_ALT, DEFAULT_ATTRS_ALT);
			defaultSettings.set(ATTRS_IDENTIFIER, DEFAULT_ATTRS_ID);
			defaultSettings.set(ATTRS_STOP, DEFAULT_ATTRS_STOP);
			defaultSettings.set(ATTRS_DESCRIPTION, DEFAULT_ATTRS_DESCRIPTION);
			defaultSettings.set(ATTRS_PATIENT_ID, DEFAULT_ATTRS_PATIENT_ID);
			return defaultSettings;
		}
	}
}
