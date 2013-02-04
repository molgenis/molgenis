<Context path="/molgenis" docBase="molgenis4mpd" debug="5" reloadable="true" crossContext="true">		
	<Resource 
		name="${db_jndiname}" 
		auth="Container" 
		factory="org.apache.commons.dbcp.BasicDataSourceFactory" 
		type="javax.sql.DataSource" 
		username="${db_user}"
		password="${db_password}" 
		driverClassName="${db_driver}" 
		url="${db_uri}?autoReconnect=true"
		validationQuery="select now()"
		maxWait="1000"
		removeAbandoned="true"
		maxActive="8" 
		maxIdle="4"/>		
</Context>