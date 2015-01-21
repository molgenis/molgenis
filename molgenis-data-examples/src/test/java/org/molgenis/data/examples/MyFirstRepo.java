package org.molgenis.data.examples;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

import com.google.common.collect.Iterables;

import com.google.common.collect.Iterables;

public class MyFirstRepo extends AbstractRepository
{
	@Override
	public String getName()
	{
		return "Users";
	}

	@Override
	public DefaultEntityMetaData getEntityMetaData()
	{
		DefaultEntityMetaData meta = new DefaultEntityMetaData("Users");
		meta.addAttribute("username");
		meta.addAttribute("active").setDataType(MolgenisFieldTypes.BOOL);
		meta.addAttribute("age").setDataType(MolgenisFieldTypes.INT);

		return meta;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		MapEntity e1 = new MapEntity();
		e1.set("username", "john");

		MapEntity e2 = new MapEntity();
		e2.set("username", "jane");

		return Arrays.asList(new Entity[]
		{ e1, e2 }).iterator();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.emptySet();
	}

	@Override
	public long count()
	{
		return Iterables.size(this);
	}

}
