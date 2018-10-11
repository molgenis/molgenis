create or replace function get_col_names(rel regclass, cols int2[])
  returns text language sql as $$
select string_agg(attname, ', ' order by ordinality)
from pg_attribute,
     unnest(cols) with ordinality
where attrelid = rel
  and attnum = unnest
$$;

DO
$$
DECLARE
  rec   record;
  nbrow bigint;
BEGIN
  FOR rec IN
  select
         conname as constraint_name,
         conrelid::regclass as table_name,
         get_col_names(conrelid, conkey) as column_names,
         confrelid::regclass as foreign_table_name,
         get_col_names(confrelid, confkey) as foreign_column_names,
         confdeltype
  from pg_constraint
  where contype ='f' AND condeferred=FALSE AND conrelid::regclass::text NOT LIKE 'acl_%'
  LOOP
    EXECUTE format('ALTER TABLE public.%s DROP CONSTRAINT "%s"', rec.table_name, rec.constraint_name);
    IF rec.confdeltype = 'c' THEN
      EXECUTE format('ALTER TABLE public.%s ADD CONSTRAINT "%s" FOREIGN KEY ("%s") REFERENCES public.%s ("%s") ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED', rec.table_name, rec.constraint_name, rec.column_names, rec.foreign_table_name, rec.foreign_column_names);
    ELSE
      EXECUTE format('ALTER TABLE public.%s ADD CONSTRAINT "%s" FOREIGN KEY ("%s") REFERENCES public.%s ("%s") DEFERRABLE INITIALLY DEFERRED', rec.table_name, rec.constraint_name, rec.column_names, rec.foreign_table_name, rec.foreign_column_names);
    END IF;
  END LOOP;
END
$$;

drop function get_col_names(rel regclass, cols int2[]);