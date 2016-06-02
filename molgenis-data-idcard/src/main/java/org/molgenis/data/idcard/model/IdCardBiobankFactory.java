package org.molgenis.data.idcard.model;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdCardBiobankFactory extends AbstractEntityFactory<IdCardBiobank, IdCardBiobankMetaData, Integer>
{
	@Autowired
	IdCardBiobankFactory(IdCardBiobankMetaData idCardBiobankMetaData)
	{
		super(IdCardBiobank.class, idCardBiobankMetaData, Integer.class);
	}
}
