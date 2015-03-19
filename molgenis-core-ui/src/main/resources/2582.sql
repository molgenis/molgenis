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

SELECT @rownum:=0;
INSERT INTO attributes_parts 
(`order`, identifier, parts)
SELECT 
	@rownum:=@rownum+1, 
	(SELECT c.identifier
	FROM attributes c
	WHERE c.name = attributes.partOfAttribute
	AND	c.entity = attributes.entity),
	identifier 
FROM attributes 
WHERE partOfAttribute IS NOT NULL;

#Renum to 0 index
UPDATE attributes_parts 
SET `order` = `order` - 
	(SELECT minOrder FROM 
		(SELECT identifier, MIN(`order`) AS minOrder 
		FROM attributes_parts other 
		GROUP BY identifier) X 
	WHERE x.identifier = attributes_parts.identifier);

CREATE TABLE entities_attributes
(
	`order`		int(11),
	fullName	varchar(255),
	attributes	varchar(255)
);

SELECT @rownum:=0;
INSERT INTO entities_attributes 
(`order`, fullName, attributes)
SELECT 
	@rownum:=@rownum+1, 
	entity,
	identifier 
FROM attributes ORDER BY entity;

#Renum to 0 index
UPDATE entities_attributes
SET `order` = `order` - 
	(SELECT newOrder FROM 
		(SELECT fullName, MIN(`order`) AS newOrder 
		FROM entities_attributes other 
		GROUP BY fullName) X 
	WHERE x.fullName = entities_attributes.fullName);