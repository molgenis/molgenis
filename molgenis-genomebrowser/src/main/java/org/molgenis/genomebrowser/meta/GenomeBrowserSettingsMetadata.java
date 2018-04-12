package org.molgenis.genomebrowser.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class GenomeBrowserSettingsMetadata extends SystemEntityType
{
	public static final String SIMPLE_NAME = "GenomeBrowserSettings";
	public static final String GENOMEBROWSERSETTINGS =
			GenomeBrowserPackage.PACKAGE_GENOME_BROWSER + PACKAGE_SEPARATOR + SIMPLE_NAME;
	public static final String IDENTIFIER = "id";
	public static final String LABEL = "label";
	public static final String LABEL_ATTR = "labelAttr";
	public static final String ENTITY = "entity";
	public static final String TRACK_TYPE = "track_type";
	public static final String MOLGENIS_REFERENCE_TRACKS = "molgenis_reference_tracks";
	public static final String MOLGENIS_REFERENCES_MODE = "molgenis_reference_tracks_mode";
	public static final String GENOME_BROWSER_ATTRS = "genome_browser_attrs";
	public static final String ACTIONS = "actions";
	public static final String ATTRS = "attrs";
	public static final String SCORE_ATTR = "scoreAttr";
	public static final String EXON_KEY = "exon_key";
	public static final String NUMERIC = "NUMERIC";
	public static final String EXON = "EXON";
	public static final String VARIANT = "VARIANT";
	public static final String ALL = "ALL";
	public static final String CONFIGURED = "CONFIGURED";
	public static final String NONE = "NONE";

	private final GenomeBrowserPackage genomeBrowserPackage;
	private EntityTypeMetadata entityTypeMetadata;
	private AttributeMetadata attributeMetadata;
	private GenomeBrowserAttributesMetadata genomeBrowserAttributesMetadata;

	public GenomeBrowserSettingsMetadata(AttributeMetadata attributeMetadata, EntityTypeMetadata entityTypeMetadata,
			GenomeBrowserAttributesMetadata genomeBrowserAttributesMetadata, GenomeBrowserPackage genomeBrowserPackage)
	{
		super(SIMPLE_NAME, GenomeBrowserPackage.PACKAGE_GENOME_BROWSER);
		this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
		this.attributeMetadata = requireNonNull(attributeMetadata);
		this.genomeBrowserAttributesMetadata = requireNonNull(genomeBrowserAttributesMetadata);
		this.genomeBrowserPackage = genomeBrowserPackage;
	}

	@Override
	protected void init()
	{
		setLabel("Genome Browser Settings");
		setPackage(genomeBrowserPackage);
		addAttribute(IDENTIFIER, ROLE_ID).setLabel("Identifier").setAuto(true).setNillable(false);
		addAttribute(LABEL, ROLE_LABEL).setLabel("Label").setNillable(false);
		addAttribute(ENTITY).setLabel("Entity").setDataType(XREF).setRefEntity(entityTypeMetadata).setNillable(false);
		addAttribute(GENOME_BROWSER_ATTRS).setLabel("Genomic attributes")
										  .setDataType(XREF)
										  .setRefEntity(genomeBrowserAttributesMetadata)
										  .setNillable(false)
										  .setDescription(
												  "Reference to the Genome Browser Attributes entity to tell the genome browser which attributes should be used for chromosome, positions, reference allele and alternative allele");
		addAttribute(LABEL_ATTR).setLabel("Label Attribute")
								.setDataType(XREF)
								.setRefEntity(attributeMetadata)
								.setNillable(false);
		addAttribute(TRACK_TYPE).setLabel("Track type")
								.setDataType(ENUM)
								.setEnumOptions(Arrays.asList(VARIANT, NUMERIC, EXON))
								.setNillable(false)
								.setDescription("The track type which determines how dalliance will render the track");
		addAttribute(EXON_KEY).setLabel("Exon/intron key")
							  .setDescription(
									  "Value to distinguish Exon and intron values, if the key is present in the label attributes we assume this is an exon")
							  .setVisibleExpression("$('" + TRACK_TYPE + "').eq('" + EXON + "').value()");
		addAttribute(SCORE_ATTR).setLabel("Score attributes")
								.setDescription("Name of the attribute that can be used for the score")
								.setVisibleExpression("$('" + TRACK_TYPE + "').eq('" + NUMERIC + "').value()");
		addAttribute(ATTRS).setLabel("Feature popup attributes")
						   .setDescription(
								   "Comma separated list of label:attr pairs that should be shown in the genome browser popup, example: \"'Reference Allele:REF,Alternative Allele:ALT'\"");
		addAttribute(MOLGENIS_REFERENCES_MODE).setLabel("Reference track mode")
											  .setDataType(ENUM)
											  .setEnumOptions(Arrays.asList(ALL, CONFIGURED, NONE))
											  .setNillable(false)
											  .setDescription(
													  "Setting to determine if all suitable molgenis entities should be shown as reference track in the genomebrowser, only those configured, or none");
		addAttribute(MOLGENIS_REFERENCE_TRACKS).setLabel("Reference tracks")
											   .setDataType(MREF)
											   .setRefEntity(this)
											   .setVisibleExpression(
													   "$('" + MOLGENIS_REFERENCES_MODE + "').eq('" + CONFIGURED
															   + "').value()")
											   .setNullableExpression(
													   "!($('" + MOLGENIS_REFERENCES_MODE + "').eq('" + CONFIGURED
															   + "')).value()")
											   .setDescription(
													   "the genome browser settings that should be shown as reference tracks");
		addAttribute(ACTIONS).setLabel("Actions")
							 .setDataType(TEXT)
							 .setDescription(
									 "a javascript array containing object that specify actions to be available in the genome browser popup, format: [{label:\"action1\" run:\"alert('action1')\"},{label:\"action2\" run:\"alert('action2')\"}]");
	}
}
