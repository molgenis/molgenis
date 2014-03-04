package org.molgenis.omx.importer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositorySource;
import org.molgenis.data.excel.ExcelRepositorySource;
import org.molgenis.data.importer.EntityImportService;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.DefaultEntityValidator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.dataset.DataSetMatrixRepository;
import org.molgenis.omx.observ.CategoryRepository;
import org.molgenis.omx.observ.CharacteristicRepository;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.DataSetRepository;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.ObservableFeatureRepository;
import org.molgenis.omx.observ.ObservationSetRepository;
import org.molgenis.omx.observ.ObservationTargetRepository;
import org.molgenis.omx.observ.ObservedValueRepository;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.ProtocolRepository;
import org.molgenis.omx.observ.target.IndividualRepository;
import org.molgenis.omx.observ.target.PanelRepository;
import org.molgenis.omx.observ.value.BoolValueRepository;
import org.molgenis.omx.observ.value.CategoricalValueRepository;
import org.molgenis.omx.observ.value.DateTimeValueRepository;
import org.molgenis.omx.observ.value.DateValueRepository;
import org.molgenis.omx.observ.value.DecimalValueRepository;
import org.molgenis.omx.observ.value.EmailValueRepository;
import org.molgenis.omx.observ.value.HtmlValueRepository;
import org.molgenis.omx.observ.value.HyperlinkValueRepository;
import org.molgenis.omx.observ.value.IntValueRepository;
import org.molgenis.omx.observ.value.LongValueRepository;
import org.molgenis.omx.observ.value.MrefValueRepository;
import org.molgenis.omx.observ.value.StringValueRepository;
import org.molgenis.omx.observ.value.TextValueRepository;
import org.molgenis.omx.observ.value.ValueRepository;
import org.molgenis.omx.observ.value.XrefValueRepository;
import org.molgenis.search.SearchService;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OmxImporterServiceTest
{
	protected DataService dataService;
	private EntityManager entityManager;
	private OmxImporterService importer;
	private EmbeddedElasticSearchServiceFactory factory;
	private SearchService searchService;

	@BeforeMethod
	public void beforeMethod()
	{
		entityManager = Persistence.createEntityManagerFactory("molgenis-import-test").createEntityManager();
		dataService = new DataServiceImpl();
		dataService.addFileRepositorySourceClass(ExcelRepositorySource.class, ExcelRepositorySource.EXTENSIONS);
		EntityValidator validator = new DefaultEntityValidator(dataService, new EntityAttributesValidator());

		dataService.addRepository(new CharacteristicRepository(entityManager, validator));
		dataService.addRepository(new ObservableFeatureRepository(entityManager, validator));
		dataService.addRepository(new ProtocolRepository(entityManager, validator));
		dataService.addRepository(new DataSetRepository(entityManager, validator));
		dataService.addRepository(new ObservationSetRepository(entityManager, validator));
		dataService.addRepository(new ObservedValueRepository(entityManager, validator));
		dataService.addRepository(new CategoryRepository(entityManager, validator));
		dataService.addRepository(new ObservationTargetRepository(entityManager, validator));
		dataService.addRepository(new IndividualRepository(entityManager, validator));
		dataService.addRepository(new PanelRepository(entityManager, validator));
		dataService.addRepository(new ValueRepository(entityManager, validator));
		dataService.addRepository(new StringValueRepository(entityManager, validator));
		dataService.addRepository(new XrefValueRepository(entityManager, validator));
		dataService.addRepository(new MrefValueRepository(entityManager, validator));
		dataService.addRepository(new EmailValueRepository(entityManager, validator));
		dataService.addRepository(new DecimalValueRepository(entityManager, validator));
		dataService.addRepository(new IntValueRepository(entityManager, validator));
		dataService.addRepository(new CategoricalValueRepository(entityManager, validator));
		dataService.addRepository(new DateValueRepository(entityManager, validator));
		dataService.addRepository(new DateTimeValueRepository(entityManager, validator));
		dataService.addRepository(new BoolValueRepository(entityManager, validator));
		dataService.addRepository(new HtmlValueRepository(entityManager, validator));
		dataService.addRepository(new HyperlinkValueRepository(entityManager, validator));
		dataService.addRepository(new LongValueRepository(entityManager, validator));
		dataService.addRepository(new TextValueRepository(entityManager, validator));

		factory = new EmbeddedElasticSearchServiceFactory(Collections.singletonMap("path.data", "target/data"));
		searchService = factory.create();

		EntityImportService eis = new EntityImportService();
		eis.setDataService(dataService);
		importer = new OmxImporterServiceImpl(dataService, searchService, new EntitiesImporterImpl(dataService, eis),
				validator);

		entityManager.getTransaction().begin();
	}

	@Test
	public void testImportSampleOmx() throws IOException, ValueConverterException
	{
		RepositorySource source = dataService.createFileRepositorySource(loadTestFile("example-omx.xls"));
		importer.doImport(source.getRepositories(), DatabaseAction.ADD_UPDATE_EXISTING);

		assertEquals(dataService.count(DataSet.ENTITY_NAME, new QueryImpl()), 1);
		DataSet dataSet = dataService.findOne(DataSet.ENTITY_NAME, new QueryImpl(), DataSet.class);
		searchService.indexRepository(new DataSetMatrixRepository(dataService, dataSet.getIdentifier()));
		searchService.refresh();

		assertEquals(dataService.count(ObservableFeature.ENTITY_NAME, new QueryImpl()), 18);
		assertEquals(dataService.count(Protocol.ENTITY_NAME, new QueryImpl()), 6);
		assertEquals(dataService.count("celiacsprue", new QueryImpl()), 4);

		Entity patient44 = dataService.findOne("celiacsprue", new QueryImpl().eq("Celiac_Individual", "id_103"));
		assertNotNull(patient44);
	}

	@Test
	public void testImportMissingXref() throws IOException, ValueConverterException
	{
		try
		{
			RepositorySource source = dataService.createFileRepositorySource(loadTestFile("missing-xref.xlsx"));
			importer.doImport(source.getRepositories(), DatabaseAction.ADD_UPDATE_EXISTING);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			assertEquals(e.getViolations().size(), 1);
			assertEquals(e.getViolations().iterator().next().getRownr(), 2);
			assertEquals(e.getViolations().iterator().next().getInvalidValue(), "Klaas");
			assertEquals(e.getViolations().iterator().next().getViolatedAttribute().getName(), "father");
		}
	}

	@Test
	public void testImportMissingMref() throws IOException, ValueConverterException
	{
		try
		{
			RepositorySource source = dataService.createFileRepositorySource(loadTestFile("missing-mref.xlsx"));
			importer.doImport(source.getRepositories(), DatabaseAction.ADD_UPDATE_EXISTING);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			assertEquals(e.getViolations().size(), 1);
			assertEquals(e.getViolations().iterator().next().getRownr(), 3);
			assertEquals(e.getViolations().iterator().next().getInvalidValue(), "Jaap,Klaas,Marie");
			assertEquals(e.getViolations().iterator().next().getViolatedAttribute().getName(), "children");
		}
	}

	@Test
	public void testImportWrongEmailValue() throws IOException, ValueConverterException
	{
		try
		{
			RepositorySource source = dataService.createFileRepositorySource(loadTestFile("wrong-email-value.xlsx"));
			importer.doImport(source.getRepositories(), DatabaseAction.ADD_UPDATE_EXISTING);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			assertEquals(e.getViolations().size(), 2);
			assertEquals(e.getViolations().iterator().next().getRownr(), 2);
			assertEquals(e.getViolations().iterator().next().getInvalidValue(), "Klaas");
			assertEquals(e.getViolations().iterator().next().getViolatedAttribute().getName(), "email");
		}
	}

	@AfterMethod
	public void afterMethod() throws IOException
	{
		factory.close();
		entityManager.getTransaction().rollback();
		entityManager.close();
	}

	private File loadTestFile(String name) throws IOException
	{
		InputStream in = getClass().getResourceAsStream("/" + name);
		File f = File.createTempFile(name, "." + StringUtils.getFilenameExtension(name));
		FileCopyUtils.copy(in, new FileOutputStream(f));

		return f;
	}

}
