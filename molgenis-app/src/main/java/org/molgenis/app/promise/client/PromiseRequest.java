package org.molgenis.app.promise.client;

import com.google.auto.value.AutoValue;
import org.molgenis.data.Entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import static org.molgenis.app.promise.client.PromiseClientImpl.ACTION_GETDATAFORXML;
import static org.molgenis.app.promise.client.PromiseClientImpl.NAMESPACE_VALUE;

@AutoValue
@XmlType(propOrder =
{ "proj", "PWS", "SEQNR", "securitycode", "username", "passw" })
@XmlRootElement(name = ACTION_GETDATAFORXML, namespace = NAMESPACE_VALUE)
public abstract class PromiseRequest
{
	@XmlElement(namespace = NAMESPACE_VALUE)
	public abstract String getProj();

	@XmlElement(namespace = NAMESPACE_VALUE)
	public abstract String getPWS();

	@XmlElement(namespace = NAMESPACE_VALUE)
	public abstract String getSEQNR();

	@XmlElement(namespace = NAMESPACE_VALUE)
	public abstract String getSecuritycode();

	@XmlElement(namespace = NAMESPACE_VALUE)
	public abstract String getUsername();

	@XmlElement(namespace = NAMESPACE_VALUE)
	public abstract String getPassw();

	public static PromiseRequest create(Entity credentials, String seqNr)
	{
		return new AutoValue_PromiseRequest(credentials.getString("PROJ"), credentials.getString("PWS"), seqNr,
				credentials.getString("SECURITYCODE"), credentials.getString("USERNAME"),
				credentials.getString("PASSW"));
	}

}
