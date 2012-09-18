package models;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class SyncRequest extends Model {

	public Date whenItHappened;
	public String ids;
	public int syncSuccessCount;
	public int syncErrorCount;

	@OneToMany(mappedBy="job", cascade=CascadeType.ALL)
	public List<SyncError> errors;

	public SyncRequest() {
		this.whenItHappened = new Date();
	}

	public String toString() {
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(whenItHappened) + ": " + ids;
	}

}
