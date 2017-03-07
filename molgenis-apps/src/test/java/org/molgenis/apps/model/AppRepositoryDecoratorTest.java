package org.molgenis.apps.model;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMeta;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.util.ResourceUtils.getFile;
import static org.testng.Assert.assertEquals;

public class AppRepositoryDecoratorTest
{
	private static final String APP_NAME = "appName";

	@Mock
	private Repository<App> appRepository;

	@Mock
	private FileStore fileStore;

	private AppRepositoryDecorator appRepositoryDecorator;

	@BeforeClass
	public void setUpBeforeClass()
	{
		initMocks(this);
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		reset(appRepository, fileStore);
		appRepositoryDecorator = new AppRepositoryDecorator(appRepository, fileStore);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testAppRepositoryDecorator()
	{
		new AppRepositoryDecorator(null, null);
	}

	@Test
	public void testDelegate()
	{
		assertEquals(appRepositoryDecorator.delegate(), appRepository);
	}

	@Test
	public void testAddInactiveNoResourceZip()
	{
		App app = getMockApp(false);
		appRepositoryDecorator.add(app);
		verify(appRepository).add(app);
	}

	@Test
	public void testAddActiveNoResourceZip()
	{
		App app = getMockApp(true);
		appRepositoryDecorator.add(app);
		verify(appRepository).add(app);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "'app-invalid.zip' is not a valid zip file.")
	public void testAddInvalidResourceZip()
	{
		App app = getMockApp(false, "app-invalid.zip");
		appRepositoryDecorator.add(app);
		verify(appRepository).add(app);
	}

	@Test
	public void testAddInactiveValidResourceZip()
	{
		App app = getMockApp(false, "app-valid.zip");
		appRepositoryDecorator.add(app);
		verify(appRepository).add(app);
		//noinspection ResultOfMethodCallIgnored
		verify(fileStore, never()).getStorageDir();
	}

	@Test
	public void testAddActiveValidResourceZip()
	{
		App app = getMockApp(true, "app-valid.zip");
		appRepositoryDecorator.add(app);
		verify(appRepository).add(app);
		//noinspection ResultOfMethodCallIgnored
		verify(fileStore).getStorageDir();
	}

	@Test
	public void testAddStream()
	{
		App app0 = getMockApp(true, "app-valid.zip");
		App app1 = getMockApp(false, "app-valid.zip");
		App app2 = getMockApp(true, "app-valid.zip");
		App app3 = getMockApp(true);
		App app4 = getMockApp(false);
		appRepositoryDecorator.add(Stream.of(app0, app1, app2, app3, app4));

		ArgumentCaptor<Stream<App>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(appRepository).add(captor.capture());
		assertEquals(captor.getValue().collect(toList()), asList(app0, app1, app2, app3, app4));

		//noinspection ResultOfMethodCallIgnored
		verify(fileStore, times(2)).getStorageDir();
	}

	@Test
	public void testUpdateActivatedResourceToActivatedValidResource() throws IOException
	{
		App existingApp = getMockApp(true, "app-valid.zip");
		App app = getMockApp(true, "app-valid-update.zip");
		when(appRepository.findOneById(APP_NAME)).thenReturn(existingApp);
		appRepositoryDecorator.update(app);
		verify(appRepository).update(app);
		verify(fileStore).deleteDirectory(anyString());
		//noinspection ResultOfMethodCallIgnored
		verify(fileStore).getStorageDir();
	}

	@Test
	public void testUpdateDeactivatedResourceToDeactivatedValidResource()
	{
		App existingApp = getMockApp(false, "app-valid.zip");
		App app = getMockApp(false, "app-valid.zip");
		when(appRepository.findOneById(APP_NAME)).thenReturn(existingApp);
		appRepositoryDecorator.update(app);
		verify(appRepository).update(app);
		//noinspection ResultOfMethodCallIgnored
		verify(fileStore, never()).getStorageDir();
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "'app-invalid.zip' is not a valid zip file.")
	public void testUpdateDeactivatedResourceToDeactivatedInvalidResource()
	{
		App existingApp = getMockApp(false, "app-valid.zip");
		App app = getMockApp(false, "app-invalid.zip");
		when(appRepository.findOneById(APP_NAME)).thenReturn(existingApp);
		appRepositoryDecorator.update(app);
	}

	@Test(expectedExceptions = MolgenisValidationException.class, expectedExceptionsMessageRegExp = "'app-invalid.zip' is not a valid zip file.")
	public void testUpdateNoResourceToDeactivatedInvalidResource()
	{
		App existingApp = getMockApp(false);
		App app = getMockApp(false, "app-invalid.zip");
		when(appRepository.findOneById(APP_NAME)).thenReturn(existingApp);
		appRepositoryDecorator.update(app);
	}

	@Test
	public void testUpdateStream() throws IOException
	{
		App existingApp = getMockApp(true, "app-valid.zip");
		App app = getMockApp(true, "app-valid-update.zip");
		when(appRepository.findOneById(APP_NAME)).thenReturn(existingApp);
		appRepositoryDecorator.update(Stream.of(app));
		ArgumentCaptor<Stream<App>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(appRepository).update(captor.capture());
		assertEquals(captor.getValue().collect(toList()), singletonList(app));
		verify(fileStore).deleteDirectory(anyString());
		//noinspection ResultOfMethodCallIgnored
		verify(fileStore).getStorageDir();
	}

	@Test
	public void testDeleteActive() throws IOException
	{
		App app = getMockApp(true, "app-valid.zip");
		appRepositoryDecorator.delete(app);
		verify(fileStore).deleteDirectory(anyString());
	}

	@Test
	public void testDeleteInactive() throws IOException
	{
		App app = getMockApp(false, "app-valid.zip");
		appRepositoryDecorator.delete(app);
		verify(fileStore, never()).deleteDirectory(anyString());
	}

	@Test
	public void testDeleteStream() throws IOException
	{
		App app0 = getMockApp(true, "app-valid.zip");
		App app1 = getMockApp(false, "app-valid.zip");
		App app2 = getMockApp(true, "app-valid-update.zip");
		appRepositoryDecorator.delete(Stream.of(app0, app1, app2));

		ArgumentCaptor<Stream<App>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(appRepository).delete(captor.capture());
		assertEquals(captor.getValue().collect(toList()), asList(app0, app1, app2));

		verify(fileStore, times(2)).deleteDirectory(anyString());
	}

	@Test
	public void testDeleteById() throws IOException
	{
		App app = getMockApp(true, "app-valid.zip");
		when(appRepository.findOneById(APP_NAME)).thenReturn(app);
		appRepositoryDecorator.deleteById(APP_NAME);
		verify(fileStore).deleteDirectory(anyString());
	}

	private App getMockApp(boolean isActive)
	{
		return getMockApp(isActive, null);
	}

	private App getMockApp(boolean isActive, String resourceFilename)
	{
		App app = mock(App.class);
		when(app.getName()).thenReturn(APP_NAME);
		when(app.isActive()).thenReturn(isActive);
		if (resourceFilename != null)
		{
			FileMeta fileMeta = mock(FileMeta.class);
			when(fileMeta.getId()).thenReturn(resourceFilename);
			when(fileMeta.getFilename()).thenReturn(resourceFilename);
			when(app.getSourceFiles()).thenReturn(fileMeta);
			when(fileStore.getFile(resourceFilename)).thenReturn(getFile(getClass(), '/' + resourceFilename));
		}
		return app;
	}
}