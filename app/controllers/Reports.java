package controllers;

import java.util.List;

import play.mvc.Controller;

import models.SyncError;
import models.SyncRequest;

public class Reports extends Controller {

	public static void index() {
		List<SyncRequest> syncJobs = SyncRequest.findAll();
		render(syncJobs);
	}

	public static void jobErrors(Long id) {
		SyncRequest job = SyncRequest.findById(id);
		List<SyncError> jobErrors = SyncError.find("job =" + id).fetch();
		render(job, jobErrors);
	}

	public static void clearHistory() {
		List<SyncRequest> requests = SyncRequest.findAll();
		for (SyncRequest s : requests) {
			s.delete();
		}
		index();
	}

}
