package org.molgenis.genomebrowser.meta;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class GenomeBrowserAttributesMetadata extends SystemEntityType
{
	public static final String SIMPLE_NAME = "GenomeBrowserAttributes";

	public static final String GENOMEBROWSERATTRIBUTES =
			GenomeBrowserPackage.PACKAGE_GENOME_BROWSER + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "id";
	public static final String DEFAULT = "default";
	public static final String ORDER = "order";
	public static final String POS = "pos";
	public static final String CHROM = "chr";
	public static final String REF = "ref";
	public static final String ALT = "alt";
	public static final String STOP = "stop";

	private final GenomeBrowserPackage genomeBrowserPackage;

	public GenomeBrowserAttributesMetadata(GenomeBrowserPackage genomeBrowserPackage)
	{
		super(SIMPLE_NAME, GenomeBrowserPackage.PACKAGE_GENOME_BROWSER);
		this.genomeBrowserPackage = genomeBrowserPackage;
	}

	@Override
	protected void init()
	{
		setLabel("Genome Browser Attributes");
		setPackage(genomeBrowserPackage);
		addAttribute(IDENTIFIER, ROLE_ID).setLabel("Identifier").setAuto(false).setNillable(false);
		addAttribute(DEFAULT).setDataType(AttributeType.BOOL).setNillable(false).setLabel("Default configuration");
		addAttribute(ORDER).setDataType(AttributeType.INT)
						   .setVisibleExpression("$('" + DEFAULT + "').eq(true).value()")
						   .setUnique(true)
						   .setLabel("Order")
						   .setDescription(
								   "The order in which the attributes of an entity are matched to these configurations, first match wins");
		addAttribute(POS).setDataType(AttributeType.STRING).setNillable(false).setLabel("Position attribute name");
		addAttribute(CHROM).setDataType(AttributeType.STRING).setNillable(false).setLabel("Chromosome attribute name");
		addAttribute(REF).setDataType(AttributeType.STRING).setLabel("Reference allele attribute name");
		addAttribute(ALT).setDataType(AttributeType.STRING).setLabel("Alternative allele attribute name");
		addAttribute(STOP).setDataType(AttributeType.STRING).setLabel("Stop position attribute name");
	}
}
