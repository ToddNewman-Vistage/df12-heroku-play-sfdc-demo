package service;

import java.util.List;

import models.Person;
import models.SyncError;
import models.SyncRequest;

import play.jobs.Job;
import service.exception.WSException;
import service.google.ContactService;
import service.salesforce.SfdcContactService;

public class SalesforceToGoogleSync extends Job {

	private List<String> contactIds;

	public SalesforceToGoogleSync(List<String> contactIds) {
		this.contactIds = contactIds;
	}

	// NOTE: made this an async job so that salesforce doesn't time out waiting for all the REST calls
	public void doJob() {

		// create log that this job was run
		SyncRequest job = new SyncRequest();
		job.ids = contactIds.toString();
		job.create();

		// sync the profiles one by one
		for (String id : contactIds) {
			if (updateSingleProfile(id, job)) {
				job.syncSuccessCount++;
			} else {
				job.syncErrorCount++;
			}
		}

		// update new job with final counts
		job.save();

	}

	public boolean updateSingleProfile(String contactId, SyncRequest job) {

		try {

			// retrieve contact details from SFDC
			SfdcContactService sContactService = new SfdcContactService();
			Person sfdcContact = sContactService.query(contactId);

			// insert/update in google
			ContactService contactService = new ContactService();
			contactService.upsertContact(sfdcContact);

			return true;

		} catch (WSException e) {
			System.out.println(e.getMessage() + " while processing " + contactId);
			new SyncError(job, contactId, e.getMessage()).save();
			return false;
		}

	}

}
