package org.molgenis.data.vcf.format;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.MetaValidationUtils;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.genotype.Allele;
import org.molgenis.genotype.GenotypeDataException;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfSample;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaFormat;
import org.molgenis.vcf.meta.VcfMetaInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.vcf.VcfRepository.NAME;
import static org.molgenis.data.vcf.VcfRepository.ORIGINAL_NAME;
import static org.molgenis.data.vcf.model.VcfAttributes.*;
import static org.molgenis.util.EntityUtils.getTypedValue;

public class VcfToEntity
{
	private final VcfMeta vcfMeta;
	private final VcfAttributes vcfAttributes;
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeFactory attrMetaFactory;
	private final EntityType entityType;
	private final EntityType sampleEntityType;

	public VcfToEntity(String entityName, VcfMeta vcfMeta, VcfAttributes vcfAttributes,
			EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory)
	{
		this.vcfMeta = requireNonNull(vcfMeta);
		this.vcfAttributes = requireNonNull(vcfAttributes);
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);

		sampleEntityType = createSampleEntityType(requireNonNull(entityName),
				requireNonNull((vcfMeta.getFormatMeta())));
		entityType = createEntityType(entityName, vcfMeta);
	}

	private EntityType createEntityType(String entityName, VcfMeta vcfMeta)
	{
		Attribute idAttribute = attrMetaFactory.create().setName(INTERNAL_ID).setDataType(STRING);
		idAttribute.setVisible(false);

		EntityType entityType = entityTypeFactory.create().setSimpleName(entityName);
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
					postFix = '_' + entityName;
					break;
				default:
					break;
			}

			String name = info.getId();
			if (MetaValidationUtils.KEYWORDS.contains(name) || MetaValidationUtils.KEYWORDS
					.contains(name.toUpperCase()))
			{
				name = name + "_";
			}
			Attribute attribute = attrMetaFactory.create().setName(name + postFix)
					.setDataType(vcfReaderFormatToMolgenisType(info)).setAggregatable(true).setParent(infoMetaData);

			attribute.setDescription(
					StringUtils.isBlank(info.getDescription()) ? VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION : info
							.getDescription());
			entityType.addAttribute(attribute);
		}
		entityType.addAttribute(infoMetaData);
		if (sampleEntityType != null)
		{
			Attribute samplesAttributeMeta = attrMetaFactory.create().setName(SAMPLES).setDataType(MREF)
					.setRefEntity(sampleEntityType).setLabel("SAMPLES");
			entityType.addAttribute(samplesAttributeMeta);
		}
		return entityType;
	}

	private EntityType createSampleEntityType(String entityName, Iterable<VcfMetaFormat> formatMetaData)
	{
		EntityType result = null;
		if (formatMetaData.iterator().hasNext())
		{
			result = entityTypeFactory.create().setSimpleName(entityName + "_Sample");

			Attribute idAttr = attrMetaFactory.create().setName(ID).setAggregatable(true).setVisible(false);
			Attribute nameAttr = attrMetaFactory.create().setName(NAME).setDataType(TEXT).setAggregatable(true);
			Attribute originalNameAttr = attrMetaFactory.create().setName(ORIGINAL_NAME).setDataType(TEXT);

			result.addAttribute(idAttr, ROLE_ID);
			result.addAttribute(nameAttr, ROLE_LABEL, ROLE_LOOKUP);
			for (VcfMetaFormat meta : formatMetaData)
			{
				String name = meta.getId();
				if (MetaValidationUtils.KEYWORDS.contains(name) || MetaValidationUtils.KEYWORDS
						.contains(name.toUpperCase()))
				{
					name = name + "_";
				}
				Attribute attr = attrMetaFactory.create().setName(name.replaceAll("[-.*$&%^()#!@?]", "_"))
						.setDataType(vcfFieldTypeToMolgenisFieldType(meta)).setAggregatable(true)
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
				throw new MolgenisDataException(format("Unknown vcf info type [%s]", vcfMetaInfo.getType()));
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
				throw new MolgenisDataException(format("Unknown vcf field type [%s]", format.getType()));
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
					if (strValue != null)
					{
						value = getTypedValue(strValue, sampleEntity.getEntityType().getAttribute(format[i]));
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
		// set all flag fields default on false.
		for (VcfMetaInfo info : vcfMeta.getInfoMeta())
		{
			String postFix = "";
			List<String> names = new ArrayList<>();
			for (Attribute attribute : entityType.getAttributes())
			{
				if (attribute.getName().equals(info.getId()))
				{
					names.add(attribute.getName());
				}
			}
			if (names.contains(info.getId())) postFix = "_" + entity.getEntityType().getName();
			if (info.getType().equals(VcfMetaInfo.Type.FLAG))
			{
				entity.set(info.getId() + postFix, false);
			}
		}

		for (VcfInfo vcfInfo : vcfRecord.getInformation())
		{
			String postFix = "";
			List<String> names = new ArrayList<>();
			for (Attribute attribute : entityType.getAttributes())
			{
				if (attribute.getName().equals(vcfInfo.getKey()))
				{
					names.add(attribute.getName());
				}
			}
			if (vcfInfo.getKey().equals("."))
			{
				continue;
			}
			Object val = vcfInfo.getVal();
			if (val instanceof List<?>)
			{
				// TODO support list of primitives datatype
				val = StringUtils.join((List<?>) val, ',');
			}
			if (val instanceof Float)
			{
				if (Float.isNaN((Float) val))
				{
					val = null;
				}
				else if (val != null)
				{
					val = new BigDecimal(String.valueOf(val)).doubleValue();
				}

			}
			// if a flag field exists in the line, then this field is true, although the value is null

			if (val == null)
			{
				if (names.contains(vcfInfo.getKey()))
				{
					postFix = "_" + entity.getEntityType().getName();
				}
				if (!(vcfInfo.getKey() + postFix).equals(".")
						&& entityType.getAttribute(vcfInfo.getKey() + postFix) != null && entityType
						.getAttribute(vcfInfo.getKey() + postFix).getDataType().equals(AttributeType.BOOL))
				{
					val = true;
				}
			}
			if (val != null)
			{
				entity.set(vcfInfo.getKey() + postFix, val);
			}
		}
	}

	public EntityType getEntityType()
	{
		return entityType;
	}
}
