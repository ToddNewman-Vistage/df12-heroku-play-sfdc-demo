package service.salesforce;

import util.Transformer;
import models.Person;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import play.Play;
import play.cache.Cache;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import service.exception.WSException;

public class SfdcContactService {

	private String token;
	private String version;
	private String urlBase;

	public SfdcContactService() throws WSException {

		// check for token in Play cache
		token = Cache.get("sfdcToken", String.class);
		if (token == null) {
			// it's not there, get a new one
			GetOauthToken t = new GetOauthToken();
			this.token = t.getToken();
			Cache.set("sfdcToken", token);
		}

		getConfigValues();

	}

	private void getConfigValues() {
		this.urlBase = Play.configuration.getProperty("salesforce.rest.baseurl");
		this.version = Play.configuration.getProperty("salesforce.rest.version");
	}

	public Person query(String contactId) throws WSException {

		try {

			// build endpoint
			String requestURL = urlBase + "/services/data/" + version + "/sobjects/Contact/" + contactId;

			// REST get contact by ID call
			HttpResponse response = WS
					.url(requestURL)
					.setHeader("Authorization", "OAuth " + token)
					.setParameter("fields", "Id,LastName,FirstName,Title,Nickname__c,Account.Name,MobilePhone,Email,MailingCity,MailingState")
					.get();

			// get json response
			JsonObject json = (JsonObject)response.getJson();
			// transform to model object
			return Transformer.sfdcJsonToPerson(json);

		} catch (Exception e) {
			throw new WSException("Exception during get salesforce contact REST call " + e);
		}

	}

}
