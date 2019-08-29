CREATE DATABASE molgenis_test;
CREATE USER molgenis WITH encrypted password 'molgenis';
GRANT ALL PRIVILEGES ON DATABASE molgenis_test TO molgenis;