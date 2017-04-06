package org.molgenis.data.i18n;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Sets;
import org.mockito.*;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.data.i18n.model.L10nStringMetaData.L10N_STRING;
import static org.molgenis.data.i18n.model.L10nStringMetaData.MSGID;
import static org.molgenis.data.i18n.model.L10nStringMetaData.NAMESPACE;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { LocalizationService.class, LocalizationServiceTest.Config.class })
public class LocalizationServiceTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private LocalizationService localizationService;
	@Autowired
	private DataService dataService;
	@Autowired
	private L10nStringFactory l10nStringFactory;

	@Mock
	private L10nString string1;
	@Mock
	private L10nString string2;
	@Mock
	private L10nString newString1;
	@Mock
	private L10nString newString2;

	private PropertiesMessageSource propertiesMessageSource;

	@Captor
	private ArgumentCaptor<Stream<L10nString>> updateCaptor;

	@Captor
	private ArgumentCaptor<Stream<L10nString>> addCaptor;

	@BeforeMethod
	public void setUp() throws Exception
	{
		Mockito.reset(l10nStringFactory);

		MockitoAnnotations.initMocks(this);
		when(string1.getMessageID()).thenReturn("EN_PLUS_NL");
		when(string1.getString("en")).thenReturn("string 1 - en");
		when(string1.getString("nl")).thenReturn("string 1 - nl");

		when(string2.getMessageID()).thenReturn("NL_ONLY");
		when(string2.getString("nl")).thenReturn("string 2 - nl");
		when(string2.getString("en")).thenReturn(null);

		when(l10nStringFactory.create(string1)).thenReturn(string1);
		when(l10nStringFactory.create(string2)).thenReturn(string2);

		propertiesMessageSource = new PropertiesMessageSource("test");

	}

	@AfterMethod
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testGetMessage() throws Exception
	{
		Query<L10nString> query = new QueryImpl<L10nString>().eq(MSGID, "EN_PLUS_NL").and().eq(NAMESPACE, "test");
		when(dataService.findOne(L10N_STRING, query, L10nString.class)).thenReturn(string1);

		assertEquals(localizationService.getMessage("test", "EN_PLUS_NL", "nl"), "string 1 - nl");
	}

	@Test
	public void testGetMessages() throws Exception
	{
		Query<L10nString> query = new QueryImpl<L10nString>().eq(NAMESPACE, "test");
		when(dataService.findAll(L10N_STRING, query, L10nString.class)).thenAnswer((i) -> Stream.of(string1, string2));

		assertEquals(localizationService.getMessages("test", "nl"),
				Maps.newHashMap("EN_PLUS_NL", "string 1 - nl", "NL_ONLY", "string 2 - nl"));
	}

	@Test
	public void testUpdateAllLanguages() throws Exception
	{
		Query<L10nString> query = new QueryImpl<L10nString>()
				.in(MSGID, asList("EN_PLUS_NL", "EN_ONLY", "NL_ONLY", "BIOBANK_UTF8")).and().eq(NAMESPACE, "test");
		when(dataService.findAll(L10N_STRING, query, L10nString.class)).thenReturn(Stream.of(string1, string2));

		when(l10nStringFactory.create()).thenReturn(newString1, newString2);
		when(newString1.getMessageID()).thenReturn("EN_ONLY");
		when(newString2.getMessageID()).thenReturn("BIOBANK_UTF8");

		localizationService.addLocalizationStrings(propertiesMessageSource);

		verify(newString1).setNamespace("test");
		verify(newString1).setMessageID("EN_ONLY");
		verify(newString1).set("en", "English only");
		verify(newString2).setNamespace("test");
		verify(newString2).setMessageID("BIOBANK_UTF8");
		verify(newString2).set("en", "Biøbånk\uD83D\uDC00");
		verify(newString2).set("nl", "\uD83D\uDC00\uD83C\uDDF3\uD83C\uDDF1");

		verify(dataService).update(eq(L10N_STRING), updateCaptor.capture());
		assertEquals(updateCaptor.getValue().collect(toSet()), Sets.newHashSet(string1, string2));
		verify(dataService).add(eq(L10N_STRING), addCaptor.capture());
		assertEquals(addCaptor.getValue().collect(toSet()), Sets.newHashSet(newString1, newString2));
	}

	@Test
	public void testAddMissingMessageIDs() throws Exception
	{
		Query<L10nString> query = new QueryImpl<L10nString>().in(MSGID, asList("EN_PLUS_NL", "NEW")).and()
				.eq(NAMESPACE, "test");
		when(dataService.findAll(L10N_STRING, query, L10nString.class)).thenReturn(Stream.of(string1));
		when(newString1.setNamespace("test")).thenReturn(newString1);
		when(newString1.setMessageID("NEW")).thenReturn(newString1);

		localizationService.addMissingMessageIDs("test", Sets.newTreeSet(asList("EN_PLUS_NL", "NEW")));

		when(l10nStringFactory.create()).thenReturn(newString1);

		verify(dataService).add(eq(L10N_STRING), addCaptor.capture());
		assertEquals(addCaptor.getValue().collect(toSet()), Collections.singleton(newString1));
		verify(newString1).setNamespace("test");
		verify(newString1).setMessageID("NEW");
	}

	@Test
	public void testGetKeys() throws Exception
	{
		Query<L10nString> query = new QueryImpl<L10nString>().eq(NAMESPACE, "test");
		when(dataService.findAll(L10N_STRING, query, L10nString.class)).thenReturn(Stream.of(string1, string2));

		Set<String> keys = localizationService.getMessageIDs("test");
		assertEquals(keys, newHashSet("EN_PLUS_NL", "NL_ONLY"));
	}

	@Configuration
	public static class Config
	{
		@Mock
		private L10nStringFactory l10nStringFactory;

		public Config()
		{
			MockitoAnnotations.initMocks(this);
		}

		@Bean
		public L10nStringFactory i18nStringFactory()
		{
			return l10nStringFactory;
		}
	}

}