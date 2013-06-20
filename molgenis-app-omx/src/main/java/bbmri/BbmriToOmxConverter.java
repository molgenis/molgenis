package bbmri;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.io.TableReader;
import org.molgenis.io.TableWriter;
import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.model.elements.Dataset;
import org.molgenis.omx.auth.Institute;
import org.molgenis.omx.auth.Person;
import org.molgenis.omx.auth.PersonRole;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class BbmriToOmxConverter
{

	/**
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @param args
	 * @throws
	 */
	public static void main(String[] args) throws IOException
	{
		if (args.length != 2)
		{
			System.err.println("usage: java " + BbmriToOmxConverter.class.getSimpleName() + " inputfolder outputfile");
			return;
		}

		File file = new File(args[1]);
		if (file.exists()) file.delete();

		new BbmriToOmxConverter(args[0], args[1]).convert();
	}

	/**
	 * TODO CURRENT_N should be int? TODO GWA_DATA_N should be int? TODO discuss what to do with canRead, canWrite,
	 * owns, Acronym, Approved
	 */
	private enum FeatureDescription
	{
		COHORT("Cohort", FieldTypeEnum.STRING), CATEGORY("Category", FieldTypeEnum.XREF), SUBCATEGORY("Subcategory",
				FieldTypeEnum.XREF), TOPIC("Topic", FieldTypeEnum.MREF), COORDINATOR("Coordinator", FieldTypeEnum.MREF), INSTITUTION(
				"Institution", FieldTypeEnum.MREF), CURRENT_N("Current n=", FieldTypeEnum.STRING), BIODATA("Biodata",
				FieldTypeEnum.MREF), GWA_DATA_N("GWA data n=", FieldTypeEnum.STRING), GWA_PLATFORM("GWA platform",
				FieldTypeEnum.TEXT), GWA_COMMENTS("GWA comments", FieldTypeEnum.TEXT), GENERAL_COMMENTS(
				"General comments", FieldTypeEnum.TEXT), PUBLICATIONS("Publications", FieldTypeEnum.TEXT);

		private final String name;
		private final FieldTypeEnum type;
		private final String identifier;

		private FeatureDescription(String name, FieldTypeEnum type)
		{
			this.name = name;
			this.type = type;
			this.identifier = UUID.randomUUID().toString();
		}

		public String getName()
		{
			return name;
		}

		public FieldTypeEnum getType()
		{
			return type;
		}

		public String getIdentifier()
		{
			return identifier;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	private final File inputFolder;
	private final File outputFile;

	public BbmriToOmxConverter(String inputFolder, String outputFile)
	{
		if (inputFolder == null) throw new IllegalArgumentException("inputFolder is null");
		if (outputFile == null) throw new IllegalArgumentException("outputFile is null");
		this.inputFolder = new File(inputFolder);
		this.outputFile = new File(outputFile);
		if (!this.inputFolder.isDirectory())
		{
			throw new IllegalArgumentException("inputfolder is not a directory [" + inputFolder + "]");
		}
	}

	public void convert() throws IOException
	{
		Map<String, File> entityFileMap = getEntityFileMap(inputFolder);

		TableWriter outputWriter = new ExcelWriter(outputFile);
		try
		{
			String dataSetIdentifier = "biobank";
			String protocolIdentifier = UUID.randomUUID().toString();

			writeFeatures(outputWriter);
			writeProtocol(outputWriter, protocolIdentifier);
			writeDataSet(outputWriter, dataSetIdentifier, protocolIdentifier);
			Map<String, String> personRoleMap = writePersonRoles(outputWriter, entityFileMap);
			Map<String, String> ontologyMap = writeOntologyTerms(outputWriter, entityFileMap);
			Map<String, String> instituteMap = writeInstitutes(outputWriter, entityFileMap);
			Map<String, String> personMap = writePersons(outputWriter, entityFileMap, personRoleMap);
			writeDataSetMatrix(outputWriter, dataSetIdentifier, entityFileMap, ontologyMap, personMap, instituteMap);
		}
		finally
		{
			IOUtils.closeQuietly(outputWriter);
		}
	}

	private Map<String, String> writeInstitutes(TableWriter outputWriter, Map<String, File> entityFileMap)
			throws IOException
	{
		Map<String, String> instituteMap = new HashMap<String, String>();

		TupleWriter tupleWriter = outputWriter.createTupleWriter(Institute.class.getSimpleName());
		TableReader tableReader = new ExcelReader(entityFileMap.get("institutes.xls"));
		try
		{
			tupleWriter.writeColNames(Arrays.asList(Institute.IDENTIFIER, Institute.NAME, Institute.ADDRESS,
					Institute.PHONE, Institute.EMAIL, Institute.FAX, Institute.TOLLFREEPHONE, Institute.CITY,
					Institute.COUNTRY));
			TupleReader tupleSheetReader = tableReader.getTupleReader("BiobankInstitute");
			try
			{
				for (Tuple inputTuple : tupleSheetReader)
				{
					String name = inputTuple.getString("name");
					String identifier = UUID.randomUUID().toString();
					instituteMap.put(name, identifier);

					WritableTuple outputTuple = new KeyValueTuple();
					outputTuple.set(Institute.IDENTIFIER, identifier);
					outputTuple.set(Institute.NAME, name);
					outputTuple.set(Institute.ADDRESS, inputTuple.getString("Address"));
					outputTuple.set(Institute.PHONE, inputTuple.getString("Phone"));
					outputTuple.set(Institute.EMAIL, inputTuple.getString("Email"));
					outputTuple.set(Institute.FAX, inputTuple.getString("Fax"));
					outputTuple.set(Institute.TOLLFREEPHONE, inputTuple.getString("tollFreePhone"));
					outputTuple.set(Institute.CITY, inputTuple.getString("City"));
					outputTuple.set(Institute.COUNTRY, inputTuple.getString("Country"));
					tupleWriter.write(outputTuple);
				}
			}
			finally
			{
				tupleSheetReader.close();
			}
		}
		finally
		{
			IOUtils.closeQuietly(tableReader);
			IOUtils.closeQuietly(tupleWriter);
		}

		return instituteMap;
	}

	private Map<String, String> writePersons(TableWriter outputWriter, Map<String, File> entityFileMap,
			Map<String, String> ontologyMap) throws IOException
	{
		Map<String, String> personMap = new HashMap<String, String>();

		TupleWriter tupleWriter = outputWriter.createTupleWriter(Person.class.getSimpleName());
		TableReader tableReader = new ExcelReader(entityFileMap.get("biobankcoordinator.xls"));
		try
		{
			tupleWriter.writeColNames(Arrays.asList(Person.IDENTIFIER, Person.NAME, Person.ADDRESS, Person.PHONE,
					Person.EMAIL, Person.FAX, Person.TOLLFREEPHONE, Person.CITY, Person.COUNTRY, Person.FIRSTNAME,
					Person.MIDINITIALS, Person.LASTNAME, Person.TITLE, Person.AFFILIATION_NAME, Person.DEPARTMENT,
					Person.ROLES_IDENTIFIER));

			TupleReader tupleSheetReader = tableReader.getTupleReader("BiobankCoordinator");
			try
			{
				for (Tuple inputTuple : tupleSheetReader)
				{
					String name = inputTuple.getString("name");
					String identifier = UUID.randomUUID().toString();
					personMap.put(name, identifier);

					WritableTuple outputTuple = new KeyValueTuple();
					outputTuple.set(Person.IDENTIFIER, identifier);
					outputTuple.set(Person.NAME, name);
					outputTuple.set(Person.ADDRESS, inputTuple.getString("Address"));
					outputTuple.set(Person.PHONE, inputTuple.getString("Phone"));
					outputTuple.set(Person.EMAIL, inputTuple.getString("Email"));
					outputTuple.set(Person.FAX, inputTuple.getString("Fax"));
					outputTuple.set(Person.TOLLFREEPHONE, inputTuple.getString("tollFreePhone"));
					outputTuple.set(Person.CITY, inputTuple.getString("City"));
					outputTuple.set(Person.COUNTRY, inputTuple.getString("Country"));
					outputTuple.set(Person.FIRSTNAME, inputTuple.getString("FirstName"));
					outputTuple.set(Person.MIDINITIALS, inputTuple.getString("MidInitials"));
					outputTuple.set(Person.LASTNAME, inputTuple.getString("LastName"));
					outputTuple.set(Person.TITLE, inputTuple.getString("Title"));
					outputTuple.set(Person.AFFILIATION_NAME, inputTuple.getString("Affiliation_name"));
					outputTuple.set(Person.DEPARTMENT, inputTuple.getString("Department"));
					outputTuple.set(Person.ROLES_IDENTIFIER, ontologyMap.get(inputTuple.getString("Roles_name")));
					tupleWriter.write(outputTuple);
				}
			}
			finally
			{
				tupleSheetReader.close();
			}
		}
		finally
		{
			IOUtils.closeQuietly(tableReader);
			IOUtils.closeQuietly(tupleWriter);
		}

		return personMap;
	}

	private Map<String, String> writePersonRoles(TableWriter outputWriter, Map<String, File> entityFileMap)
			throws IOException
	{
		Map<String, String> personRoleMap = new HashMap<String, String>();

		TupleWriter tupleWriter = outputWriter.createTupleWriter(PersonRole.class.getSimpleName());
		try
		{
			tupleWriter.writeColNames(Arrays.asList(PersonRole.IDENTIFIER, PersonRole.NAME));

			TableReader tableReader = new ExcelReader(entityFileMap.get("personrole.xls"));
			try
			{
				TupleReader tupleSheetReader = tableReader.getTupleReader("BiobankPersonRole");
				try
				{
					for (Tuple inputTuple : tupleSheetReader)
					{
						String name = inputTuple.getString("name");
						String identifier = UUID.randomUUID().toString();
						personRoleMap.put(name, identifier);

						WritableTuple outputTuple = new KeyValueTuple();
						outputTuple.set(OntologyTerm.IDENTIFIER, identifier);
						outputTuple.set(OntologyTerm.NAME, name);
						tupleWriter.write(outputTuple);
					}
				}
				finally
				{
					tupleSheetReader.close();
				}
			}
			finally
			{
				IOUtils.closeQuietly(tableReader);
			}
		}
		finally
		{
			IOUtils.closeQuietly(tupleWriter);
		}
		return personRoleMap;
	}

	private Map<String, String> writeOntologyTerms(TableWriter outputWriter, Map<String, File> entityFileMap)
			throws IOException
	{
		Map<String, String> ontologyMap = new HashMap<String, String>();

		TupleWriter tupleWriter = outputWriter.createTupleWriter(OntologyTerm.class.getSimpleName());
		try
		{
			tupleWriter.writeColNames(Arrays.asList(OntologyTerm.IDENTIFIER, OntologyTerm.NAME));

			// biodata
			{
				TableReader tableReader = new ExcelReader(entityFileMap.get("biobankdatatype.xls"));
				try
				{
					TupleReader tupleSheetReader = tableReader.getTupleReader("BiobankDataType");
					try
					{
						for (Tuple inputTuple : tupleSheetReader)
						{
							String name = inputTuple.getString("name");
							String identifier = UUID.randomUUID().toString();
							ontologyMap.put(name, identifier);

							WritableTuple outputTuple = new KeyValueTuple();
							outputTuple.set(OntologyTerm.IDENTIFIER, identifier);
							outputTuple.set(OntologyTerm.NAME, name);
							tupleWriter.write(outputTuple);
						}
					}
					finally
					{
						tupleSheetReader.close();
					}
				}
				finally
				{
					IOUtils.closeQuietly(tableReader);
				}
			}

			// category
			{
				TableReader tableReader = new ExcelReader(entityFileMap.get("categories.xls"));
				try
				{
					TupleReader tupleSheetReader = tableReader.getTupleReader("BiobankCategory");
					try
					{
						for (Tuple inputTuple : tupleSheetReader)
						{
							String name = inputTuple.getString("name");
							String identifier = UUID.randomUUID().toString();
							ontologyMap.put(name, identifier);

							WritableTuple outputTuple = new KeyValueTuple();
							outputTuple.set(OntologyTerm.IDENTIFIER, identifier);
							outputTuple.set(OntologyTerm.NAME, name);
							tupleWriter.write(outputTuple);
						}
					}
					finally
					{
						tupleSheetReader.close();
					}
				}
				finally
				{
					IOUtils.closeQuietly(tableReader);
				}
			}

			// subcategory
			{
				TableReader tableReader = new ExcelReader(entityFileMap.get("subcategories.xls"));
				try
				{
					TupleReader tupleSheetReader = tableReader.getTupleReader("BiobankSubCategory");
					try
					{
						for (Tuple inputTuple : tupleSheetReader)
						{
							String name = inputTuple.getString("name");
							String identifier = UUID.randomUUID().toString();
							ontologyMap.put(name, identifier);

							WritableTuple outputTuple = new KeyValueTuple();
							outputTuple.set(OntologyTerm.IDENTIFIER, identifier);
							outputTuple.set(OntologyTerm.NAME, name);
							tupleWriter.write(outputTuple);
						}
					}
					finally
					{
						tupleSheetReader.close();
					}
				}
				finally
				{
					IOUtils.closeQuietly(tableReader);
				}
			}

			// topics
			{
				TableReader tableReader = new ExcelReader(entityFileMap.get("topics.xls"));
				try
				{
					TupleReader tupleSheetReader = tableReader.getTupleReader("BiobankTopic");
					try
					{
						for (Tuple inputTuple : tupleSheetReader)
						{
							String name = inputTuple.getString("name");
							String identifier = UUID.randomUUID().toString();
							ontologyMap.put(name, identifier);

							WritableTuple outputTuple = new KeyValueTuple();
							outputTuple.set(OntologyTerm.IDENTIFIER, identifier);
							outputTuple.set(OntologyTerm.NAME, name);
							tupleWriter.write(outputTuple);
						}
					}
					finally
					{
						tupleSheetReader.close();
					}
				}
				finally
				{
					IOUtils.closeQuietly(tableReader);
				}
			}
		}
		finally
		{
			IOUtils.closeQuietly(tupleWriter);
		}
		return ontologyMap;
	}

	private void writeDataSetMatrix(TableWriter outputWriter, String dataSetIdentifier,
			Map<String, File> entityFileMap, Map<String, String> ontologyMap, final Map<String, String> personMap,
			final Map<String, String> institutionMap) throws IOException
	{

		TupleWriter tupleWriter = outputWriter.createTupleWriter(DataSet.class.getSimpleName().toLowerCase() + '_'
				+ dataSetIdentifier);
		TableReader tableReader = new ExcelReader(entityFileMap.get("cohorts.xls"));
		try
		{
			tupleWriter.writeColNames(Lists.transform(Arrays.asList(FeatureDescription.values()),
					new Function<FeatureDescription, String>()
					{
						@Override
						public String apply(FeatureDescription featureDescription)
						{
							return featureDescription.getIdentifier();
						}
					}));

			TupleReader tupleReader = tableReader.getTupleReader("Biobank");
			try
			{
				for (Tuple inputTuple : tupleReader)
				{
					WritableTuple outputTuple = new KeyValueTuple();
					for (FeatureDescription featureDescription : FeatureDescription.values())
					{
						String featureIdentifier = featureDescription.getIdentifier();
						switch (featureDescription)
						{
							case COHORT:
								outputTuple.set(featureIdentifier, inputTuple.get("Cohort"));
								break;
							case BIODATA:
								outputTuple.set(featureIdentifier, ontologyMap.get(inputTuple.get("Biodata_name")));
								break;
							case CATEGORY:
								outputTuple.set(featureIdentifier, ontologyMap.get(inputTuple.get("Category_name")));
								break;
							case COORDINATOR:
								List<String> coordinatorIdentifiers = Lists.transform(
										inputTuple.getList("Coordinator_name"), new Function<String, String>()
										{
											@Override
											public String apply(String coordinatorName)
											{
												return personMap.get(coordinatorName);
											}
										});
								outputTuple.set(featureIdentifier, coordinatorIdentifiers);
								break;
							case CURRENT_N:
								outputTuple.set(featureIdentifier, inputTuple.get("PanelSize"));
								break;
							case GENERAL_COMMENTS:
								outputTuple.set(featureIdentifier, inputTuple.get("GeneralComments"));
								break;
							case GWA_COMMENTS:
								outputTuple.set(featureIdentifier, inputTuple.get("GwaComments"));
								break;
							case GWA_DATA_N:
								outputTuple.set(featureIdentifier, inputTuple.get("GwaDataNum"));
								break;
							case GWA_PLATFORM:
								outputTuple.set(featureIdentifier, inputTuple.get("GwaPlatform"));
								break;
							case INSTITUTION:
								List<String> institutionIdentifiers = Lists.transform(
										inputTuple.getList("Institutes_name"), new Function<String, String>()
										{
											@Override
											public String apply(String coordinatorName)
											{
												return institutionMap.get(coordinatorName);
											}
										});
								outputTuple.set(featureIdentifier, institutionIdentifiers);
								break;
							case PUBLICATIONS:
								outputTuple.set(featureIdentifier, inputTuple.get("Publications"));
								break;
							case SUBCATEGORY:
								outputTuple.set(featureIdentifier, ontologyMap.get(inputTuple.get("SubCategory_name")));
								break;
							case TOPIC:
								outputTuple.set(featureIdentifier, ontologyMap.get(inputTuple.get("Topic_name")));
								break;
							default:
								break;
						}
					}
					tupleWriter.write(outputTuple);
				}
			}
			finally
			{
				tupleReader.close();
			}
		}
		finally
		{
			IOUtils.closeQuietly(tableReader);
			IOUtils.closeQuietly(tupleWriter);
		}
	}

	private void writeFeatures(TableWriter tableWriter) throws IOException
	{
		TupleWriter tupleWriter = tableWriter.createTupleWriter(ObservableFeature.class.getSimpleName());
		try
		{
			tupleWriter.writeColNames(Arrays.asList(ObservableFeature.IDENTIFIER, ObservableFeature.NAME,
					ObservableFeature.DATATYPE));

			for (FeatureDescription featureDescription : FeatureDescription.values())
			{
				WritableTuple tuple = new KeyValueTuple();
				tuple.set(ObservableFeature.IDENTIFIER, featureDescription.getIdentifier());
				tuple.set(ObservableFeature.NAME, featureDescription.getName());
				tuple.set(ObservableFeature.DATATYPE, featureDescription.getType().toString().toLowerCase());
				tupleWriter.write(tuple);
			}
		}
		finally
		{
			tupleWriter.close();
		}
	}

	private void writeProtocol(TableWriter tableWriter, String identifier) throws IOException
	{
		TupleWriter tupleWriter = tableWriter.createTupleWriter(Protocol.class.getSimpleName());
		try
		{
			String featureIdentifiersStr = Joiner.on(',').join(
					Lists.transform(Arrays.asList(FeatureDescription.values()),
							new Function<FeatureDescription, String>()
							{
								@Override
								public String apply(FeatureDescription featureDescription)
								{
									return featureDescription.getIdentifier();
								}
							}));

			tupleWriter.writeColNames(Arrays.asList(Protocol.IDENTIFIER, Protocol.NAME, Protocol.FEATURES_IDENTIFIER));
			WritableTuple tuple = new KeyValueTuple();
			tuple.set(Protocol.IDENTIFIER, identifier);
			tuple.set(Protocol.NAME, "Biobanks");
			tuple.set(Protocol.FEATURES_IDENTIFIER, featureIdentifiersStr);
			tupleWriter.write(tuple);
		}
		finally
		{
			tupleWriter.close();
		}
	}

	private void writeDataSet(TableWriter tableWriter, String identifier, String protocolIdentifier) throws IOException
	{
		TupleWriter tupleWriter = tableWriter.createTupleWriter(Dataset.class.getSimpleName());
		try
		{
			tupleWriter.writeColNames(Arrays.asList(DataSet.IDENTIFIER, DataSet.NAME, DataSet.PROTOCOLUSED_IDENTIFIER));
			WritableTuple tuple = new KeyValueTuple();
			tuple.set(DataSet.IDENTIFIER, identifier);
			tuple.set(DataSet.NAME, "Biobanks data set");
			tuple.set(DataSet.PROTOCOLUSED_IDENTIFIER, protocolIdentifier);
			tupleWriter.write(tuple);
		}
		finally
		{
			tupleWriter.close();
		}
	}

	private Map<String, File> getEntityFileMap(File folder)
	{
		Map<String, File> fileMap = new HashMap<String, File>();
		File[] files = folder.listFiles();
		for (File file : files)
			fileMap.put(file.getName(), file);
		return fileMap;
	}

}
