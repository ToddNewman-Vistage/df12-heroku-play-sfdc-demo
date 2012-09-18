package controllers;

import java.util.Arrays;
import java.util.List;

import play.mvc.Controller;
import service.SalesforceToGoogleSync;

public class Synchronize extends Controller {

	public static void syncContacts() {
		try {
			// parse comma-seperated IDs from header
			String rawids = request.headers.get("ids").value();
			List<String> ids = Arrays.asList(rawids.split(","));

			// run job immediately
			new SalesforceToGoogleSync(ids).now();

		} catch (Exception e) {
			System.out.println(e);
		}

	}

}
