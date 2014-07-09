package uk.ac.soton.ecs.lifeguide.randomisation;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.InvalidUserDataException;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * A class providing access to a function which converts user data in XML format to a Participant object.
 *
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dionisio Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */
public class UserDataLoader {

	private static final String ATTRIBUTE_FLAG = "value";
	private static final String ATTRIBUTE_ID_FLAG = "identifier";
	private static final String ATTRIBUTE_TYPE_FLAG = "baseType";


	/**
	 * Constructs a Participant object from the given XML user data.
	 *
	 * @param userData The user's question/answer pairs, as XML.
	 * @return The Participant object, storing a map of responses.
	 * @throws InvalidUserDataException Contains a message with a human-readable description of the problem,
	 *                                  giving the XML element responsible (where possible).
	 */
	public static Participant loadParticipant(String userData) throws InvalidUserDataException {
		Participant participant = new Participant();
		Map<String, Float> responseMap = new HashMap<String, Float>();

		try {
			// Set up the XML reader
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(userData));

			String identifier = null;
			float value = 0.0f;

			// Iterate through the elements
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();

				// New element
				if (event.isStartElement()) {
					StartElement element = event.asStartElement();

					if (element.getName().getLocalPart().equals(ATTRIBUTE_FLAG)) {
						// Reset values
						identifier = null;
						value = 0.0f;

						// Get attribute ID/type
						javax.xml.stream.events.Attribute idAttr = element.getAttributeByName(new QName(ATTRIBUTE_ID_FLAG));
						javax.xml.stream.events.Attribute typeAttr = element.getAttributeByName(new QName(ATTRIBUTE_TYPE_FLAG));

						String type = null;

						// Reject the attribute if it has no identifier or type.
						if (idAttr == null || typeAttr == null
							|| (identifier = idAttr.getValue()) == null
							|| (type = typeAttr.getValue()) == null) {
							String errorMsg = "Attribute is missing either an identifier or a type.";
							throw new InvalidUserDataException(errorMsg, element.toString());
						}

						// Ignore the attribute if the type is specified as anything other than a float or an integer.
						if (!(type.toLowerCase().equals("float") || type.toLowerCase().equals("integer"))) {
							identifier = null;
							value = 0.0f;
							continue;
						}

						// Check that the attribute is followed by a value
						event = eventReader.nextEvent();
						if (event.isCharacters()) {
							try {
								value = Float.parseFloat(event.asCharacters().getData());
							} catch (NumberFormatException e) {
								String errorMsg = "Data value is not in a valid numeric format: " + event.asCharacters().getData();
								throw new InvalidUserDataException(errorMsg, element.toString());
							}
						} else {
							String errorMsg = "An attribute is missing a value.";
							throw new InvalidUserDataException(errorMsg, element.toString());
						}
					}

					// End of an element
				} else if (event.isEndElement()) {
					if (identifier != null)
						responseMap.put(identifier, value);
				}

			}
		} catch (XMLStreamException e) {
			throw new InvalidUserDataException(e.getMessage(), "none");
		}

		participant.setResponses(responseMap);
		return participant;
	}

}
