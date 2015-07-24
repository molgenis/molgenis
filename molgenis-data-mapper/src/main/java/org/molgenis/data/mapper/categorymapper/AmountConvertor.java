package org.molgenis.data.mapper.categorymapper;

import org.jscience.physics.amount.Amount;

interface AmountConvertor
{
	boolean matchCriteria(String description);

	Amount<?> getAmount(String description);
}
