ALTER TABLE attributes_tags
ADD COLUMN `order` int(11);

ALTER TABLE entities_tags
ADD COLUMN `order` int(11);

ALTER TABLE packages_tags
ADD COLUMN `order` int(11);

CREATE TABLE attributes_parts
( 
	`order`		int(11),
	identifier	varchar(255),
	parts		varchar(255)
);

INSERT INTO attributes_parts
(`order`, identifier, parts)
SELECT
	null,
	(SELECT c.identifier
		FROM attributes c
		WHERE c.name = attributes.partOfAttribute
		AND c.entity = attributes.entity),
	identifier
FROM attributes
WHERE partOfAttribute IS NOT NULL;

CREATE TABLE entities_attributes
(
	`order`		int(11),
	fullName	varchar(255),
	attributes	varchar(255)
);

INSERT INTO entities_attributes
(`order`, fullName, attributes)
SELECT
	null,
	entity,
	identifier
FROM attributes;

ALTER TABLE entities
ADD COLUMN backend varchar(255);

ALTER TABLE attributes
ADD COLUMN expression varchar(255);

ALTER TABLE attributes DROP FOREIGN KEY attributes_ibfk_1;

ALTER TABLE attributes DROP FOREIGN KEY attributes_ibfk_2;

ALTER TABLE attributes
DROP COLUMN partOfAttribute;

ALTER TABLE attributes
DROP COLUMN entity;