package models;

import java.text.DateFormat;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class SyncError extends Model {

	public Date whenItHappened;
	public String contactId;
	public Integer googId;
	public String errorMessage;

	@Required
    @ManyToOne
	public SyncRequest job;

	public SyncError() {
		this.whenItHappened = new Date();
	}

	public SyncError(SyncRequest job, String contactId, String errorMessage) {
		this.job = job;
		this.whenItHappened = new Date();
		this.contactId = contactId;
		this.errorMessage = errorMessage;
	}

	public SyncError(SyncRequest job, Integer googId, String errorMessage) {
		this.job = job;
		this.whenItHappened = new Date();
		this.googId = googId;
		this.errorMessage = errorMessage;
	}

	public String toString() {
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(whenItHappened) + ": " + errorMessage;
	}

}
