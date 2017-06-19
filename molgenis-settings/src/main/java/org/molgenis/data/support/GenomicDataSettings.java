package org.molgenis.data.support;

import com.google.common.collect.Sets;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class GenomicDataSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;

	private static final String ID = "genomicdata";

	public GenomicDataSettings()
	{
		super(ID);
	}

	public Attribute getAttributeMetadataForAttributeNameArray(String propertyName, EntityType entityType)
	{
		String attrNamesStr = getString(propertyName);
		if (attrNamesStr != null)
		{
			Set<String> attrNames = Sets.newHashSet(attrNamesStr.split(","));
			for (Attribute attr : entityType.getAtomicAttributes())
			{
				if (attrNames.contains(attr.getName()))
				{
					return attr;
				}
			}
		}
		return null;
	}

	public String getAttributeNameForAttributeNameArray(String propertyName, EntityType metadata)
	{
		Attribute attribute = getAttributeMetadataForAttributeNameArray(propertyName, metadata);
		if (attribute != null)
		{
			return attribute.getName();
		}
		return "";
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
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
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("Genomic data settings");
			setDescription("Settings for genomic data sets.");

			addAttribute(ATTRS_POS).setNillable(false)
								   .setDefaultValue(DEFAULT_ATTRS_POS)
								   .setLabel("Start nucleotide")
								   .setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_CHROM).setNillable(false)
									 .setDefaultValue(DEFAULT_ATTRS_CHROM)
									 .setLabel("Chromosome")
									 .setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_REF).setNillable(false)
								   .setDefaultValue(DEFAULT_ATTRS_REF)
								   .setLabel("Reference base(s)")
								   .setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_ALT).setNillable(false)
								   .setDefaultValue(DEFAULT_ATTRS_ALT)
								   .setLabel("Alternate base(s)")
								   .setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_IDENTIFIER).setNillable(false)
										  .setDefaultValue(DEFAULT_ATTRS_ID)
										  .setLabel("Identifier")
										  .setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_STOP).setNillable(false)
									.setDefaultValue(DEFAULT_ATTRS_STOP)
									.setLabel("End nucleotide")
									.setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_DESCRIPTION).setNillable(false)
										   .setDefaultValue(DEFAULT_ATTRS_DESCRIPTION)
										   .setLabel("Description")
										   .setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_PATIENT_ID).setNillable(false)
										  .setDefaultValue(DEFAULT_ATTRS_PATIENT_ID)
										  .setLabel("Patient identifier")
										  .setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_NAME).setNillable(true)
									.setLabel("Name")
									.setDescription("Comma-separated attribute names");
			addAttribute(ATTRS_LINKOUT).setNillable(true)
									   .setLabel("Link out")
									   .setDescription("Comma-separated attribute names");
		}
	}

	public String getAttrsPos()
	{
		return getString(Meta.ATTRS_POS);
	}

	public void setAttrsPos(String attrsPos)
	{
		set(Meta.ATTRS_POS, attrsPos);
	}

	public String getAttrsChrom()
	{
		return getString(Meta.ATTRS_CHROM);
	}

	public void setAttrsChrom(String attrsChrom)
	{
		set(Meta.ATTRS_CHROM, attrsChrom);
	}

	public String getAttrsRef()
	{
		return getString(Meta.ATTRS_REF);
	}

	public void setAttrsRef(String attrsRef)
	{
		set(Meta.ATTRS_REF, attrsRef);
	}

	public String getAttrsAlt()
	{
		return getString(Meta.ATTRS_ALT);
	}

	public void setAttrsAlt(String attrsAlt)
	{
		set(Meta.ATTRS_ALT, attrsAlt);
	}

	public String getAttrsIdentifier()
	{
		return getString(Meta.ATTRS_IDENTIFIER);
	}

	public void setAttrsIdentifier(String attrsIdentifier)
	{
		set(Meta.ATTRS_IDENTIFIER, attrsIdentifier);
	}

	public String getAttrsStop()
	{
		return getString(Meta.ATTRS_STOP);
	}

	public void setAttrsStop(String attrsStop)
	{
		set(Meta.ATTRS_STOP, attrsStop);
	}

	public String getAttrsDescription()
	{
		return getString(Meta.ATTRS_DESCRIPTION);
	}

	public void setAttrsDescription(String attrsDescription)
	{
		set(Meta.ATTRS_DESCRIPTION, attrsDescription);
	}

	public String getAttrsPatientId()
	{
		return getString(Meta.ATTRS_PATIENT_ID);
	}

	public void setAttrsPatientId(String attrsPatientId)
	{
		set(Meta.ATTRS_PATIENT_ID, attrsPatientId);
	}

	public String getAttrsName()
	{
		return getString(Meta.ATTRS_NAME);
	}

	public void setAttrsName(String attrsName)
	{
		set(Meta.ATTRS_NAME, attrsName);
	}

	public String getAttrsLinkout()
	{
		return getString(Meta.ATTRS_LINKOUT);
	}

	public void setAttrsLinkout(String attrsLinkout)
	{
		set(Meta.ATTRS_LINKOUT, attrsLinkout);
	}
}
