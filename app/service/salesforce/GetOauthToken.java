package service.salesforce;

import com.google.gson.JsonObject;

import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import service.exception.WSException;

public class GetOauthToken {

	private String oauthUri;
	private String clientId;
	private String clientSecret;
	private String sfUsername;
	private String sfPassword;
	private String sfToken;

	public GetOauthToken() {
		this.oauthUri = Play.configuration.getProperty("salesforce.rest.oauthUri");
		this.clientId = Play.configuration.getProperty("salesforce.rest.clientId");
		this.clientSecret = Play.configuration.getProperty("salesforce.rest.clientSecret");
		this.sfUsername = Play.configuration.getProperty("salesforce.rest.sfUsername");
		this.sfPassword = Play.configuration.getProperty("salesforce.rest.sfPassword");
		this.sfToken = Play.configuration.getProperty("salesforce.rest.sfToken");
	}

	public String getToken() throws WSException {

		try {

			// send auth request to Salesforce
			HttpResponse response = WS
					.url(oauthUri)
					.mimeType("application/x-www-form-urlencoded")
					.body("grant_type=password" +
							"&client_id=" + clientId +
							"&client_secret=" + clientSecret + 
							"&username=" + sfUsername +
							"&password=" + sfPassword + sfToken)
					.post();

			// parse out token
			JsonObject json = (JsonObject)response.getJson();
			if (json.has("access_token")) {
				return json.get("access_token").getAsString();
			} else {
				throw new WSException("Oauth request returned but did not contain an access token.  Response was " + response.getString());
			}

		} catch (Exception e) {
			throw new WSException("Got an Exception trying to get token. " + e);
		}

	}

}
