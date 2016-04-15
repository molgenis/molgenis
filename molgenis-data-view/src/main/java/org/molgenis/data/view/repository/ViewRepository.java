package org.molgenis.data.view.repository;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.EntityMetaData.AttributeRole;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.view.meta.JoinedAttributeMetaData;
import org.molgenis.data.view.meta.SlaveEntityMetaData;
import org.molgenis.data.view.meta.ViewMetaData;

import com.google.common.collect.Sets;

public class ViewRepository extends AbstractRepository
{
	private final DataService dataService;
	public static final int BATCH_SIZE = 1000;

	private final String name;

	public ViewRepository(String name, DataService dataService)
	{
		this.name = requireNonNull(name);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Sets.newHashSet(QUERYABLE);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		String masterEntityName = getMasterEntityName();

		AttributeMetaData masterIdAttribute = dataService.getEntityMetaData(masterEntityName).getIdAttribute();
		AttributeMetaData masterLabelAttribute = dataService.getEntityMetaData(masterEntityName).getLabelAttribute();

		DefaultEntityMetaData viewMetaData = new DefaultEntityMetaData(name);
		viewMetaData.setBackend("VIEW");
		viewMetaData.setPackage(PackageImpl.defaultPackage);
		viewMetaData.setIdAttribute(masterIdAttribute);
		viewMetaData.setLabelAttribute(masterLabelAttribute);
		viewMetaData.setDescription("A view on the following dataset " + masterEntityName);
		viewMetaData.setLabel(name);

		// Add master compound
		DefaultAttributeMetaData masterCompoundAttribute = new DefaultAttributeMetaData(masterEntityName,
				FieldTypeEnum.COMPOUND);
		masterCompoundAttribute.setAttributesMetaData(dataService.getEntityMetaData(masterEntityName).getAttributes());
		viewMetaData.addAttributeMetaData(masterCompoundAttribute, AttributeRole.ROLE_LOOKUP);

		// Add slave compounds
		Entity view = dataService.query(ViewMetaData.ENTITY_NAME).eq(ViewMetaData.NAME, name).findOne();
		List<String> slaveEntities = newArrayList();
		view.getEntities(ViewMetaData.SLAVE_ENTITIES)
				.forEach(slaveEntity -> slaveEntities.add(slaveEntity.getString(SlaveEntityMetaData.SLAVE_ENTITY)));
		slaveEntities.stream().forEach(joinedEntityName -> {
			DefaultAttributeMetaData slaveCompoundAttribute = new DefaultAttributeMetaData(joinedEntityName,
					FieldTypeEnum.COMPOUND);
			slaveCompoundAttribute.setAttributesMetaData(StreamSupport
					.stream(dataService.getEntityMetaData(joinedEntityName).getAtomicAttributes().spliterator(), false)
					.map(f -> {
				String prefixedAttributeName = prefixSlaveEntityAttributeName(joinedEntityName, f.getName());
				return new DefaultAttributeMetaData(prefixedAttributeName, prefixedAttributeName, f);
			}).collect(Collectors.toList()));
			viewMetaData.addAttributeMetaData(slaveCompoundAttribute, AttributeRole.ROLE_LOOKUP);
		});

		return viewMetaData;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return findAll(new QueryImpl()).iterator();
	}

	@Override
	public long count(Query q)
	{
		return dataService.count(getMasterEntityName(), q);
	}

	@Override
	// TODO Make this streaming
	public Stream<Entity> findAll(Query q)
	{
		Entity view = dataService.query(ViewMetaData.ENTITY_NAME).eq(ViewMetaData.NAME, name).findOne();

		List<Entity> masterEntityEntities = dataService.getRepository(view.getString(ViewMetaData.MASTER_ENTITY))
				.findAll(q).collect(Collectors.toList());

		List<Entity> viewEntityEntities = newArrayList();
		for (Entity masterEntity : masterEntityEntities)
		{
			DefaultEntity viewEntity = new DefaultEntity(getEntityMetaData(), dataService);

			viewEntity.setOnlyAttributesWithSameMetadata(masterEntity);

			for (Entity slaveEntity : view.getEntities(ViewMetaData.SLAVE_ENTITIES))
			{
				String slaveEntityName = slaveEntity.getString(SlaveEntityMetaData.SLAVE_ENTITY);

				Query viewQuery = new QueryImpl();
				int counter = 0;

				for (Entity joinedAttributeEntity : slaveEntity.getEntities(SlaveEntityMetaData.JOINED_ATTRIBUTES))
				{
					if (counter != 0) viewQuery.and();

					viewQuery.eq(joinedAttributeEntity.getString(JoinedAttributeMetaData.JOIN_ATTRIBUTE), masterEntity
							.get(joinedAttributeEntity.getString(JoinedAttributeMetaData.MASTER_ATTRIBUTE)));
					counter++;
				}
				Entity matchingEntity = dataService.findOne(slaveEntityName, viewQuery);

				// TODO Do not add the joinAttributes, this is data duplication and messy
				if (matchingEntity != null)
				{
					for (String attributeName : matchingEntity.getAttributeNames())
					{
						viewEntity.set(prefixSlaveEntityAttributeName(slaveEntityName, attributeName),
								matchingEntity.get(attributeName));
					}
				}
			}

			viewEntityEntities.add(viewEntity);
		}

		return viewEntityEntities.stream();
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		if (id == null) return null;
		return findOne(new QueryImpl().eq(getEntityMetaData().getIdAttribute().getName(), id).fetch(fetch));
	}

	@Override
	public void create()
	{
		/**
		 * Skip this method. The view is dynamic and have not real physical copy.
		 */
	}

	@Override
	public Iterable<AttributeMetaData> getQueryableAttributes()
	{
		return dataService.getMeta().getEntityMetaData(getMasterEntityName()).getAtomicAttributes();
	}

	private String getMasterEntityName()
	{
		String masterEntityName = dataService.query(ViewMetaData.ENTITY_NAME).eq(ViewMetaData.NAME, name).findOne()
				.getString(ViewMetaData.MASTER_ENTITY);
		return masterEntityName;
	}

	private String prefixSlaveEntityAttributeName(String entityName, String attributeName)
	{
		return entityName + "_" + attributeName;
	}
}
