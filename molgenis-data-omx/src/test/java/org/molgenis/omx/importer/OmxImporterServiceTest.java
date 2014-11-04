package org.molgenis.omx.importer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.jpa.importer.EntityImportService;
import org.molgenis.data.omx.importer.OmxImporterServiceImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.QueryResolver;
import org.molgenis.data.validation.DefaultEntityValidator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.CategoryRepository;
import org.molgenis.omx.observ.CharacteristicRepository;
import org.molgenis.omx.observ.DataSetRepository;
import org.molgenis.omx.observ.ObservableFeatureRepository;
import org.molgenis.omx.observ.ObservationSetRepository;
import org.molgenis.omx.observ.ObservationTargetRepository;
import org.molgenis.omx.observ.ObservedValueRepository;
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
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OmxImporterServiceTest
{
	private static Authentication AUTHENTICATION_PREVIOUS;

	@BeforeClass
	public void setUpBeforeClass()
	{
		AUTHENTICATION_PREVIOUS = SecurityContextHolder.getContext().getAuthentication();
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_PREVIOUS);
	}

	protected DataService dataService;
	private EntityManager entityManager;
	private OmxImporterServiceImpl importer;
	private EmbeddedElasticSearchServiceFactory factory;
	private SearchService searchService;
	private FileRepositoryCollectionFactory fileRepositorySourceFactory;

	@BeforeMethod
	public void beforeMethod()
	{
		entityManager = Persistence.createEntityManagerFactory("molgenis-import-test").createEntityManager();
		dataService = new DataServiceImpl();
		fileRepositorySourceFactory = new FileRepositoryCollectionFactory();

		fileRepositorySourceFactory.addFileRepositoryCollectionClass(ExcelRepositoryCollection.class,
				ExcelRepositoryCollection.EXTENSIONS);
		QueryResolver queryResolver = new QueryResolver(dataService);

		dataService.addRepository(new CharacteristicRepository(entityManager, queryResolver));
		dataService.addRepository(new ProtocolRepository(entityManager, queryResolver));
		dataService.addRepository(new DataSetRepository(entityManager, queryResolver));
		dataService.addRepository(new ObservationSetRepository(entityManager, queryResolver));
		dataService.addRepository(new ObservedValueRepository(entityManager, queryResolver));
		dataService.addRepository(new CategoryRepository(entityManager, queryResolver));
		dataService.addRepository(new ObservationTargetRepository(entityManager, queryResolver));
		dataService.addRepository(new IndividualRepository(entityManager, queryResolver));
		dataService.addRepository(new PanelRepository(entityManager, queryResolver));
		dataService.addRepository(new ValueRepository(entityManager, queryResolver));
		dataService.addRepository(new StringValueRepository(entityManager, queryResolver));
		dataService.addRepository(new XrefValueRepository(entityManager, queryResolver));
		dataService.addRepository(new MrefValueRepository(entityManager, queryResolver));
		dataService.addRepository(new EmailValueRepository(entityManager, queryResolver));
		dataService.addRepository(new DecimalValueRepository(entityManager, queryResolver));
		dataService.addRepository(new IntValueRepository(entityManager, queryResolver));
		dataService.addRepository(new CategoricalValueRepository(entityManager, queryResolver));
		dataService.addRepository(new DateValueRepository(entityManager, queryResolver));
		dataService.addRepository(new DateTimeValueRepository(entityManager, queryResolver));
		dataService.addRepository(new BoolValueRepository(entityManager, queryResolver));
		dataService.addRepository(new HtmlValueRepository(entityManager, queryResolver));
		dataService.addRepository(new HyperlinkValueRepository(entityManager, queryResolver));
		dataService.addRepository(new LongValueRepository(entityManager, queryResolver));
		dataService.addRepository(new TextValueRepository(entityManager, queryResolver));
		dataService.addRepository(new ObservableFeatureRepository(entityManager, queryResolver));

		factory = new EmbeddedElasticSearchServiceFactory(Collections.singletonMap("path.data", "target/data"));
		EntityToSourceConverter entityToSourceConverter = new EntityToSourceConverter();
		searchService = factory.create(dataService, entityToSourceConverter);
		EntityValidator validator = new DefaultEntityValidator(dataService, new EntityAttributesValidator());

		EntityImportService eis = new EntityImportService();
		eis.setDataService(dataService);
		importer = new OmxImporterServiceImpl(dataService, searchService, new EntitiesImporterImpl(
				fileRepositorySourceFactory, eis), validator, new QueryResolver(dataService), null);

		entityManager.getTransaction().begin();

		// set super user credentials
		Collection<? extends GrantedAuthority> authorities = Arrays
				.<SimpleGrantedAuthority> asList(new SimpleGrantedAuthority(SecurityUtils.AUTHORITY_SU));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(null, null, authorities));
	}

	@Test
	public void testImportMissingXref() throws IOException, ValueConverterException
	{
		try
		{
			RepositoryCollection source = fileRepositorySourceFactory
					.createFileRepositoryCollection(loadTestFile("missing-xref.xlsx"));
			importer.doImport(source, DatabaseAction.ADD_UPDATE_EXISTING);
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
			RepositoryCollection source = fileRepositorySourceFactory
					.createFileRepositoryCollection(loadTestFile("missing-mref.xlsx"));
			importer.doImport(source, DatabaseAction.ADD_UPDATE_EXISTING);
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
			RepositoryCollection source = fileRepositorySourceFactory
					.createFileRepositoryCollection(loadTestFile("wrong-email-value.xlsx"));
			importer.doImport(source, DatabaseAction.ADD_UPDATE_EXISTING);
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

	@Test
	public void testFeatureInMultipleProtocols() throws IOException, ValueConverterException
	{
		try
		{
			RepositoryCollection source = fileRepositorySourceFactory
					.createFileRepositoryCollection(loadTestFile("feature-in-multiple-protocols.xls"));
			importer.doImport(source, DatabaseAction.ADD_UPDATE_EXISTING);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			assertEquals(e.getViolations().size(), 1);
			assertEquals(e.getViolations().iterator().next().getInvalidValue(), "Celiac_Individual");
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
