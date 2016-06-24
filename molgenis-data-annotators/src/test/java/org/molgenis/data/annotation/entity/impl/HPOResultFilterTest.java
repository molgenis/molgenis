package org.molgenis.data.annotation.entity.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.hpo.HPOAnnotator;
import org.molgenis.data.annotation.entity.impl.hpo.HPOAnnotator.HPOResultFilter;
import org.molgenis.data.annotation.entity.impl.hpo.HPORepository;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

import com.google.common.base.Optional;

public class HPOResultFilterTest
{
	@Test
	public void filterResults()
	{
		HPOResultFilter filter = new HPOResultFilter();

		Entity e1 = new MapEntity();
		e1.set(HPORepository.HPO_ID_COL_NAME, "id1");
		e1.set(HPORepository.HPO_TERM_COL_NAME, "term1");

		Entity e2 = new MapEntity();
		e2.set(HPORepository.HPO_ID_COL_NAME, "id2");
		e2.set(HPORepository.HPO_TERM_COL_NAME, "term2");

		Optional<Entity> result = filter.filterResults(Arrays.asList(e1, e2), new MapEntity(), false);
		assertTrue(result.isPresent());
		assertEquals(result.get().getString(HPOAnnotator.HPO_IDS), "id1/id2");
		assertEquals(result.get().getString(HPOAnnotator.HPO_TERMS), "term1/term2");
	}
}
