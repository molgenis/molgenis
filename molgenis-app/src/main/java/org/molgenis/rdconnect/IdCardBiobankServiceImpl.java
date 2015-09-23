package org.molgenis.rdconnect;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

//@Service
public class IdCardBiobankServiceImpl implements IdCardBiobankService
{
	private final MetaDataService metaDataService;

	@Autowired
	public IdCardBiobankServiceImpl(MetaDataService metaDataService)
	{
		this.metaDataService = requireNonNull(metaDataService);
	}

	@Override
	public Iterable<Entity> getIdCardBiobanks()
	{
		return Arrays.asList(getIdCardBiobank("0"), getIdCardBiobank("1"), getIdCardBiobank("2"));
	}

	@Override
	public Entity getIdCardBiobank(String id)
	{
		EntityMetaData entityMeta = metaDataService.getEntityMetaData("rdconnect_regbb");
		MapEntity mapEntity = new MapEntity(entityMeta);
		mapEntity.set(entityMeta.getIdAttribute().getName(), id);
		String name;
		switch (id)
		{
			case "0":
				name = "Biobank #0";
				break;
			case "1":
				name = "Biobank #1";
				break;
			case "2":
				name = "Biobank #2";
				break;
			default:
				name = null;
				break;
		}
		if (name != null)
		{
			mapEntity.set("name", name);
		}
		return mapEntity;
	}

	@Override
	public Iterable<Entity> getIdCardBiobanks(Iterable<String> ids)
	{
		return Iterables.transform(ids, new Function<String, Entity>()
		{

			@Override
			public Entity apply(String id)
			{
				return getIdCardBiobank(id);
			}
		});
	}

}
