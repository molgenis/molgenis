INSERT INTO public."sys_sec_Role#b6639604" (name, label, "labelEn", "labelNl", id) VALUES ( 'ANONYMOUS', 'Anonymous','Anonymous', 'Anoniem','Anonymous');

INSERT INTO public."sys_sec_Role#b6639604_includes" (id, includes, "order")(SELECT "role".id,(SELECT id from public."sys_sec_Role#b6639604" where name = 'ANONYMOUS'), COALESCE(MAX("order")+1,0) from public."sys_sec_Role#b6639604" role
LEFT JOIN public."sys_sec_Role#b6639604_includes" ON "role".id = public."sys_sec_Role#b6639604_includes".id
where name = 'USER' GROUP BY "role".id);
