package org.molgenis.data.i18n;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.data.i18n.model.L10nStringMetaData.*;
import static org.testng.Assert.assertEquals;
import static org.testng.collections.Lists.newArrayList;

public class LocalizationServiceTest extends AbstractMockitoTest
{
	private static final Locale DUTCH = new Locale("nl");

	private LocalizationService localizationService;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private DataService dataService;
	@Mock
	private L10nStringFactory l10nStringFactory;

	@Mock
	private L10nString enPlusNl;
	@Mock
	private L10nString nlOnly;
	@Mock
	private L10nString newString1;
	@Mock
	private L10nString newString2;

	@Captor
	private ArgumentCaptor<Stream<L10nString>> updateCaptor;
	@Captor
	private ArgumentCaptor<Stream<L10nString>> addCaptor;
	@Captor
	private ArgumentCaptor<Stream<L10nString>> deleteCaptor;

	@BeforeMethod
	public void setUp() throws Exception
	{
		localizationService = new LocalizationService(dataService, l10nStringFactory);
	}

	@Test
	public void testGetMessage()
	{
		doReturn("string 1 - nl").when(enPlusNl).getString(DUTCH);
		when(dataService.query(L10N_STRING, L10nString.class).eq(MSGID, "EN_PLUS_NL").findOne()).thenReturn(enPlusNl);

		assertEquals(localizationService.resolveCodeWithoutArguments("EN_PLUS_NL", DUTCH), "string 1 - nl");
	}

	@Test
	public void testGetMessages()
	{
		when(dataService.query(L10N_STRING, L10nString.class).eq(NAMESPACE, "test").findAll()).thenReturn(
				Stream.of(enPlusNl, nlOnly));
		when(enPlusNl.getMessageID()).thenReturn("EN_PLUS_NL");
		doReturn("string 1 - nl").when(enPlusNl).getString(DUTCH);
		when(nlOnly.getMessageID()).thenReturn("NL_ONLY");
		doReturn("string 2 - nl").when(nlOnly).getString(DUTCH);

		assertEquals(localizationService.getMessages("test", DUTCH),
				ImmutableMap.of("EN_PLUS_NL", "string 1 - nl", "NL_ONLY", "string 2 - nl"));
	}

	@Test
	public void testAddMissingMessageIds()
	{
		Set<String> messageIds = ImmutableSet.of("EN_PLUS_NL", "NEW");
		when(dataService.query(L10N_STRING, L10nString.class)
						.in(MSGID, messageIds)
						.and()
						.eq(NAMESPACE, "test")
						.findAll()).thenReturn(Stream.of(enPlusNl));
		when(enPlusNl.getMessageID()).thenReturn("EN_PLUS_NL");

		when(l10nStringFactory.create("NEW")).thenReturn(newString1);
		when(newString1.setMessageID("NEW")).thenReturn(newString1);
		when(newString1.setNamespace("test")).thenReturn(newString1);

		localizationService.addMissingMessageIds("test", messageIds);

		verify(dataService).add(eq(L10N_STRING), addCaptor.capture());
		assertEquals(addCaptor.getValue().collect(toSet()), Collections.singleton(newString1));
	}

	@Test
	public void testDeleteNamespace()
	{
		when(dataService.query(L10N_STRING, L10nString.class).eq(NAMESPACE, "test").findAll()).thenReturn(
				Stream.of(enPlusNl, nlOnly));

		localizationService.deleteNamespace("test");
		verify(dataService).delete(eq(L10N_STRING), deleteCaptor.capture());
		assertEquals(deleteCaptor.getValue().collect(Collectors.toList()), newArrayList(enPlusNl, nlOnly));
	}

	@Test
	public void testStore()
	{
		List<L10nString> toAdd = singletonList(newString1);
		List<L10nString> toUpdate = singletonList(enPlusNl);
		localizationService.store(toUpdate, toAdd);

		verify(dataService).add(eq(L10N_STRING), addCaptor.capture());
		verify(dataService).update(eq(L10N_STRING), updateCaptor.capture());

		assertEquals(addCaptor.getValue().collect(toList()), toAdd);
		assertEquals(updateCaptor.getValue().collect(toList()), toUpdate);
	}

	@Test
	public void testGetAllMessageIds()
	{
		when(dataService.findAll(L10N_STRING, L10nString.class)).thenReturn(Stream.of(enPlusNl, nlOnly));
		when(enPlusNl.getMessageID()).thenReturn("A");
		when(nlOnly.getMessageID()).thenReturn("B");
		assertEquals(localizationService.getAllMessageIds(), Arrays.asList("A", "B"));
	}
}