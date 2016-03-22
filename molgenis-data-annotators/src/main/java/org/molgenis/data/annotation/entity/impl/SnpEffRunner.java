package org.molgenis.data.annotation.entity.impl;

import static java.io.File.createTempFile;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.support.VcfEffectsMetaData.ALT;
import static org.molgenis.data.support.VcfEffectsMetaData.ANNOTATION;
import static org.molgenis.data.support.VcfEffectsMetaData.CDS_POSITION;
import static org.molgenis.data.support.VcfEffectsMetaData.C_DNA_POSITION;
import static org.molgenis.data.support.VcfEffectsMetaData.DISTANCE_TO_FEATURE;
import static org.molgenis.data.support.VcfEffectsMetaData.ERRORS;
import static org.molgenis.data.support.VcfEffectsMetaData.FEATURE_ID;
import static org.molgenis.data.support.VcfEffectsMetaData.FEATURE_TYPE;
import static org.molgenis.data.support.VcfEffectsMetaData.GENE;
import static org.molgenis.data.support.VcfEffectsMetaData.GENE_ID;
import static org.molgenis.data.support.VcfEffectsMetaData.GENE_NAME;
import static org.molgenis.data.support.VcfEffectsMetaData.HGVS_C;
import static org.molgenis.data.support.VcfEffectsMetaData.HGVS_P;
import static org.molgenis.data.support.VcfEffectsMetaData.ID;
import static org.molgenis.data.support.VcfEffectsMetaData.PROTEIN_POSITION;
import static org.molgenis.data.support.VcfEffectsMetaData.PUTATIVE_IMPACT;
import static org.molgenis.data.support.VcfEffectsMetaData.RANK_TOTAL;
import static org.molgenis.data.support.VcfEffectsMetaData.TRANSCRIPT_BIOTYPE;
import static org.molgenis.data.support.VcfEffectsMetaData.VARIANT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.snpEff.SnpEffResultIterator;
import org.molgenis.data.annotation.utils.JarRunner;
import org.molgenis.data.annotation.utils.JarRunnerImpl;
import org.molgenis.data.annotator.websettings.SnpEffAnnotatorSettings;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.data.support.VcfEffectsMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;

@Component
public class SnpEffRunner
{
	private static final Logger LOG = LoggerFactory.getLogger(SnpEffAnnotator.class);

	private JarRunner jarRunner;
	private String snpEffPath;

	private static final String CHARSET = "UTF-8";

	public static final String NAME = "snpEff";
	public static final String LOF = "LOF";
	public static final String NMD = "NMD";
	public static final String ANN = "ANN";

	public enum Impact
	{
		MODIFIER, LOW, MODERATE, HIGH
	}

	@Autowired
	private Entity snpEffAnnotatorSettings;

	@Autowired
	private UuidGenerator idGenerator;

	public SnpEffRunner()
	{
		this.jarRunner = new JarRunnerImpl();
	}

	public Stream<Entity> getSnpEffects(Iterable<Entity> source)
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
	private Stream<Entity> getSnpEffects(Iterator<Entity> source, final File inputVcf)
	{
		try
		{
			if (!source.hasNext()) return Stream.empty();

			// get meta data by peeking at the first entity (work-around for issue #4701)
			PeekingIterator<Entity> peekingIterator = Iterators.peekingIterator(source);
			EntityMetaData sourceEMD = peekingIterator.peek().getEntityMetaData();

			List<String> params = Arrays.asList("-Xmx2g", getSnpEffPath(), "hg19", "-noStats", "-noLog", "-lof",
					"-canon", "-ud", "0", "-spliceSiteSize", "5");
			File outputVcf = jarRunner.runJar(NAME, params, inputVcf);

			// TODO always output to "snpEff"? won't this overwrite when you do parallel annotations?
			File snpEffOutputWithMetaData = addVcfMetaDataToOutputVcf(outputVcf);
			VcfRepository repo = new VcfRepository(snpEffOutputWithMetaData, "SNPEFF_OUTPUT_VCF_" + inputVcf.getName());

			SnpEffResultIterator snpEffResultIterator = new SnpEffResultIterator(repo.iterator());

			DefaultEntityMetaData effectsEMD = new VcfEffectsMetaData(sourceEMD.getSimpleName() + "_EFFECTS",
					sourceEMD.getPackage(), sourceEMD);

			List<Entity> effectsEntities = Lists.newArrayList();
			peekingIterator.forEachRemaining(sourceEntity -> {
				String chromosome = sourceEntity.getString(VcfRepository.CHROM);
				Long position = sourceEntity.getLong(VcfRepository.POS);

				if (chromosome != null && position != null)
				{
					Entity snpEffEntity = snpEffResultIterator.get(chromosome, position);
					if (snpEffEntity != null)
					{
						effectsEntities.addAll(getSnpEffectsFromSnpEffEntity(sourceEntity, snpEffEntity, effectsEMD));
					}
				}
			});

			return effectsEntities.stream();
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

	// ANN=G|intron_variant|MODIFIER|LOC101926913|LOC101926913|transcript|NR_110185.1|Noncoding|5/5|n.376+9526G>C||||||,G|non_coding_exon_variant|MODIFIER|LINC01124|LINC01124|transcript|NR_027433.1|Noncoding|1/1|n.590G>C||||||;
	private List<Entity> getSnpEffectsFromSnpEffEntity(Entity sourceEntity, Entity snpEffEntity,
			DefaultEntityMetaData effectsEMD)
	{
		String[] annotations = snpEffEntity.getString(SnpEffRunner.ANN).split(Pattern.quote(","), -1);

		// LOF and NMD fields can't be associated with a single allele-gene combination so we log them instead
		String lof = snpEffEntity.getString(SnpEffRunner.LOF);
		String nmd = snpEffEntity.getString(SnpEffRunner.NMD);
		if (lof != null || nmd != null)
		{
			LOG.info("LOF / NMD found for CHROM:{} POS:{} ANN:{} LOF:{} NMD:{} ",
					snpEffEntity.getString(VcfRepository.CHROM), snpEffEntity.getString(VcfRepository.POS),
					snpEffEntity.getString(SnpEffRunner.ANN), lof, nmd);
		}

		List<Entity> effects = Lists.newArrayList();
		for (String annotation : annotations)
		{
			String[] fields = annotation.split(Pattern.quote("|"), -1);

			MapEntity effect = new MapEntity(effectsEMD);

			if (fields.length >= 15)
			{
				effect.set(ID, idGenerator.generateId());
				effect.set(ALT, fields[0]);
				effect.set(GENE, fields[4]);
				effect.set(VARIANT, sourceEntity);

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
				LOG.info("No results for CHROM:{} POS:{} REF:{} ALT:{} ", effect.getString(VcfRepository.CHROM),
						effect.getString(VcfRepository.POS), effect.getString(VcfRepository.REF),
						effect.getString(VcfRepository.ALT));
			}

			effects.add(effect);
		}

		return effects;
	}

	/**
	 * Takes the VCF produced by SnpEff, adds metadata, and returns a file that can be used to create a VcfRepository
	 * 
	 * @param outputVcf
	 * @return
	 * @throws IOException
	 */
	private File addVcfMetaDataToOutputVcf(File outputVcf) throws IOException
	{
		File snpEffOutputWithMetaData = createTempFile(NAME + "_withMetaData", ".vcf");
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(outputVcf.getAbsolutePath()), CHARSET));

		List<String> lines = reader.lines().filter(line -> !line.startsWith("##SnpEff")).collect(toList());
		reader.close();

		FileWriter writer = new FileWriter(snpEffOutputWithMetaData);
		boolean metaDone = false;
		for (String line : lines)
		{
			if (!line.startsWith(VcfRepository.PREFIX) && metaDone == false)
			{
				writer.write(VcfRepository.CHROM + "\t" + VcfRepository.POS + "\t" + VcfRepository.ID + "\t"
						+ VcfRepository.REF + "\t" + VcfRepository.ALT + "\t" + VcfRepository.QUAL + "\t"
						+ VcfRepository.FILTER + "\t" + VcfRepository.INFO + "\n");
				metaDone = true;
			}
			writer.write(line + "\n");
		}
		writer.close();

		return snpEffOutputWithMetaData;
	}

	/**
	 * Converts entities to a VCF file that can be passed to SnpEff.
	 * 
	 * @param source
	 *            the Entities to convert to VCF
	 * @return a VCF file
	 */
	public File getInputVcfFile(Iterator<Entity> it) throws IOException
	{
		File vcf = createTempFile(NAME, ".vcf");
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(vcf), CHARSET)))
		{
			while (it.hasNext())
			{
				Entity entity = it.next();
				StringBuilder builder = new StringBuilder();
				builder.append(entity.getString(VcfRepository.CHROM));
				builder.append("\t");
				builder.append(entity.getString(VcfRepository.POS));
				builder.append("\t.\t");
				builder.append(entity.getString(VcfRepository.REF));
				builder.append("\t");
				builder.append(entity.getString(VcfRepository.ALT));

				if (it.hasNext())
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
	 * @return the path to the SnpEff JAR, or nul
	 */
	public String getSnpEffPath()
	{
		if ((snpEffAnnotatorSettings != null) && (snpEffPath == null))
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
}
