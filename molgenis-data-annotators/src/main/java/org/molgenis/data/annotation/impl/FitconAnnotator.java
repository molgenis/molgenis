//package org.molgenis.data.annotation.impl;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
//import org.molgenis.data.Entity;
//import org.molgenis.data.EntityMetaData;
//import org.molgenis.data.annotation.AnnotationService;
//import org.molgenis.data.annotation.utils.AnnotatorUtils;
//import org.molgenis.data.annotation.utils.TabixReader;
//import org.molgenis.data.annotation.VariantAnnotator;
//import org.molgenis.data.vcf.utils.VcfUtils;
//import org.molgenis.data.support.AnnotationServiceImpl;
//import org.molgenis.data.support.DefaultAttributeMetaData;
//import org.molgenis.data.support.DefaultEntityMetaData;
//import org.molgenis.data.support.MapEntity;
//import org.molgenis.data.vcf.VcfRepository;
//import org.molgenis.framework.server.MolgenisSettings;
//import org.molgenis.framework.server.MolgenisSimpleSettings;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.event.ContextRefreshedEvent;
//import org.springframework.stereotype.Component;
//
//@Component("fitconService")
//public class FitconAnnotator extends VariantAnnotator
//{
//	private static final Logger LOG = LoggerFactory.getLogger(FitconAnnotator.class);
//
//	private final MolgenisSettings molgenisSettings;
//	private final AnnotationService annotatorService;
//
//	public static final String FITCONS_LABEL = "fitCons";
//	public static final String FITCONS = VcfRepository.getInfoPrefix() + "fitCons";
//
//	private static final String NAME = "FITCON";
//
//	final List<String> infoFields = Arrays
//			.asList(new String[]
//			{ "##INFO=<ID="
//					+ FITCONS.substring(VcfRepository.getInfoPrefix().length())
//					+ ",Number=1,Type=Float,Description=\"FitCon score. See Gulko, B. et al: A method for calculating probabilities of fitness consequences for point mutations across the human genome. Nat. Genet. 47, 276–283 (2015) http://www.nature.com/doifinder/10.1038/ng.3196." });
//
//	public static final String CADD_ANNOTATED_FILE_LOCATION_PROPERTY = "fitcon_location";
//
//	private volatile TabixReader tabixReader;
//
//	@Autowired
//	public FitconAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService) throws IOException
//	{
//		this.molgenisSettings = molgenisSettings;
//		this.annotatorService = annotatorService;
//	}
//
//	public FitconAnnotator(File caddAnnotatedTsvGzFile, File inputVcfFile, File outputVCFFile) throws Exception
//	{
//		this.molgenisSettings = new MolgenisSimpleSettings();
//		molgenisSettings.setProperty(CADD_ANNOTATED_FILE_LOCATION_PROPERTY, caddAnnotatedTsvGzFile.getAbsolutePath());
//
//		this.annotatorService = new AnnotationServiceImpl();
//
//		tabixReader = new TabixReader(molgenisSettings.getProperty(CADD_ANNOTATED_FILE_LOCATION_PROPERTY));
//		checkTabixReader();
//
//		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");
//
//		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
//		Iterator<Entity> vcfIter = vcfRepo.iterator();
//
//		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields,
//				FITCONS.substring(VcfRepository.getInfoPrefix().length()));
//
//		System.out.println("Now starting to process the data.");
//
//		while (vcfIter.hasNext())
//		{
//			Entity record = vcfIter.next();
//
//			List<Entity> annotatedRecord = annotateEntity(record);
//
//			if (annotatedRecord.size() > 1)
//			{
//				outputVCFWriter.close();
//				vcfRepo.close();
//				throw new Exception("Multiple outputs for " + record.toString());
//			}
//			else if (annotatedRecord.size() == 0)
//			{
//				outputVCFWriter.println(VcfUtils.convertToVCF(record));
//			}
//			else
//			{
//				outputVCFWriter.println(VcfUtils.convertToVCF(annotatedRecord.get(0)));
//			}
//		}
//		outputVCFWriter.close();
//		vcfRepo.close();
//		System.out.println("All done!");
//	}
//
//	private synchronized Map<String, Object> annotateEntityWithFitcon(String chromosome, Long position,
//			String reference, String alternative) throws IOException, InterruptedException
//	{
//		TabixReader.Iterator tabixIterator = null;
//		try
//		{
//			tabixIterator = tabixReader.query(chromosome + ":" + position + "-" + position);
//		}
//		catch (Exception e)
//		{
//			LOG.error("Something went wrong (chromosome not in data?) when querying CADD tabix file for " + chromosome
//					+ " POS: " + position + " REF: " + reference + " ALT: " + alternative + "! skipping...");
//		}
//
//		Double fitcon = null;
//
//		// TabixReaderIterator does not have a hasNext();
//		boolean done = tabixIterator == null;
//		int i = 0;
//		boolean fitconIsNaN = false;
//		Map<String, Object> resultMap = new HashMap<String, Object>();
//
//		// get line(s) from data, we expect 0 (no hit), 1 (specialized files such as 1000G) or 3 (whole genome file), so
//		// 0 to 3 hits
//		while (!done)
//		{
//			String line = null;
//
//			try
//			{
//				line = tabixIterator.next();
//			}
//			catch (net.sf.samtools.SAMFormatException sfx)
//			{
//				LOG.error("Bad GZIP file for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
//						+ " ALT: " + alternative + " LINE: " + line);
//				throw sfx;
//			}
//			catch (NullPointerException npe)
//			{
//				LOG.info("No data for CHROM: " + chromosome + " POS: " + position + " REF: " + reference + " ALT: "
//						+ alternative + " LINE: " + line);
//			}
//
//			if (line != null)
//			{
//				String[] split = null;
//				i++;
//				split = line.split("\t");
//				if (split.length != 116)
//				{
//					LOG.error("bad CADD output for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
//							+ " ALT: " + alternative + " LINE: " + line);
//					continue;
//				}
//				if (split[2].equals(reference) && split[4].equals(alternative))
//				{
//					LOG.info("FitCon scores found for CHROM: " + chromosome + " POS: " + position + " REF: "
//							+ reference + " ALT: " + alternative + " LINE: " + line);
//					try
//					{
//						fitcon = Double.parseDouble(split[38]);
//						done = true;
//					}
//					catch (NumberFormatException nfe)
//					{
//						LOG.error("NumberFormatException for line: " + chromosome + " POS: " + position + " REF: "
//								+ reference + " ALT: " + alternative + " LINE: " + line);
//						
//						fitconIsNaN = true;
//
//
//					}
//
//				}
//				// In some cases, the ref and alt are swapped. If this is the case, the initial if statement above will
//				// fail, we can just check whether such a swapping has occured
//				else if (split[4].equals(reference) && split[2].equals(alternative))
//				{
//					LOG.info("FitCon scores found [swapped REF and ALT!] for CHROM: " + chromosome + " POS: "
//							+ position + " REF: " + reference + " ALT: " + alternative + " LINE: " + line);
//					try
//					{
//						fitcon = Double.parseDouble(split[38]);
//						done = true;
//					}
//					catch (NumberFormatException nfe)
//					{
//						LOG.error("NumberFormatException for line: " + chromosome + " POS: " + position + " REF: "
//								+ reference + " ALT: " + alternative + " LINE: " + line + "FitCon score Value: "
//								+ split[38]);
//						
//						fitconIsNaN = true;
//					}
//
//					done = true;
//				}
//				else
//				{
//					if (i > 3)
//					{
//						// If there are more then three annotations per position it might be double annotated or mapped
//						// to different transcripts
//						// This is not bad if the Fitcon values do not change.
//						if (fitcon == null)
//						{
//							try
//							{
//								fitcon = Double.parseDouble(split[38]);
//								done = true;
//							}
//							catch (NumberFormatException nfe)
//							{
//								LOG.error("NumberFormatException for line: " + chromosome + " POS: " + position
//										+ " REF: " + reference + " ALT: " + alternative + " LINE: " + line
//										+ "FitCon score Value: " + split[38]);
//								
//								fitconIsNaN = true;
//
//
//							}
//						}
//						else
//						{
//							try
//							{
//								if (fitcon != Double.parseDouble(split[38]))
//								{
//									LOG.warn("More than 1 different FitCon score in the CADD file! for CHROM: "
//											+ chromosome + " POS: " + position + " REF: " + reference + " ALT: "
//											+ alternative);
//								
//								}
//								else
//								{
//									try
//									{
//										fitcon = Double.parseDouble(split[38]);
//
//									}
//									catch (NumberFormatException nfe)
//									{
//										LOG.error("NumberFormatException for line: " + chromosome + " POS: " + position
//												+ " REF: " + reference + " ALT: " + alternative + " LINE: " + line
//												+ "FitCon score Value: " + split[38]);
//										
//										fitconIsNaN = true;
//
//
//									}
//								}
//
//							}
//							catch (NumberFormatException nfe)
//							{
//								LOG.error("NumberFormatException for line: " + chromosome + " POS: " + position
//										+ " REF: " + reference + " ALT: " + alternative + " LINE: " + line
//										+ "FitCon score Value: " + split[38]);
//								
//								fitconIsNaN = true;
//
//							}
//						}
//					}
//				}
//			}
//			else if( line == null && fitcon == null && fitconIsNaN  )
//			// case: line == null and fitcon not == null but was not fitcon scored file
//			{
//				
//				done = true;
//			}
//			else{
//				LOG.warn("No hit found in CADD file for CHROM: " + chromosome + " POS: " + position + " REF: "
//						+ reference + " ALT: " + alternative + " query was: " + chromosome + ":" + position + "-" + position);
//				System.exit(1);
//			}
//
//		}
//
//		resultMap.put(FITCONS, fitcon);
//
//		return resultMap;
//	}
//
//	@Override
//	public String getSimpleName()
//	{
//		return NAME;
//	}
//
//	@Override
//	public EntityMetaData getOutputMetaData()
//	{
//		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
//		DefaultAttributeMetaData fitcon = new DefaultAttributeMetaData(FITCONS, FieldTypeEnum.DECIMAL).setLabel(
//				FITCONS_LABEL).setDescription(
//				"FitCon is a tool for estimating the probability that a point mutation at each position in "
//						+ "a genome will influence fitness. These ‘fitness consequence’ (fitCons) "
//						+ "scores serve as evolution-based measures of potential genomic function. "
//						+ "(source: http://www.nature.com/doifinder/10.1038/ng.3196)");
//
//		metadata.addAttributeMetaData(fitcon);
//
//		return metadata;
//	}
//
//	@Override
//	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
//	{
//		checkTabixReader();
//		Map<String, Object> resultMap = annotateEntityWithFitcon(entity.getString(VcfRepository.CHROM),
//				entity.getLong(VcfRepository.POS), entity.getString(VcfRepository.REF),
//				entity.getString(VcfRepository.ALT));
//		return Collections.<Entity> singletonList(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
//	}
//
//	@Override
//	public void onApplicationEvent(ContextRefreshedEvent event)
//	{
//		annotatorService.addAnnotator(this);
//	}
//
//	@Override
//	public boolean annotationDataExists()
//	{
//		return new File(molgenisSettings.getProperty(CADD_ANNOTATED_FILE_LOCATION_PROPERTY)).exists();
//	}
//
//	/**
//	 * Makes sure the tabixReader exists.
//	 */
//	private void checkTabixReader() throws IOException
//	{
//		if (tabixReader == null)
//		{
//			synchronized (this)
//			{
//				if (tabixReader == null)
//				{
//					tabixReader = new TabixReader(molgenisSettings.getProperty(CADD_ANNOTATED_FILE_LOCATION_PROPERTY));
//				}
//			}
//		}
//	}
//
//}
