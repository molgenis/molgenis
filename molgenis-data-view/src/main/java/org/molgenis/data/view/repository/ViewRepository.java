package org.molgenis.data.view.repository;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;
import static org.molgenis.data.view.meta.JoinedAttributeMetaData.JOIN_ATTRIBUTE;
import static org.molgenis.data.view.meta.JoinedAttributeMetaData.MASTER_ATTRIBUTE;
import static org.molgenis.data.view.meta.SlaveEntityMetaData.JOINED_ATTRIBUTES;
import static org.molgenis.data.view.meta.SlaveEntityMetaData.SLAVE_ENTITY;
import static org.molgenis.data.view.meta.ViewMetaData.ENTITY_NAME;
import static org.molgenis.data.view.meta.ViewMetaData.MASTER_ENTITY;
import static org.molgenis.data.view.meta.ViewMetaData.NAME;
import static org.molgenis.data.view.meta.ViewMetaData.SLAVE_ENTITIES;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
		DefaultAttributeMetaData masterCompoundAttribute = new DefaultAttributeMetaData(masterEntityName, COMPOUND);
		masterCompoundAttribute.setAttributesMetaData(dataService.getEntityMetaData(masterEntityName).getAttributes());
		viewMetaData.addAttributeMetaData(masterCompoundAttribute, AttributeRole.ROLE_LOOKUP);

		// Add slave compounds
		Entity view = getViewEntity();
		List<String> slaveEntities = newArrayList();
		view.getEntities(SLAVE_ENTITIES).forEach(slaveEntity -> slaveEntities.add(slaveEntity.getString(SLAVE_ENTITY)));
		slaveEntities.stream().forEach(joinedEntityName -> {
			DefaultAttributeMetaData slaveCompoundAttribute = new DefaultAttributeMetaData(joinedEntityName, COMPOUND);
			slaveCompoundAttribute.setAttributesMetaData(StreamSupport
					.stream(dataService.getEntityMetaData(joinedEntityName).getAtomicAttributes().spliterator(), false)
					.map(f -> {
						String prefixedAttributeName = prefixSlaveEntityAttributeName(joinedEntityName, f.getName());
						return new DefaultAttributeMetaData(prefixedAttributeName, prefixedAttributeName, f);
					}).collect(toList()));
			viewMetaData.addAttributeMetaData(slaveCompoundAttribute, ROLE_LOOKUP);
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
	public Stream<Entity> findAll(Query q)
	{
		Entity view = getViewEntity();
		String masterEntityName = view.getString(MASTER_ENTITY);
		return dataService.getRepository(masterEntityName).findAll(q)
				.map(masterEntity -> joinWithSlaveEntities(view, masterEntity));
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		if (id == null) return null;
		return findOne(new QueryImpl().eq(getEntityMetaData().getIdAttribute().getName(), id).fetch(fetch));
	}

	@Override
	public Entity findOne(Query q)
	{
		Entity view = getViewEntity();
		String masterEntityName = view.getString(MASTER_ENTITY);
		Entity masterEntity = dataService.getRepository(masterEntityName).findOne(q);
		return joinWithSlaveEntities(view, masterEntity);
	}

	private DefaultEntity joinWithSlaveEntities(Entity viewEntity, Entity masterEntity)
	{
		DefaultEntity result = new DefaultEntity(getEntityMetaData(), dataService);
		result.setOnlyAttributesWithSameMetadata(masterEntity);

		for (Entity slaveEntity : viewEntity.getEntities(SLAVE_ENTITIES))
		{
			String slaveEntityName = slaveEntity.getString(SLAVE_ENTITY);

			Query viewQuery = new QueryImpl();
			int counter = 0;

			for (Entity joinedAttributeEntity : slaveEntity.getEntities(JOINED_ATTRIBUTES))
			{
				if (counter != 0) viewQuery.and();

				viewQuery.eq(joinedAttributeEntity.getString(JOIN_ATTRIBUTE),
						masterEntity.get(joinedAttributeEntity.getString(MASTER_ATTRIBUTE)));
				counter++;
			}
			Entity matchingEntity = dataService.findOne(slaveEntityName, viewQuery);

			// TODO Do not add the joinAttributes, this is data duplication and messy
			if (matchingEntity != null)
			{
				for (String attributeName : matchingEntity.getAttributeNames())
				{
					result.set(prefixSlaveEntityAttributeName(slaveEntityName, attributeName),
							matchingEntity.get(attributeName));
				}
			}
		}
		return result;
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

	/**
	 * @return the View entity for this view.
	 */
	private Entity getViewEntity()
	{
		return dataService.query(ENTITY_NAME).eq(NAME, name).findOne();
	}

	private String getMasterEntityName()
	{
		return getViewEntity().getString(MASTER_ENTITY);
	}

	private String prefixSlaveEntityAttributeName(String entityName, String attributeName)
	{
		return entityName + "_" + attributeName;
	}
}
