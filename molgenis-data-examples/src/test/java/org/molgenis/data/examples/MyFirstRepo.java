package org.molgenis.data.examples;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

public class MyFirstRepo implements Repository
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
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		return null;
	}

	@Override
	public String getUrl()
	{
		return null;
	}

	@Override
	public void close() throws IOException
	{

	}

}
