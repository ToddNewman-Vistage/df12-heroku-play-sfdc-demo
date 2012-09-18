package util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import service.google.ElementHelper;
import service.google.ElementParser;

import com.google.gdata.data.contacts.ContactEntry;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import models.Person;

public class Transformer {

	public static Person sfdcJsonToPerson(JsonObject json) {

		Person p = new Person();

		// transfer JSON to POJO
		p.sfdcId = json.get("Id").getAsString();
		p.firstName = safeGet(json, "FirstName");
		p.lastName = json.get("LastName").getAsString();
		p.title = safeGet(json, "Title");
		p.company = ((JsonObject)(json.get("Account"))).get("Name").getAsString();
		p.nickname = safeGet(json, "Nickname__c");
		p.mobile = safeGet(json, "MobilePhone");
		p.email = safeGet(json, "Email");
		p.city = safeGet(json, "MailingCity");
		p.state = safeGet(json, "MailingState");

		return p;

	}

	private static String safeGet(JsonObject json, String name) {
		if (!json.has(name)) return "";
		if (json.get(name).isJsonNull()) return "";
		return json.get(name).getAsString();
	}

	public static Person googContactToPerson(ContactEntry c) {

		Person p = new Person();

		p.firstName = (c.getName().getGivenName() == null ? "" : c.getName().getGivenName().getValue());
		p.lastName = (c.getName().getFamilyName() == null ? "" : c.getName().getFamilyName().getValue());
		p.nickname = (c.getNickname() == null ? "" : c.getNickname().getValue());
		p.title = (c.getTitle() == null ? "" : c.getTitle().getPlainText());

		if (c.getOrganizations() == null ? false : c.getOrganizations().size() > 0) {
			p.company = c.getOrganizations().get(0).getOrgTitle().getValue();
		}
		if (c.getEmailAddresses() == null ? false : c.getEmailAddresses().size() > 0) {
			p.email = c.getEmailAddresses().get(0).getAddress();
		}
		if (c.getPhoneNumbers() == null ? false : c.getPhoneNumbers().size() > 0) {
			p.mobile = c.getPhoneNumbers().get(0).getPhoneNumber();
		}
		if (c.getPostalAddresses() == null ? false : c.getPostalAddresses().size() > 0) {
			String addressText = c.getPostalAddresses().get(0).getValue();
			if (addressText.indexOf(",") > 0) {
				List<String> pieces = Arrays.asList(addressText.split(","));
				p.city = pieces.get(0);
				if (pieces.size() > 1) p.state = pieces.get(1);
			} else {
				p.city = addressText;
			}
		}

		p.googleId = getIdFromGoogleUrl(c.getId());

		return p;

	}

	public static String getIdFromGoogleUrl(String url) {
		if (url != null) {
			if (url.lastIndexOf("/", url.length()) > 0) {
				return url.substring(url.lastIndexOf("/", url.length()) + 1);
			}
		}
		return null;
	}

	public static void updateContactObjectValues(ContactEntry existingContact, Person updateValues) {

		// Google contact was retrieve with SFDC Id so no need to update that

		// process name
		Name name = new Name();
		name.setFullName(new FullName(updateValues.getFullName(), null));
		if (!"".equals(updateValues.firstName)) name.setGivenName(new GivenName(updateValues.firstName, null));
		name.setFamilyName(new FamilyName(updateValues.lastName, null));
		existingContact.setName(name);

		// process nickname
		Nickname nickname = new Nickname();
		nickname.setValue(("".equals(updateValues.nickname) ? null : updateValues.nickname));
		existingContact.setNickname(nickname);

		// process company/title
		Organization org = new Organization();
		org.setRel("http://schemas.google.com/g/2005#work");
		org.setOrgName(new OrgName(("".equals(updateValues.company) ? null : updateValues.company)));
		org.setOrgTitle(new OrgTitle(("".equals(updateValues.title) ? null : updateValues.title)));
		if (existingContact.getOrganizations().size() > 0) {
			existingContact.getOrganizations().set(0, org);
		} else {
			existingContact.addOrganization(org);
		}

		// process email
		if (!"".equals(updateValues.email))  {
			Email email = new Email();
			email.setRel("http://schemas.google.com/g/2005#work");
			email.setPrimary(true);
			email.setAddress(("".equals(updateValues.email) ? null : updateValues.email));
			if (existingContact.getEmailAddresses().size() > 0) {
				existingContact.getEmailAddresses().set(0, email);
			} else {
				existingContact.addEmailAddress(email);
			}
		} else {
			List<Email> all = existingContact.getEmailAddresses();
			if (all.size() > 0) {
				all.remove(0);
			}
		}

		// process phone
		if (!"".equals(updateValues.mobile))  {
			PhoneNumber phone = new PhoneNumber();
			phone.setRel("http://schemas.google.com/g/2005#mobile");
			phone.setPrimary(true);
			phone.setPhoneNumber(("".equals(updateValues.mobile) ? null : updateValues.mobile));
			if (existingContact.getPhoneNumbers().size() > 0) {
				existingContact.getPhoneNumbers().set(0, phone);
			} else {
				existingContact.addPhoneNumber(phone);
			}
		} else {
			List<PhoneNumber> all = existingContact.getPhoneNumbers();
			if (all.size() > 0) {
				all.remove(0);
			}
		}

		// process mailing address
		StructuredPostalAddress address = new StructuredPostalAddress();
		address.setRel("http://schemas.google.com/g/2005#work");
		address.setPrimary(true);
		address.setCity(new City(("".equals(updateValues.city) ? null : updateValues.city)));
		address.setRegion(new Region(("".equals(updateValues.state) ? null : updateValues.state)));
		if (existingContact.getStructuredPostalAddresses().size() > 0) {
			existingContact.getStructuredPostalAddresses().set(0, address);
		} else {
			existingContact.addStructuredPostalAddress(address);
		}

	}

	public static ContactEntry buildGoogleContact(Person p) {

		Map<String, String> parameters = buildGoogleContactParameters(p);

		ContactEntry contact = new ContactEntry();
		for (String paramName : parameters.keySet()) {
			ElementHelper helper = ElementHelper.find(paramName);
			if (helper == null) {
				throw new IllegalArgumentException("unknown argument: " + paramName);
			}
			helper.parse(contact, new ElementParser(parameters.get(paramName)));
		}

		// put the salesforce ID in a user-defined field so we can find the contact
		// extended property would be better since it's hidden from the UI but AFAIK you can't search on those
		// I'm sure there are google experts out there who could set me straight
		UserDefinedField userDefinedField = new UserDefinedField();
		userDefinedField.setKey("SFDCId");
		userDefinedField.setValue(p.sfdcId);
		contact.addUserDefinedField(userDefinedField);

		return contact;
	}

	private static Map<String, String> buildGoogleContactParameters(Person p) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("name", p.getFullName());
		if (p.nickname == null ? false : !"".equals(p.nickname)) parameters.put("nickname", p.nickname);
		if (p.mobile == null ? false : !"".equals(p.mobile)) parameters.put("phone1", p.mobile + ",rel:http://schemas.google.com/g/2005#mobile,primary:true");
		if (p.email == null ? false : !"".equals(p.email)) parameters.put("email1", p.email + ",rel:http://schemas.google.com/g/2005#work,primary:true");
		if (p.company == null ? false : !"".equals(p.company)) {
			if (p.title == null ? false : !"".equals(p.title)) {
				parameters.put("organization", p.company + ",rel:http://schemas.google.com/g/2005#work,title:" + p.title);
			} else {
				parameters.put("organization", p.company + ",rel:http://schemas.google.com/g/2005#work");
			}
		}
		if (p.state == null ? false : !"".equals(p.state)) {
			if (p.city == null ? false : !"".equals(p.city)) {
				parameters.put("postal", "rel:http://schemas.google.com/g/2005#work,city:" + p.city + ",region:" + p.state + ",primary:true");
			} else {
				parameters.put("postal", "rel:http://schemas.google.com/g/2005#work,region:" + p.state + ",primary:true");
			}
		}
		return parameters;
	}

}
