package service.google;

import java.io.PrintStream;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactGroupEntry;

/**
 * Interface to define the common methods of the {@link ElementHelper}.
 * 
 * Stolen shamelessly from Google's Sample Contacts Tool
 * https://developers.google.com/google-apps/contacts/v3/
 * 
 */
public interface ElementHelperInterface {

	/**
	 * Parses an element from the textual description, and sets or adds it to
	 * the contact entry.
	 * 
	 * @param contact	the contact the parsed element should be added or set. 
	 * @param parser	 the parser used for the parsing of the description.
	 * 
	 * @see ElementParser
	 */
	public void parse(ContactEntry contact, ElementParser parser); 
	
	/**
	 * Parses an element from the textual description, and sets or adds it to
	 * the group entry.
	 * 
	 * @param group	the group the parsed element should be added or set. 
	 * @param parser the parser used for the parsing of the description.
	 * 
	 * @throws UnsupportedOperationException in case the specific element
	 *				 can not be set on a ContactGroupEntry.
	 * 
	 * @see ElementParser
	 */
	public void parseGroup(ContactGroupEntry group, ElementParser parser); 

	/**
	 * Prints the content of the element to a stream.
	 * 
	 * @param out			output stream.
	 * @param contact	the contact containing the element to print.
	 */
	public void print(PrintStream out, ContactEntry contact); 
	
	/**
	 * Updates element of destination contact with data from source contact.
	 * If the source contact entry does not has the specific element, it should
	 * leave the destination contact entry as is, otherwise is should copy the
	 * element from the source to the destination contact entry.
	 *	
	 * @param dest	the destination contact entry.
	 * @param src	 the source contact entry.
	 */
	public void update(ContactEntry dest, ContactEntry src);

	/**
	 * Returns the usage help text regarding the formating of an element
	 * description.
	 * 
	 * @return the usage help text for the element description.
	 */
	public String getUsage();

}
