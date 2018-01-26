package org.molgenis.core.ui.style;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.settings.AppSettings;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class StyleSheetRepositoryDecoratorTest
{
	private StyleSheetRepositoryDecorator decorator;
	private AppSettings settings;
	private Repository delegate;

	@BeforeClass
	public void setUp()
	{

		delegate = mock(Repository.class);
		settings = mock(AppSettings.class);
		when(settings.getBootstrapTheme()).thenReturn("2");

		decorator = new StyleSheetRepositoryDecorator(delegate, settings);
	}

	@Test
	public void testDeleteById() throws Exception
	{
		decorator.deleteById("1");
		verify(delegate).deleteById("1");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testDeleteByIdCurrent() throws Exception
	{
		decorator.deleteById("2");
		verifyZeroInteractions(delegate);
	}

	@Test
	public void testDelete() throws Exception
	{
		StyleSheet sheet = mock(StyleSheet.class);
		when(sheet.getId()).thenReturn("1");
		decorator.delete(sheet);
		verify(delegate).delete(sheet);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testDeleteCurrent() throws Exception
	{
		StyleSheet sheet = mock(StyleSheet.class);
		when(sheet.getId()).thenReturn("2");
		decorator.delete(sheet);
		verifyZeroInteractions(delegate);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testDeleteAll() throws Exception
	{
		decorator.deleteAll();
		verifyZeroInteractions(delegate);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testDeleteAllStream() throws Exception
	{
		decorator.deleteAll(Arrays.<Object>asList("1", "2", "3", "4").stream());
	}

}