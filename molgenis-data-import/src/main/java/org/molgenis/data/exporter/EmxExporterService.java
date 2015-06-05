package org.molgenis.data.exporter;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.Writable;
import org.molgenis.data.WritableFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

@Service
public class EmxExporterService
{
	private final DataService dataService;

	@Autowired
	public EmxExporterService(DataService dataService)
	{
		this.dataService = dataService;
	}

	public void export(WritableFactory writableFactory)
	{
		for (Repository repository : dataService)
		{
			List<String> attrNames = toAttrNames(repository.getEntityMetaData().getAtomicAttributes());
			Writable writable = writableFactory.createWritable(repository.getName(), attrNames);
			try
			{
				writable.add(repository);
			}
			finally
			{
				try
				{
					writable.close();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

	// public void export(Iterable<Repository> repositories, WritableFactory writableFactory)
	// {
	// // tags
	// List<String> tagsAttrs = toAttrNames(new TagMetaData().getAtomicAttributes());
	// Writable tagsWritable = writableFactory.createWritable(TAGS, tagsAttrs);
	// try
	// {
	//
	// }
	// finally
	// {
	// try
	// {
	// tagsWritable.close();
	// }
	// catch (IOException e)
	// {
	// throw new RuntimeException(e);
	// }
	// }
	//
	// // packages
	// List<String> packagesAttrs = toAttrNames(new PackageMetaData().getAtomicAttributes());
	// Writable packagesWritable = writableFactory.createWritable(PACKAGES, packagesAttrs);
	// try
	// {
	//
	// }
	// finally
	// {
	// try
	// {
	// packagesWritable.close();
	// }
	// catch (IOException e)
	// {
	// throw new RuntimeException(e);
	// }
	// }
	//
	// // attributes
	// List<String> attributesAttrs = toAttrNames(new AttributeMetaDataMetaData().getAtomicAttributes());
	// Writable attributesWritable = writableFactory.createWritable(ATTRIBUTES, attributesAttrs);
	// try
	// {
	//
	// }
	// finally
	// {
	// try
	// {
	// attributesWritable.close();
	// }
	// catch (IOException e)
	// {
	// throw new RuntimeException(e);
	// }
	// }
	//
	// // entities
	// List<String> entitiesAttrs = toAttrNames(new EntityMetaDataMetaData().getAtomicAttributes());
	// Writable entitiesWritable = writableFactory.createWritable(ENTITIES, entitiesAttrs);
	// try
	// {
	//
	// }
	// finally
	// {
	// try
	// {
	// entitiesWritable.close();
	// }
	// catch (IOException e)
	// {
	// throw new RuntimeException(e);
	// }
	// }
	// }

	private List<String> toAttrNames(Iterable<AttributeMetaData> attrs)
	{
		return Lists.newArrayList(Iterables.transform(attrs, new Function<AttributeMetaData, String>()
		{
			@Override
			public String apply(AttributeMetaData attr)
			{
				return attr.getName();
			}
		}));
	}
}
