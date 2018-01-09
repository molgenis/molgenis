package org.molgenis.data.i18n;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.i18n.properties.AllPropertiesMessageSource;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class LocalizationPopulatorTest extends AbstractMockitoTest
{
	private LocalizationPopulator localizationPopulator;
	private AllPropertiesMessageSource allPropertiesMessageSource;
	@Mock
	private LocalizationService localizationService;
	@Mock
	private L10nStringFactory l10nStringFactory;
	@Mock
	private L10nString enPlusNl;
	@Mock
	private L10nString enOnly;
	@Mock
	private L10nString nlOnly;
	@Mock
	private L10nString biobankUTF8;
	@Captor
	private ArgumentCaptor<Collection<L10nString>> updateCaptor;
	@Captor
	private ArgumentCaptor<Collection<L10nString>> addCaptor;
	private Locale nl = new Locale("nl");
	private Locale en = new Locale("en");

	@BeforeMethod

	public void beforeMethod()
	{
		localizationPopulator = new LocalizationPopulator(localizationService, l10nStringFactory);
		allPropertiesMessageSource = new AllPropertiesMessageSource();
		allPropertiesMessageSource.addMolgenisNamespaces("test");
	}

	@Test
	public void testPopulate()
	{
		// existing
		when(enPlusNl.getMessageID()).thenReturn("EN_PLUS_NL");
		doReturn("updated in database").when(enPlusNl).getString(en);
		doReturn("").when(enPlusNl).getString(nl);
		when(nlOnly.getMessageID()).thenReturn("NL_ONLY");
		doReturn("alleen Nederlands").when(nlOnly).getString(nl);
		when(localizationService.getExistingMessages("test",
				ImmutableSet.of("EN_PLUS_NL", "EN_ONLY", "NL_ONLY", "BIOBANK_UTF8"))).thenReturn(
				Stream.of(enPlusNl, nlOnly));

		// new
		doReturn(enOnly).when(l10nStringFactory).create("EN_ONLY");
		when(enOnly.setMessageID("EN_ONLY")).thenReturn(enOnly);
		when(enOnly.getMessageID()).thenReturn("EN_ONLY");
		doReturn(biobankUTF8).when(l10nStringFactory).create("BIOBANK_UTF8");
		when(biobankUTF8.setMessageID("BIOBANK_UTF8")).thenReturn(biobankUTF8);
		when(biobankUTF8.getMessageID()).thenReturn("BIOBANK_UTF8");

		localizationPopulator.populateLocalizationStrings(allPropertiesMessageSource);

		// updates
		verify(enPlusNl).set("nl", "Engels plus Nederlands");

		// newly created
		verify(enOnly).setNamespace("test");
		verify(enOnly).set("en", "English only");
		verify(biobankUTF8).setNamespace("test");
		verify(biobankUTF8).set("en", "Biøbånk\uD83D\uDC00");
		verify(biobankUTF8).set("nl", "\uD83D\uDC00\uD83C\uDDF3\uD83C\uDDF1");

		verify(localizationService).store(updateCaptor.capture(), addCaptor.capture());
		assertEquals(newArrayList(updateCaptor.getValue()), ImmutableList.of(enPlusNl, nlOnly));
		assertEquals(newArrayList(addCaptor.getValue()), ImmutableList.of(enOnly, biobankUTF8));
	}
}