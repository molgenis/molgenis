CREATE SCHEMA molgenis;
CREATE USER molgenis WITH encrypted password 'molgenis';
GRANT ALL PRIVILEGES ON SCHEMA molgenis TO molgenis;