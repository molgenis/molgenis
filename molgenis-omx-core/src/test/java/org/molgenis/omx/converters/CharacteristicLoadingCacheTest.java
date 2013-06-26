package org.molgenis.omx.converters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import javax.persistence.EntityManager;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.omx.observ.Characteristic;
import org.testng.annotations.Test;

public class CharacteristicLoadingCacheTest
{

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void CharacteristicLoadingCache()
	{
		new CharacteristicLoadingCache(null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findCharacteristic() throws DatabaseException
	{
		Database database = mock(Database.class);
		CharacteristicLoadingCache characteristicLoadingCache = new CharacteristicLoadingCache(database);
		Characteristic ch1 = when(mock(Characteristic.class).getName()).thenReturn("ch1").getMock();

		when(ch1.getId()).thenReturn(1);
		Query<Characteristic> query = mock(Query.class);
		when(database.query(Characteristic.class)).thenReturn(query);
		when(query.eq(Characteristic.IDENTIFIER, "ch1")).thenReturn(query);
		when(query.find()).thenReturn(Arrays.asList(ch1));
		EntityManager em = mock(EntityManager.class);
		when(database.getEntityManager()).thenReturn(em);
		when(em.getReference(Characteristic.class, 1)).thenReturn(ch1);

		assertEquals(characteristicLoadingCache.findCharacteristic("ch1"), ch1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findCharacteristics() throws DatabaseException
	{
		Database database = mock(Database.class);
		CharacteristicLoadingCache characteristicLoadingCache = new CharacteristicLoadingCache(database);
		Characteristic ch1 = when(mock(Characteristic.class).getName()).thenReturn("ch1").getMock();
		Characteristic ch2 = when(mock(Characteristic.class).getName()).thenReturn("ch2").getMock();
		when(ch1.getId()).thenReturn(1);
		when(ch2.getId()).thenReturn(2);
		Query<Characteristic> query = mock(Query.class);
		when(database.query(Characteristic.class)).thenReturn(query);
		Query<Characteristic> query2 = mock(Query.class);
		when(query.in(Characteristic.IDENTIFIER, Arrays.asList("ch1", "ch2"))).thenReturn(query2);
		when(query2.find()).thenReturn(Arrays.asList(ch1, ch2));
		EntityManager em = mock(EntityManager.class);
		when(database.getEntityManager()).thenReturn(em);
		when(em.getReference(Characteristic.class, 1)).thenReturn(ch1);
		when(em.getReference(Characteristic.class, 2)).thenReturn(ch2);

		assertEquals(characteristicLoadingCache.findCharacteristics(Arrays.asList("ch1", "ch2")),
				Arrays.asList(ch1, ch2));
	}
}
