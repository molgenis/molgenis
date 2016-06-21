package org.molgenis.app.promise.client;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.molgenis.data.Entity;

import com.google.auto.value.AutoValue;

@AutoValue
@XmlType(propOrder =
{ "proj", "PWS", "SEQNR", "securitycode", "username", "passw" })
@XmlRootElement(name=PromiseClientImpl.ACTION_GETDATAFORXML, namespace="http://tempuri.org")
public abstract class PromiseRequest
{
	@XmlElement
	public abstract String getProj();

	@XmlElement
	public abstract String getPWS();

	@XmlElement
	public abstract String getSEQNR();

	@XmlElement
	public abstract String getSecuritycode();

	@XmlElement
	public abstract String getUsername();

	@XmlElement
	public abstract String getPassw();

	public static PromiseRequest create(Entity credentials, String seqNr)
	{
		return new AutoValue_PromiseRequest(credentials.getString("PROJ"), credentials.getString("PWS"), seqNr,
				credentials.getString("SECURITYCODE"), credentials.getString("USERNAME"),
				credentials.getString("PASSW"));
	}

}
