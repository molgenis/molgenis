package org.molgenis.data.vcf.format;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.validation.meta.NameValidator;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.genotype.Allele;
import org.molgenis.genotype.GenotypeDataException;
import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfSample;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaFormat;
import org.molgenis.vcf.meta.VcfMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.util.EntityUtils.getTypedValue;
import static org.molgenis.data.vcf.VcfRepository.NAME;
import static org.molgenis.data.vcf.VcfRepository.ORIGINAL_NAME;
import static org.molgenis.data.vcf.model.VcfAttributes.*;

public class VcfToEntity
{
	private static final Logger LOG = LoggerFactory.getLogger(VcfToEntity.class);
	private static final String[] EMPTY_FORMAT = { "." };

	private final VcfMeta vcfMeta;
	private final VcfAttributes vcfAttributes;
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeFactory attrMetaFactory;
	private final EntityType sampleEntityType;
	private final EntityType entityType;

	/**
	 * Performance: VCF record info column keys of for info columns of type 'Flag'
	 */
	private final Set<String> vcfInfoFlagFieldKeys;

	/**
	 * Performance: VCF record info column ID to attribute name map
	 */
	private final Map<String, String> infoFieldKeyToAttrNameMap;

	public VcfToEntity(String entityTypeId, VcfMeta vcfMeta, VcfAttributes vcfAttributes,
			EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory)
	{
		requireNonNull(entityTypeId);
		this.vcfMeta = requireNonNull(vcfMeta);
		requireNonNull(vcfMeta.getFormatMeta());
		this.vcfAttributes = requireNonNull(vcfAttributes);
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);

		this.vcfInfoFlagFieldKeys = determineVcfInfoFlagFields(vcfMeta);
		this.infoFieldKeyToAttrNameMap = createInfoFieldKeyToAttrNameMap(vcfMeta, entityTypeId);

		this.sampleEntityType = createSampleEntityType(entityTypeId, vcfMeta.getFormatMeta());
		this.entityType = createEntityType(entityTypeId, vcfMeta);
	}

	private EntityType createEntityType(String entityTypeId, VcfMeta vcfMeta)
	{
		Attribute idAttribute = attrMetaFactory.create().setName(INTERNAL_ID).setDataType(STRING);
		idAttribute.setVisible(false);

		EntityType entityType = entityTypeFactory.create(entityTypeId);
		entityType.setLabel(entityTypeId);
		entityType.addAttribute(vcfAttributes.getChromAttribute());
		entityType.addAttribute(vcfAttributes.getAltAttribute());
		entityType.addAttribute(vcfAttributes.getPosAttribute());
		entityType.addAttribute(vcfAttributes.getRefAttribute());
		entityType.addAttribute(vcfAttributes.getFilterAttribute());
		entityType.addAttribute(vcfAttributes.getQualAttribute());
		entityType.addAttribute(vcfAttributes.getIdAttribute());
		entityType.addAttribute(idAttribute, ROLE_ID);

		Attribute infoMetaData = attrMetaFactory.create().setName(INFO).setDataType(COMPOUND).setNillable(true);
		for (VcfMetaInfo info : vcfMeta.getInfoMeta())
		{
			String attrName = toAttributeName(info.getId());
			AttributeType attrType = vcfReaderFormatToMolgenisType(info);
			String attrDescription = StringUtils.isBlank(
					info.getDescription()) ? VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION : info.getDescription();

			Attribute attribute = attrMetaFactory.create()
												 .setName(attrName)
												 .setDataType(attrType)
												 .setDescription(attrDescription)
												 .setAggregatable(true)
												 .setParent(infoMetaData);

			entityType.addAttribute(attribute);
		}
		entityType.addAttribute(infoMetaData);
		if (sampleEntityType != null)
		{
			Attribute samplesAttributeMeta = attrMetaFactory.create()
															.setName(SAMPLES)
															.setDataType(MREF)
															.setRefEntity(sampleEntityType)
															.setLabel("SAMPLES");
			entityType.addAttribute(samplesAttributeMeta);
		}
		return entityType;
	}

	private EntityType createSampleEntityType(String entityTypeId, Iterable<VcfMetaFormat> formatMetaData)
	{
		EntityType result = null;
		if (formatMetaData.iterator().hasNext())
		{
			String sampleEntityTypeId = entityTypeId + "Sample";
			result = entityTypeFactory.create(sampleEntityTypeId);
			result.setLabel(sampleEntityTypeId);
			Attribute idAttr = attrMetaFactory.create().setName(ID).setAggregatable(true).setVisible(false);
			Attribute nameAttr = attrMetaFactory.create()
												.setName(NAME)
												.setDataType(TEXT)
												.setAggregatable(true)
												.setNillable(false);
			Attribute originalNameAttr = attrMetaFactory.create().setName(ORIGINAL_NAME).setDataType(TEXT);

			result.addAttribute(idAttr, ROLE_ID);
			result.addAttribute(nameAttr, ROLE_LABEL, ROLE_LOOKUP);
			for (VcfMetaFormat meta : formatMetaData)
			{
				String name = meta.getId();
				if (NameValidator.KEYWORDS.contains(name) || NameValidator.KEYWORDS.contains(name.toUpperCase()))
				{
					name = name + "_";
				}
				Attribute attr = attrMetaFactory.create()
												.setName(name.replaceAll("[-.*$&%^()#!@?_]", ""))
												.setDataType(vcfFieldTypeToMolgenisFieldType(meta))
												.setAggregatable(true)
												.setLabel(meta.getId());

				result.addAttribute(attr);
			}

			result.addAttribute(originalNameAttr);
		}
		return result;
	}

	private static AttributeType vcfReaderFormatToMolgenisType(VcfMetaInfo vcfMetaInfo)
	{
		String number = vcfMetaInfo.getNumber();
		boolean isListValue;
		try
		{
			isListValue = number.equals("A") || number.equals("R") || number.equals("G") || number.equals(".")
					|| Integer.parseInt(number) > 1;
		}
		catch (NumberFormatException ex)
		{
			throw new GenotypeDataException("Error parsing length of vcf info field. " + number
					+ " is not a valid int or expected preset (A, R, G, .)", ex);
		}
		switch (vcfMetaInfo.getType())
		{
			case CHARACTER:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return STRING;
				}
				return STRING;
			case FLAG:
				return BOOL;
			case FLOAT:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return STRING;
				}
				return DECIMAL;
			case INTEGER:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return STRING;
				}
				return INT;
			case STRING:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return TEXT;
				}
				return TEXT;
			default:
				throw new UnexpectedEnumException(vcfMetaInfo.getType());
		}
	}

	private static AttributeType vcfFieldTypeToMolgenisFieldType(VcfMetaFormat format)
	{
		String number = format.getNumber();
		boolean isListValue;
		try
		{
			isListValue = number.equals("A") || number.equals("R") || number.equals("G") || number.equals(".")
					|| Integer.parseInt(number) > 1;
		}
		catch (NumberFormatException ex)
		{
			throw new GenotypeDataException("Error parsing length of vcf info field. " + number
					+ " is not a valid int or expected preset (A, R, G, .)", ex);
		}
		switch (format.getType())
		{
			case CHARACTER:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return STRING;
				}
				return STRING;
			case FLOAT:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return STRING;
				}
				return DECIMAL;
			case INTEGER:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return STRING;
				}
				return INT;
			case STRING:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return STRING;
				}
				return STRING;
			default:
				throw new UnexpectedEnumException(format.getType());
		}
	}

	public Entity toEntity(String[] tokens)
	{
		return toEntity(new VcfRecord(vcfMeta, tokens));
	}

	public Entity toEntity(VcfRecord vcfRecord)
	{
		Entity entity = new DynamicEntity(entityType);
		entity.set(CHROM, vcfRecord.getChromosome());
		entity.set(ALT, StringUtils.join(Lists.transform(vcfRecord.getAlternateAlleles(), Allele::toString), ','));
		entity.set(POS, vcfRecord.getPosition());
		entity.set(REF, vcfRecord.getReferenceAllele().toString());
		entity.set(FILTER, vcfRecord.getFilterStatus());
		entity.set(QUAL, vcfRecord.getQuality());
		entity.set(ID, StringUtils.join(vcfRecord.getIdentifiers(), ','));

		String id = VcfUtils.createId(entity);
		entity.set(INTERNAL_ID, id);

		writeInfoFieldsToEntity(vcfRecord, entity);
		if (sampleEntityType != null)
		{
			List<Entity> samples = createSampleEntities(vcfRecord, entity.get(POS) + "_" + entity.get(ALT), id);
			entity.set(SAMPLES, samples);
		}
		return entity;
	}

	private List<Entity> createSampleEntities(VcfRecord vcfRecord, String entityPosAlt, String entityId)
	{
		List<Entity> samples = new ArrayList<>();
		Iterator<VcfSample> sampleIterator = vcfRecord.getSamples().iterator();
		if (vcfRecord.getNrSamples() > 0)
		{
			Iterator<String> sampleNameIterator = vcfMeta.getSampleNames().iterator();
			for (int j = 0; sampleIterator.hasNext(); ++j)
			{
				String[] format = vcfRecord.getFormat();
				VcfSample sample = sampleIterator.next();
				Entity sampleEntity = new DynamicEntity(sampleEntityType);
				for (int i = 0; i < format.length; i = i + 1)
				{
					String strValue = sample.getData(i);
					Object value = null;
					EntityType sampleEntityType = sampleEntity.getEntityType();
					Attribute attr = sampleEntityType.getAttribute(format[i]);
					if (attr != null)
					{
						if (strValue != null)
						{
							value = getTypedValue(strValue, attr);
						}
					}
					else
					{
						if (Arrays.equals(EMPTY_FORMAT, format))
						{
							LOG.debug("Found a dot as format, assuming no samples present");
						}
						else
						{
							throw new MolgenisDataException("Sample entity contains an attribute [" + format[i]
									+ "] which is not specified in vcf headers");
						}
					}
					sampleEntity.set(format[i], value);
				}
				sampleEntity.set(ID, entityId + j);

				// FIXME remove entity ID from Sample label after #1400 is fixed, see also:
				// jquery.molgenis.table.js line 152
				String original_name = sampleNameIterator.next();
				sampleEntity.set(NAME, entityPosAlt + "_" + original_name);
				sampleEntity.set(ORIGINAL_NAME, original_name);
				samples.add(sampleEntity);
			}
		}
		return samples;
	}

	private void writeInfoFieldsToEntity(VcfRecord vcfRecord, Entity entity)
	{
		// Set default values for VCF info fields of type 'flag' to false. Note that VcfInfo of a VcfRecord do not
		// have to contain all flag fields.
		for (String vcfInfoFlagFieldKey : vcfInfoFlagFieldKeys)
		{
			entity.set(toAttributeName(vcfInfoFlagFieldKey), false);
		}

		for (VcfInfo vcfInfo : vcfRecord.getInformation())
		{
			if (vcfInfo.getKey().equals(".")) // value not available
			{
				continue;
			}

			Object val;
			if (vcfInfoFlagFieldKeys.contains(vcfInfo.getKey()))
			{
				val = true;
			}
			else
			{
				Object vcfInfoVal = vcfInfo.getVal();
				if (vcfInfoVal == null)
				{
					val = null;
				}
				else if (vcfInfoVal instanceof List<?>)
				{
					List<?> vcfInfoValTokens = (List<?>) vcfInfoVal;
					// TODO Use list data type once available (see http://www.molgenis.org/ticket/2681)
					val = vcfInfoValTokens.stream()
										  .map(vcfInfoValToken ->
												  vcfInfoValToken != null ? vcfInfoValToken.toString() : ".")
										  .collect(joining(","));
				}
				else if (vcfInfoVal instanceof Float)
				{
					if (Float.isNaN((Float) vcfInfoVal))
					{
						val = null;
					}
					else
					{
						val = new BigDecimal(
								String.valueOf(vcfInfoVal)).doubleValue(); // TODO why not Double.valueOf(string)?
					}
				}
				else if (vcfInfoVal instanceof Character)
				{
					val = vcfInfoVal.toString();
				}
				else
				{
					val = vcfInfoVal; // VCF value type matches type expected for this MOLGENIS attribute type
				}
			}

			entity.set(toAttributeName(vcfInfo.getKey()), val);
		}
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	/**
	 * Returns the corresponding attribute name for a VCF info field key
	 *
	 * @param vcfInfoFieldKey VCF info field key
	 * @return MOLGENIS attribute name
	 * @throws RuntimeException if no attribute could be found for a VCF info field key
	 */
	private String toAttributeName(String vcfInfoFieldKey)
	{
		String attrName = infoFieldKeyToAttrNameMap.get(vcfInfoFieldKey);
		if (attrName == null)
		{
			throw new RuntimeException(format("Missing attribute for VCF info field [%s]", vcfInfoFieldKey));
		}
		return attrName;
	}

	/**
	 * Returns a set of all possible VCF info fields of type 'Flag'
	 *
	 * @param vcfMeta VCF metadata
	 * @return Set of VCF info fields of type 'Flag'
	 */
	private static Set<String> determineVcfInfoFlagFields(VcfMeta vcfMeta)
	{
		return stream(vcfMeta.getInfoMeta().spliterator(), false).filter(
				vcfInfoMeta -> vcfInfoMeta.getType().equals(VcfMetaInfo.Type.FLAG))
																 .map(VcfMetaInfo::getId)
																 .collect(toSet());
	}

	/**
	 * Returns a mapping of VCF info field keys to MOLGENIS attribute names
	 *
	 * @param vcfMeta      VCF metadata
	 * @param entityTypeId entity name (that could be used to create a MOLGENIS attribute name)
	 * @return map of VCF info field keys to MOLGENIS attribute names
	 */
	private static Map<String, String> createInfoFieldKeyToAttrNameMap(VcfMeta vcfMeta, String entityTypeId)
	{
		Map<String, String> infoFieldIdToAttrNameMap = newHashMapWithExpectedSize(size(vcfMeta.getInfoMeta()));
		for (VcfMetaInfo info : vcfMeta.getInfoMeta())
		{
			// according to the VCF standard it is allowed to have info columns with names that equal default VCF cols.
			// rename these info columns in the meta data to prevent collisions.
			String postFix = "";
			switch (info.getId())
			{
				case INTERNAL_ID:
				case CHROM:
				case ALT:
				case POS:
				case REF:
				case FILTER:
				case QUAL:
				case ID:
					postFix = '_' + entityTypeId;
					break;
				default:
					break;
			}

			String name = info.getId();
			if (NameValidator.KEYWORDS.contains(name) || NameValidator.KEYWORDS.contains(name.toUpperCase()))
			{
				name = name + '_';
			}
			infoFieldIdToAttrNameMap.put(info.getId(), name + postFix);
		}
		return infoFieldIdToAttrNameMap;
	}
}
