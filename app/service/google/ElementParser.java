package service.google;

import java.util.EnumMap;
import java.util.Map;

/**
 * Stolen shamelessly from Google's Sample Contacts Tool
 * https://developers.google.com/google-apps/contacts/v3/
 * 
 * Parser to tokenize element descriptions.
 * The general format of an element specification looks like:
 * [<value>]|[<name_1>:<value_1>][,<name_2>:<value_2>]...[,<name_n>:<value_n>]
 * 
 * For convenience a single unnamed value can be specified. It is treated as 
 * "value:<value>", and the parsed value can be accessed with the VALUE key.
 *
 * 
 */
public class ElementParser {

	// Map of parsed property names and values.
	private Map<PropertyName, String> propertyMap;
		
	/**
	 * Constructor.
	 * Parses the element description, and fills the propertyMap.
	 * 
	 * @param elementDesc the element description.
	 * @throws IllegalArgumentException on badly formated element description.
	 */
	public ElementParser(String elementDesc) throws IllegalArgumentException {
		propertyMap = new EnumMap<PropertyName, String>(PropertyName.class);
		for (String propertyDesc : elementDesc.split(",")) {
			String nameValuePair[] = propertyDesc.split(":", 2);
			if (nameValuePair.length == 1) {
				propertyMap.put(PropertyName.VALUE, propertyDesc);
			} else if (nameValuePair.length == 2){
				propertyMap.put(
						PropertyName.valueOf(nameValuePair[0].toUpperCase()), 
						nameValuePair[1]);
			} else {
				throw new IllegalArgumentException("Badly formated property:" 
						+ propertyDesc);
			}
		}
	}
	
	/**
	 * Checks if the given property has a parsed in value.
	 * 
	 * @param property the property.
	 * @return {@code true} if the given property has a parsed in value,
	 *				 otherwise {@code false} 
	 */
	boolean has(PropertyName property) {
		return propertyMap.containsKey(property);
	}
	
	/**
	 * Retrieves the parsed in value for the given property.
	 * 
	 * @param property the property.
	 * @return the parsed in value for the given property, or {@code null} if
	 *				 the given property was not specified in the element description 
	 *				 string.
	 */
	String get(PropertyName property) {
		return propertyMap.get(property);
	}
	
	/**
	 * Convenience method to check the value of a boolean property.
	 * 
	 * @param property the property.
	 * @return {@code true} if the parsed in value of the given property is "true"
	 *				 {@code false} in any other cases.
	 */
	boolean is(PropertyName property) {
		String value = propertyMap.get(property);
		return (value != null) && (value.toLowerCase().equals("true"));
	}

}
