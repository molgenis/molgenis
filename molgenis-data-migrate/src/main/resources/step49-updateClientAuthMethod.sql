UPDATE "sys_sec_oidc_OidcClient#3e7b1b4d"
SET "clientAuthenticationMethod" =
    CASE
        WHEN "clientAuthenticationMethod" in ('post', 'client_secret_post') THEN 'client_secret_post'
        WHEN "clientAuthenticationMethod" = 'none' THEN 'none'
        WHEN "clientAuthenticationMethod" IS NULL THEN NULL
        ELSE 'client_secret_basic'
    END;