package org.molgenis.ui.controller;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.ui.settings.StaticContent;
import org.molgenis.ui.settings.StaticContentFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.ui.settings.StaticContentMeta.STATIC_CONTENT;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class StaticContentServiceImplTest extends AbstractMockitoTest
{
	@Mock
	private DataService dataService;
	@Mock
	private StaticContentFactory staticContentFactory;

	private StaticContentServiceImpl staticContentServiceImpl;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		staticContentServiceImpl = new StaticContentServiceImpl(dataService, staticContentFactory);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testStaticContentServiceImpl()
	{
		new StaticContentServiceImpl(null, null);
	}

	@Test
	public void testSubmitContentAdd()
	{
		String key = "key";
		String content = "content";
		when(dataService.findOneById(STATIC_CONTENT, key, StaticContent.class)).thenReturn(null);
		StaticContent staticContent = mock(StaticContent.class);
		when(staticContentFactory.create(key)).thenReturn(staticContent);

		assertTrue(staticContentServiceImpl.submitContent(key, content));
		ArgumentCaptor<StaticContent> staticContentArgumentCaptor = ArgumentCaptor.forClass(StaticContent.class);
		verify(dataService).add(eq(STATIC_CONTENT), staticContentArgumentCaptor.capture());
		verify(staticContentArgumentCaptor.getValue()).setContent(content);
	}

	@Test
	public void testSubmitContentUpdate()
	{
		String key = "key";
		String content = "content";
		StaticContent staticContent = mock(StaticContent.class);
		when(dataService.findOneById(STATIC_CONTENT, key, StaticContent.class)).thenReturn(staticContent);

		assertTrue(staticContentServiceImpl.submitContent(key, content));
		ArgumentCaptor<StaticContent> staticContentArgumentCaptor = ArgumentCaptor.forClass(StaticContent.class);
		verify(dataService).update(eq(STATIC_CONTENT), staticContentArgumentCaptor.capture());
		verify(staticContentArgumentCaptor.getValue()).setContent(content);
	}

	@Test
	public void testSubmitContentUpdateNotAllowed()
	{
		String key = "key";
		String content = "content";
		doThrow(new MolgenisDataAccessException()).when(dataService)
												  .findOneById(STATIC_CONTENT, key, StaticContent.class);

		assertFalse(staticContentServiceImpl.submitContent(key, content));
	}

	@Test
	public void testIsCurrentUserCanEditWritePermission()
	{
		String key = "key";
		StaticContent staticContent = mock(StaticContent.class);
		when(dataService.findOneById(STATIC_CONTENT, key, StaticContent.class)).thenReturn(staticContent);
		assertTrue(staticContentServiceImpl.isCurrentUserCanEdit(key));
		//		StaticContent staticContent = mock(StaticContent.class).isWritable()).thenReturn(true).getMock();
		throw new UnsupportedOperationException("FIXME");
	}

	@Test
	public void testIsCurrentUserCanEditReadPermission()
	{
		String key = "key";
		StaticContent staticContent = mock(StaticContent.class);
		when(dataService.findOneById(STATIC_CONTENT, key, StaticContent.class)).thenReturn(staticContent);
		assertFalse(staticContentServiceImpl.isCurrentUserCanEdit(key));
		//		StaticContent staticContent = when(mock(StaticContent.class).isWritable()).thenReturn(false).getMock();
		throw new UnsupportedOperationException("FIXME");
	}

	@Test
	public void testIsCurrentUserCanEditNotAllowed()
	{
		String key = "key";
		doThrow(new MolgenisDataAccessException()).when(dataService)
												  .findOneById(STATIC_CONTENT, key, StaticContent.class);
		assertFalse(staticContentServiceImpl.isCurrentUserCanEdit(key));
	}

	@Test
	public void testCheckPermissionWritePermission()
	{
		String key = "key";
		StaticContent staticContent = mock(StaticContent.class);
		when(dataService.findOneById(STATIC_CONTENT, key, StaticContent.class)).thenReturn(staticContent);
		staticContentServiceImpl.checkPermissions(key); // no exception == test success
		//		StaticContent staticContent = when(mock(StaticContent.class).isWritable()).thenReturn(true).getMock();
		throw new UnsupportedOperationException("FIXME");
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void testCheckPermissionReadPermission()
	{
		String key = "key";
		StaticContent staticContent = mock(StaticContent.class);
		when(dataService.findOneById(STATIC_CONTENT, key, StaticContent.class)).thenReturn(staticContent);
		staticContentServiceImpl.checkPermissions(key);
		//		StaticContent staticContent = when(mock(StaticContent.class).isWritable()).thenReturn(false).getMock();
		throw new UnsupportedOperationException("FIXME");
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void testCheckPermissionNotAllowed()
	{
		String key = "key";
		doThrow(new MolgenisDataAccessException()).when(dataService)
												  .findOneById(STATIC_CONTENT, key, StaticContent.class);
		staticContentServiceImpl.checkPermissions(key);
	}

	@Test
	public void testGetContent()
	{
		String key = "key";
		String content = "content";
		StaticContent staticContent = when(mock(StaticContent.class).getContent()).thenReturn(content).getMock();
		when(dataService.findOneById(STATIC_CONTENT, key, StaticContent.class)).thenReturn(staticContent);

		staticContentServiceImpl.getContent(content);
	}
}