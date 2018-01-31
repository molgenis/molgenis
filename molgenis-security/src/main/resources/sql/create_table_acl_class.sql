create table if not exists acl_class(
	id bigserial not null primary key,
	class varchar not null,
	class_id_type varchar not null,
	constraint unique_uk_2 unique(class)
);