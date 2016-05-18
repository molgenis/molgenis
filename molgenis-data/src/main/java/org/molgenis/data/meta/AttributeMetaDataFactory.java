package org.molgenis.data.meta;

import static java.util.Objects.requireNonNull;

import javax.management.Attribute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class AttributeMetaDataFactory
{
	private AttributeMetaDataMetaData attrMetaDataMetaData;

	public AttributeMetaData create() {
		return new AttributeMetaData(attrMetaDataMetaData);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setAttributeMetaDataMetaData(AttributeMetaDataMetaData attrMetaDataMetaData) {
		this.attrMetaDataMetaData = requireNonNull(attrMetaDataMetaData);
	}
}
