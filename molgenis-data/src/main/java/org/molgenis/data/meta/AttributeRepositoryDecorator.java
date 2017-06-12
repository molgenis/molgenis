package org.molgenis.data.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Decorator for the attribute repository:
 * - filters requested entities based on the entity permissions of the current user.
 * - applies attribute metadata updates to the backend
 * <p>
 * TODO replace permission based entity filtering with generic row-level security once available
 */
public class AttributeRepositoryDecorator extends AbstractRepositoryDecorator<Attribute>
{
	private final Repository<Attribute> decoratedRepo;
	private final DataService dataService;

	public AttributeRepositoryDecorator(Repository<Attribute> decoratedRepo, DataService dataService)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	protected Repository<Attribute> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public void update(Attribute attr)
	{
		updateBackend(attr);
		decoratedRepo.update(attr);
	}

	@Override
	public void update(Stream<Attribute> attrs)
	{
		decoratedRepo.update(attrs.filter(attr ->
		{
			updateBackend(attr);
			return true;
		}));
	}

	@Override
	public void delete(Attribute attr)
	{
		// If compound attribute is deleted then change the parent of children to null
		// This will change the children attributes into regular attributes.
		if (AttributeType.COMPOUND.equals(attr.getDataType()))
		{
			attr.getChildren().forEach(e ->
			{
				if (null != e.getParent())
				{
					dataService.getMeta()
							   .getRepository(AttributeMetadata.ATTRIBUTE_META_DATA)
							   .update(e.setParent(null));
				}
			});
		}

		// remove this attribute
		decoratedRepo.delete(attr);
	}

	@Override
	public void delete(Stream<Attribute> attrs)
	{
		// The validateDeleteAllowed check if querying the table in which we are deleting. Since the decorated repo only
		// guarantees that the attributes are deleted after the operation completes we have to delete the attributes one
		// by one
		attrs.forEach(this::delete);
	}

	@Override
	public void deleteById(Object id)
	{
		Attribute attr = findOneById(id);
		delete(attr);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		delete(findAll(ids));
	}

	@Override
	public void deleteAll()
	{
		delete(this.query().findAll());
	}

	/**
	 * Updates an attribute's representation in the backend for each concrete {@link EntityType} that
	 * has the {@link Attribute}.
	 *
	 * @param attr        current version of the attribute
	 * @param updatedAttr new version of the attribute
	 */
	private void updateAttributeInBackend(Attribute attr, Attribute updatedAttr)
	{
		MetaDataService meta = dataService.getMeta();
		meta.getConcreteChildren(attr.getEntity())
			.forEach(entityType -> meta.getBackend(entityType).updateAttribute(entityType, attr, updatedAttr));
	}

	private void updateBackend(Attribute attr)
	{
		Attribute currentAttr = findOneById(attr.getIdentifier());
		updateAttributeInBackend(currentAttr, attr);
	}
}
