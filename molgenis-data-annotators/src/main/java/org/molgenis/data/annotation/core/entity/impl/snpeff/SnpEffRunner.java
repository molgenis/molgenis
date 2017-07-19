package org.molgenis.data.annotation.core.entity.impl.snpeff;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.effects.EffectsMetaData;
import org.molgenis.data.annotation.core.utils.JarRunner;
import org.molgenis.data.annotation.web.settings.SnpEffAnnotatorSettings;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import static com.google.common.collect.Iterators.peekingIterator;
import static java.io.File.createTempFile;
import static org.molgenis.data.annotation.core.effects.EffectsMetaData.*;
import static org.molgenis.data.meta.AttributeType.XREF;

@Component
public class SnpEffRunner
{
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeFactory attributeFactory;
	private final VcfAttributes vcfAttributes;

	private static final Logger LOG = LoggerFactory.getLogger(SnpEffAnnotator.class);

	private String snpEffPath;

	private static final String CHARSET = "UTF-8";
	public static final String ENTITY_NAME_SUFFIX = "EFFECTS";

	public static final String NAME = "snpEff";
	public static final String ANN = "ANN";

	private EffectsMetaData effectsMetaData = new EffectsMetaData();

	private final JarRunner jarRunner;
	private final Entity snpEffAnnotatorSettings;
	private final IdGenerator idGenerator;

	@Autowired
	public SnpEffRunner(JarRunner jarRunner, Entity snpEffAnnotatorSettings, IdGenerator idGenerator,
			VcfAttributes vcfAttributes, EffectsMetaData effectsMetaData, EntityTypeFactory entityTypeFactory,
			AttributeFactory attributeFactory)
	{
		this.jarRunner = jarRunner;
		this.snpEffAnnotatorSettings = snpEffAnnotatorSettings;
		this.idGenerator = idGenerator;
		this.vcfAttributes = vcfAttributes;
		this.effectsMetaData = effectsMetaData;
		this.entityTypeFactory = entityTypeFactory;
		this.attributeFactory = attributeFactory;
	}

	public Iterator<Entity> getSnpEffects(Iterable<Entity> source)
	{
		try
		{
			File inputVcf = getInputVcfFile(source.iterator());
			return getSnpEffects(source.iterator(), inputVcf);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Exception making temporary VCF file", e);
		}
	}

	@SuppressWarnings("resource")
	public Iterator<Entity> getSnpEffects(Iterator<Entity> source, final File inputVcf)
	{
		try
		{
			if (!source.hasNext()) return Collections.<Entity>emptyList().iterator();

			// get meta data by peeking at the first entity (work-around for issue #4701)
			PeekingIterator<Entity> peekingSourceIterator = Iterators.peekingIterator(source);
			EntityType sourceEMD = peekingSourceIterator.peek().getEntityType();

			List<String> params = Arrays.asList("-Xmx2g", getSnpEffPath(), "hg19", "-noStats", "-noLog", "-lof",
					"-canon", "-ud", "0", "-spliceSiteSize", "5");
			File outputVcf = jarRunner.runJar(NAME, params, inputVcf);

			VcfRepository repo = new VcfRepository(outputVcf, "SNPEFF_OUTPUT_VCF_" + inputVcf.getName(), vcfAttributes,
					entityTypeFactory, attributeFactory);

			PeekingIterator<Entity> snpEffResultIterator = peekingIterator(repo.iterator());

			return new Iterator<Entity>()
			{
				final LinkedList<Entity> effects = Lists.newLinkedList();

				@Override
				public boolean hasNext()
				{
					return (peekingSourceIterator.hasNext() || !effects.isEmpty());
				}

				@Override
				public Entity next()
				{
					if (effects.isEmpty())
					{
						// go to next source entity and get effects
						Entity sourceEntity = peekingSourceIterator.next();
						String chromosome = sourceEntity.getString(VcfAttributes.CHROM);
						Integer position = sourceEntity.getInt(VcfAttributes.POS);

						if (chromosome != null && position != null)
						{
							Entity snpEffEntity = getSnpEffEntity(snpEffResultIterator, chromosome, position);
							if (snpEffEntity != null)
							{
								effects.addAll(getSnpEffectsFromSnpEffEntity(sourceEntity, snpEffEntity,
										getTargetEntityType(sourceEMD)));
							}
							else
							{
								effects.add(getEmptyEffectsEntity(sourceEntity, getTargetEntityType(sourceEMD)));
							}
						}
						else
						{
							effects.add(getEmptyEffectsEntity(sourceEntity, getTargetEntityType(sourceEMD)));
						}
					}
					return effects.removeFirst();
				}

			};
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		catch (InterruptedException e)
		{
			throw new MolgenisDataException("Exception running SnpEff", e);
		}
	}

	/**
	 * Returns the next entity containing SnpEff annotations if its Chrom and Pos match. This implementation works
	 * because SnpEff always returns output in the same order as the input
	 *
	 * @param snpEffResultIterator the snpEff results
	 * @param chromosome           chromosome for the entity that is being annotated
	 * @param position             position for the entity that is being annotated
	 * @return {@link Entity}
	 */
	private Entity getSnpEffEntity(PeekingIterator<Entity> snpEffResultIterator, String chromosome, int position)
	{
		if (snpEffResultIterator.hasNext())
		{
			Entity entityCandidate = snpEffResultIterator.peek();
			if (chromosome.equals(entityCandidate.getString(VcfAttributes.CHROM)) && position == entityCandidate.getInt(
					VcfAttributes.POS) && entityCandidate.getString(SnpEffRunner.ANN) != null)
			{
				snpEffResultIterator.next();
				return entityCandidate;
			}
		}
		return null;
	}

	private Entity getEmptyEffectsEntity(Entity sourceEntity, EntityType effectsEMD)
	{
		Entity effect = new DynamicEntity(effectsEMD);
		effect.set(ID, idGenerator.generateId());
		effect.set(VARIANT, sourceEntity);

		return effect;
	}

	// ANN=G|intron_variant|MODIFIER|LOC101926913|LOC101926913|transcript|NR_110185.1|Noncoding|5/5|n.376+9526G>C||||||,G|non_coding_exon_variant|MODIFIER|LINC01124|LINC01124|transcript|NR_027433.1|Noncoding|1/1|n.590G>C||||||;
	private List<Entity> getSnpEffectsFromSnpEffEntity(Entity sourceEntity, Entity snpEffEntity, EntityType effectsEMD)
	{
		String[] annotations = snpEffEntity.getString(SnpEffRunner.ANN).split(Pattern.quote(","), -1);

		List<Entity> effects = Lists.newArrayList();
		for (String annotation : annotations)
		{
			String[] fields = annotation.split(Pattern.quote("|"), -1);

			Entity effect = new DynamicEntity(effectsEMD);

			if (fields.length >= 15)
			{
				effect.set(ID, idGenerator.generateId());
				effect.set(VARIANT, sourceEntity);

				effect.set(ALT, fields[0]);
				effect.set(GENE_NAME, fields[4]);
				effect.set(ANNOTATION, fields[1]);
				effect.set(PUTATIVE_IMPACT, fields[2]);
				effect.set(GENE_NAME, fields[3]);
				effect.set(GENE_ID, fields[4]);
				effect.set(FEATURE_TYPE, fields[5]);
				effect.set(FEATURE_ID, fields[6]);
				effect.set(TRANSCRIPT_BIOTYPE, fields[7]);
				effect.set(RANK_TOTAL, fields[8]);
				effect.set(HGVS_C, fields[9]);
				effect.set(HGVS_P, fields[10]);
				effect.set(C_DNA_POSITION, fields[11]);
				effect.set(CDS_POSITION, fields[12]);
				effect.set(PROTEIN_POSITION, fields[13]);
				effect.set(DISTANCE_TO_FEATURE, fields[14]);
				effect.set(ERRORS, fields[15]);
			}
			else
			{
				LOG.info("No results for CHROM:{} POS:{} REF:{} ALT:{} ", effect.getString(VcfAttributes.CHROM),
						effect.getString(VcfAttributes.POS), effect.getString(VcfAttributes.REF),
						effect.getString(VcfAttributes.ALT));
			}

			effects.add(effect);
		}

		return effects;
	}

	/**
	 * Converts entities to a VCF file that can be passed to SnpEff.
	 *
	 * @param source the Entities to convert to VCF
	 * @return a VCF file
	 */
	public File getInputVcfFile(Iterator<Entity> source) throws IOException
	{
		File vcf = createTempFile(NAME, ".vcf");
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(vcf), CHARSET)))
		{
			bw.write(VcfAttributes.CHROM + "\t" + VcfAttributes.POS + "\t" + VcfAttributes.ID + "\t" + VcfAttributes.REF
					+ "\t" + VcfAttributes.ALT + "\t" + VcfAttributes.QUAL + "\t" + VcfAttributes.FILTER + "\t"
					+ VcfAttributes.INFO + "\n");
			while (source.hasNext())
			{
				Entity entity = source.next();
				StringBuilder builder = new StringBuilder();
				builder.append(entity.getString(VcfAttributes.CHROM));
				builder.append("\t");
				builder.append(entity.getInt(VcfAttributes.POS));
				builder.append("\t.\t");//ID
				builder.append(entity.getString(VcfAttributes.REF));
				builder.append("\t");
				builder.append(entity.getString(VcfAttributes.ALT));
				builder.append("\t.\t");//QUAL
				builder.append("\t.\t");//FILTER
				builder.append(".");//INFO

				if (source.hasNext())
				{
					builder.append("\n");
				}

				bw.write(builder.toString());
			}
		}

		return vcf;
	}

	/**
	 * Gets the path to the SnpEff JAR. Returns null when the path is not found or snpEffAnnotatorSettings is null.
	 *
	 * @return the path to the SnpEff JAR, or null
	 */
	public String getSnpEffPath()
	{
		if (snpEffAnnotatorSettings != null)
		{
			snpEffPath = RunAsSystemProxy.runAsSystem(
					() -> snpEffAnnotatorSettings.getString(SnpEffAnnotatorSettings.Meta.SNPEFF_JAR_LOCATION));

			if (snpEffPath != null)
			{
				File snpEffFile = new File(snpEffPath);
				if (snpEffFile.exists() && snpEffFile.isFile())
				{
					LOG.info("SnpEff found at: " + snpEffFile.getAbsolutePath());
				}
				else
				{
					LOG.debug("SnpEff not found at: " + snpEffFile.getAbsolutePath());
					snpEffPath = null;
				}
			}
		}

		return snpEffPath;
	}

	/**
	 * @param sourceEntityType The entity type for the entity that is being annotated by snpEff
	 * @return entityType Returns the EntityType for the effect entity
	 */
	public EntityType getTargetEntityType(EntityType sourceEntityType)
	{
		EntityType entityType = entityTypeFactory.create()
												 .setId(sourceEntityType.getId() + ENTITY_NAME_SUFFIX)
												 .setLabel(sourceEntityType.getId() + "_" + ENTITY_NAME_SUFFIX)
												 .setPackage(sourceEntityType.getPackage());
		entityType.setBackend(sourceEntityType.getBackend());
		Attribute id = attributeFactory.create()
									   .setName(EffectsMetaData.ID)
									   .setAuto(true)
									   .setVisible(false)
									   .setIdAttribute(true);
		entityType.addAttribute(id);
		for (Attribute attr : effectsMetaData.getOrderedAttributes())
		{
			entityType.addAttribute(attr);
		}
		entityType.addAttribute(attributeFactory.create()
												.setName(EffectsMetaData.VARIANT)
												.setNillable(false)
												.setDataType(XREF)
												.setRefEntity(sourceEntityType));
		return entityType;
	}
}
