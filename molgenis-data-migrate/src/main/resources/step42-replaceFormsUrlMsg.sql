-- rename url to hyperlink
-- first add new row
INSERT INTO public."sys_L10nString#95a21e09" (id, msgid, namespace, description, en, nl, de, es, it, pt, fr, xx)
VALUES ('form_not_a_valid_hyperlink', 'form_not_a_valid_hyperlink', 'ui-form', null, 'Not a valid Hyperlink',
        'Geen geldige Hyperlink', null, null, null, null, null, null);
-- then remove old row
DELETE
FROM public."sys_L10nString#95a21e09"
WHERE id LIKE 'form_not_a_valid_url' ESCAPE '#';
