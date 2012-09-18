package service.google;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Person;
import play.Play;
import service.exception.WSException;
import util.Transformer;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.Nickname;
import com.google.gdata.data.contacts.UserDefinedField;
import com.google.gdata.data.extensions.City;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.FamilyName;
import com.google.gdata.data.extensions.FullName;
import com.google.gdata.data.extensions.GivenName;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.OrgName;
import com.google.gdata.data.extensions.OrgTitle;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.data.extensions.Region;
import com.google.gdata.data.extensions.StructuredPostalAddress;
import com.google.gdata.util.AuthenticationException;

/*
 * 
 * NOTE: this is a functional class that can read/insert/update Google contacts
 * It's not meant to be optimally written or a good example of anything. It's
 * good enough for a demo
 * 
 * If you need to interact with Google's API I suggest you look at their java samples at
 * http://code.google.com/p/gdata-java-client/
 * 
 */
public class ContactService {

	private String gUsername;
	private String gPassword;
	private String gProjection;
	private String gFeedBaseUrl;

	private ContactsService service;
	private ContactEntry mostRecentContact;

	public ContactEntry getMostRecentContact() {
		return mostRecentContact;
	}
	public void setMostRecentContact(ContactEntry mostRecentContact) {
		this.mostRecentContact = mostRecentContact;
	}

	public ContactService() throws WSException {
		gUsername = Play.configuration.getProperty("google.ws.username");
		gPassword = Play.configuration.getProperty("google.ws.password");
		gProjection = Play.configuration.getProperty("google.ws.projection");
		gFeedBaseUrl = Play.configuration.getProperty("google.ws.feedBaseUrl");

		try {
			loginToGoogle();
		} catch (Exception e) {
			throw new WSException("Unable to login to Google " + e.getMessage());
		}
	}

	//https://www.google.com/m8/feeds/contacts/{userEmail}/full/{contactId}
	public void retrieveContactById(String id) throws WSException {

		try {

			String url = gFeedBaseUrl
					+ "contacts/"
					+ gUsername + "/" + gProjection + "/" + id;
			URL feedUrl = new URL(url);

			mostRecentContact = service.getEntry(feedUrl, ContactEntry.class);
			System.out.println("TTN: in searching for " + id + ", found a contact " + mostRecentContact.getId());

		} catch (Exception e) {
			throw new WSException("Unable to search for contact by Goog ID " + e.getMessage());
		}

	}

	// GET https://www.google.com/m8/feeds/contacts/df12contactsync@gmail.com/full?q=Benioff
	public int findContactByOtherValue(String value) throws WSException {

		try {

			String url = gFeedBaseUrl
					+ "contacts/default/full?q=" + value;
			URL feedUrl = new URL(url);

			// Create query and submit a request
			Query myQuery = new Query(feedUrl);

			ContactFeed resultFeed = service.query(myQuery, ContactFeed.class);
			if (resultFeed.getTotalResults() > 0) {
				mostRecentContact = resultFeed.getEntries().get(0);
			}

			return resultFeed.getTotalResults();

		} catch (Exception e) {
			throw new WSException("Unable to search for Google contact by SFDC id " + e.getMessage());
		}

	}

	// https://www.google.com/m8/feeds/contacts/{userEmail}/full
	public void addContact(Person p) throws WSException {

		try {

			ContactEntry contact = Transformer.buildGoogleContact(p);

			String url = gFeedBaseUrl
					+ "contacts/"
					+ gUsername + "/" + gProjection;
			URL feedUrl = new URL(url);

			mostRecentContact = service.insert(feedUrl, contact);
			System.out.println("TTN: created a contact " + Transformer.getIdFromGoogleUrl(mostRecentContact.getId()));

		} catch (Exception e) {
			throw new WSException("Unable to insert Google contact " + e.getMessage());
		}

	}

	// https://www.google.com/m8/feeds/contacts/userEmail/full/{contactId}
	public void updateContact(ContactEntry existingContact, Person updateValues) throws WSException {

		try {

			String googId = Transformer.getIdFromGoogleUrl(existingContact.getId());
			String url = gFeedBaseUrl
					+ "contacts/"
					+ gUsername + "/" + gProjection + "/" + googId;
			URL feedUrl = new URL(url);

			Transformer.updateContactObjectValues(existingContact, updateValues);

			service.update(feedUrl, existingContact);
			System.out.println("TTN: updated contact " + googId);

		} catch (Exception e) {
			throw new WSException("Unable to update Google contact " + e.getMessage());
		}

	}

	public void upsertContact(Person sContact) throws WSException {
		if (findContactByOtherValue(sContact.sfdcId) > 0) {
			updateContact(getMostRecentContact(), sContact);
		} else {
			addContact(sContact);
		}
	}

	//https://www.google.com/m8/feeds/contacts/{userEmail}/full/{contactId}
	// but have to use an "ETag" so the method on the contact object is useful
	public void deleteContact(String id) throws WSException {

		try {

			retrieveContactById(id);
			mostRecentContact.delete();

		} catch (Exception e) {
			throw new WSException("Unable to delete Google contact " + e.getMessage());
		}

	}


	private void loginToGoogle() throws MalformedURLException, AuthenticationException {
		service = new ContactsService("Dreamforce-sync-sample-app");
		service.setUserCredentials(gUsername, gPassword);
	}

}
