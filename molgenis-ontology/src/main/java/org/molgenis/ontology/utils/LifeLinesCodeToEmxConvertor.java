package org.molgenis.ontology.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.excel.ExcelRepository;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.excel.ExcelSheetWriter;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.excel.ExcelWriter.FileFormat;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;

import com.google.common.collect.FluentIterable;

public class LifeLinesCodeToEmxConvertor
{
	private static final String CODE_SYSTEM = "codesystem";
	private static final String NAME = "name";
	private static final String CODE = "code";
	private static final String TOP_NODE_PATH = "0[0]";
	private static final String TOP_NODE_NAME = "top";

	public static void main(String args[]) throws InvalidFormatException, IOException, MolgenisInvalidFormatException
	{
		if (args.length != 0)
		{
			File lifeLinesCodeFile = new File(args[0]);

			if (lifeLinesCodeFile.exists())
			{
				convert(lifeLinesCodeFile);
			}
		}
		else
		{
			System.out.println("Please provide the file path for the lifelines codes!");
		}
	}

	public static void convert(File lifeLinesCodeFile) throws InvalidFormatException, IOException,
			MolgenisInvalidFormatException
	{
		UuidGenerator uuidGenerator = new UuidGenerator();

		File lifeLinesCodeEmxFile = new File(lifeLinesCodeFile.getParent() + "/"
				+ createLifelinesCodeEmxFileName(lifeLinesCodeFile));

		ExcelWriter excelWriter = new ExcelWriter(lifeLinesCodeEmxFile);

		ExcelSheetWriter ontologyExcelSheet = excelWriter.createWritable(OntologyMetaData.ENTITY_NAME, FluentIterable
				.from(OntologyMetaData.INSTANCE.getAtomicAttributes()).transform(attr -> attr.getName()).toList());

		ExcelSheetWriter ontologyTermExcelSheet = excelWriter.createWritable(
				OntologyTermMetaData.ENTITY_NAME,
				FluentIterable.from(OntologyTermMetaData.INSTANCE.getAtomicAttributes())
						.transform(attr -> attr.getName()).toList());

		ExcelSheetWriter ontologyTermSynonymExcelSheet = excelWriter.createWritable(
				OntologyTermSynonymMetaData.ENTITY_NAME,
				FluentIterable.from(OntologyTermSynonymMetaData.INSTANCE.getAtomicAttributes())
						.transform(attr -> attr.getName()).toList());

		ExcelSheetWriter ontologyTermNodePathExcelSheet = excelWriter.createWritable(
				OntologyTermNodePathMetaData.ENTITY_NAME,
				FluentIterable.from(OntologyTermNodePathMetaData.INSTANCE.getAtomicAttributes())
						.transform(attr -> attr.getName()).toList());

		ExcelRepositoryCollection excelRepositoryCollection = new ExcelRepositoryCollection(lifeLinesCodeFile);

		if (excelRepositoryCollection.getNumberOfSheets() > 0)
		{
			ExcelRepository excelRepository = excelRepositoryCollection.getSheet(0);

			Map<String, String> ontologyToIdMap = new HashMap<String, String>();
			for (Entity entity : excelRepository)
			{
				String codeSystem = entity.getString(CODE_SYSTEM);
				if (!ontologyToIdMap.containsKey(codeSystem))
				{
					String generateId = uuidGenerator.generateId();
					ontologyToIdMap.put(codeSystem, generateId);
					ontologyExcelSheet.add(createOntologyEntity(codeSystem, generateId));
				}
			}

			Map<String, String> ontologyTermSynonymMap = new HashMap<String, String>();
			for (Entity entity : excelRepository)
			{
				String codeSystem = entity.getString(CODE_SYSTEM);
				String code = entity.getString(CODE);
				String codeName = entity.getString(NAME);

				String generateId = uuidGenerator.generateId();
				ontologyTermSynonymExcelSheet.add(createOntologyTermSynonymEntity(codeName, generateId));

				String identifier = codeSystem + code + codeName;
				ontologyTermSynonymMap.put(identifier, generateId);
			}

			int count = 0;
			Map<String, String> ontologyTermNodePathMap = new HashMap<String, String>();
			for (Entity entity : excelRepository)
			{
				String codeSystem = entity.getString(CODE_SYSTEM);
				String code = entity.getString(CODE);
				String codeName = entity.getString(NAME);
				String identifier = codeSystem + code + codeName;
				String nodePath = codeName.equals(TOP_NODE_NAME) ? TOP_NODE_PATH : TOP_NODE_PATH + "." + count + "[1]";
				String generatedId = uuidGenerator.generateId();
				ontologyTermNodePathExcelSheet.add(createOntologyNodePathEntity(nodePath,
						StringUtils.equalsIgnoreCase(TOP_NODE_NAME, codeName), generatedId));
				count++;
				ontologyTermNodePathMap.put(identifier, generatedId);
			}

			for (Entity entity : excelRepository)
			{
				String codeSystem = entity.getString(CODE_SYSTEM);
				String code = entity.getString(CODE);
				String codeName = entity.getString(NAME);
				String identifier = codeSystem + code + codeName;
				String ontologyReferenceId = ontologyToIdMap.get(codeSystem);
				String synonymReferenceId = ontologyTermSynonymMap.get(identifier);
				String nodePathReferenceId = ontologyTermNodePathMap.get(identifier);

				ontologyTermExcelSheet.add(createOntologyTermEntity(codeName, code, ontologyReferenceId,
						synonymReferenceId, nodePathReferenceId, uuidGenerator.generateId()));
			}
		}

		ontologyExcelSheet.close();
		ontologyTermExcelSheet.close();
		ontologyTermSynonymExcelSheet.close();
		ontologyTermNodePathExcelSheet.close();

		excelWriter.close();
	}

	private static Entity createOntologyTermEntity(String ontologyTernMane, String ontologyTermIri, String ontology,
			String synonym, String nodePath, String generatedId)
	{
		MapEntity mapEntity = new MapEntity();
		mapEntity.set(OntologyTermMetaData.ID, generatedId);
		mapEntity.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, ontologyTernMane);
		mapEntity.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, ontologyTermIri);
		mapEntity.set(OntologyTermMetaData.ONTOLOGY, ontology);
		mapEntity.set(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, synonym);
		mapEntity.set(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, nodePath);
		return mapEntity;
	}

	private static Entity createOntologyEntity(String codeSystem, String generatedId)
	{
		MapEntity mapEntity = new MapEntity();
		mapEntity.set(OntologyMetaData.ONTOLOGY_IRI, codeSystem);
		mapEntity.set(OntologyMetaData.ONTOLOGY_NAME, codeSystem);
		mapEntity.set(OntologyMetaData.ID, generatedId);
		return mapEntity;
	}

	private static Entity createOntologyNodePathEntity(String nodePath, boolean isTop, String generatedId)
	{
		MapEntity mapEntity = new MapEntity();
		mapEntity.set(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH, nodePath);
		mapEntity.set(OntologyTermNodePathMetaData.ROOT, isTop);
		mapEntity.set(OntologyTermNodePathMetaData.ID, generatedId);
		return mapEntity;
	}

	private static Entity createOntologyTermSynonymEntity(String synonym, String generatedId)
	{
		MapEntity mapEntity = new MapEntity();
		mapEntity.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM, synonym);
		mapEntity.set(OntologyTermSynonymMetaData.ID, generatedId);
		return mapEntity;
	}

	private static String createLifelinesCodeEmxFileName(File lifeLinesCodeFile)
	{
		String lifelinesCodeFileName = lifeLinesCodeFile.getName();
		StringBuilder lifeLinesCodeEmxFileName = new StringBuilder();
		if (lifelinesCodeFileName.toLowerCase().endsWith(ExcelWriter.FileFormat.XLS.toString().toLowerCase()))
		{
			lifeLinesCodeEmxFileName.append(lifelinesCodeFileName.toLowerCase().replaceAll(
					ExcelWriter.FileFormat.XLS.toString().toLowerCase(), StringUtils.EMPTY));
		}
		else if (lifelinesCodeFileName.toLowerCase().endsWith(ExcelWriter.FileFormat.XLSX.toString().toLowerCase()))
		{
			lifeLinesCodeEmxFileName.append(lifelinesCodeFileName.toLowerCase().replaceAll(
					ExcelWriter.FileFormat.XLSX.toString().toLowerCase(), StringUtils.EMPTY));
		}
		lifeLinesCodeEmxFileName.append("EMX.").append(FileFormat.XLS.toString().toLowerCase());
		return lifeLinesCodeEmxFileName.toString();
	}
}