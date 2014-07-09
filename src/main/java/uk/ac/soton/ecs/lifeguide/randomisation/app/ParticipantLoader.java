package uk.ac.soton.ecs.lifeguide.randomisation.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.soton.ecs.lifeguide.randomisation.Participant;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.ParticipantLoadException;


public class ParticipantLoader{

	public static List<Participant> load(String csvFilePath) throws ParticipantLoadException{
		List<Participant> participants = new ArrayList<Participant>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
			String line = "";

			String[] responseHeaders = null;
			int lineNum = 1;

			// Get the response keys
			line = reader.readLine();
			if(line != null){
				responseHeaders = line.split(",");
				for(int i = 0; i < responseHeaders.length; ++i)
					responseHeaders[i] = responseHeaders[i].trim();
			} else {
				reader.close();
				String errorMsg = "File is empty. It must at least contain column headings.";
				throw new ParticipantLoadException(errorMsg, lineNum);
			}

			// Get the response values
			while ((line = reader.readLine()) != null){
				++lineNum;
				
				Map<String, Float> responses = new HashMap<String, Float>();
				
				String[] responseList = line.split(",");
				
				// Check for the correct number of response values
				if(responseList.length != responseHeaders.length){
					reader.close();
					String errMsg = "Participant does not have the correct number of column values.";
					throw new ParticipantLoadException(errMsg, lineNum);
				}
				
				int id = -1;
				boolean valid = true;
				
				for(int i = 0; i < responseList.length; ++i){
					
					if(responseHeaders[i].toLowerCase().equals("id")){
						try{
							id = Integer.parseInt(responseList[i]);
						} catch(NumberFormatException e){
							valid = false;
							break;
						}
					} else {
						// Attempt conversion of response to float.
						float value = 0;
						try{
							value = Float.parseFloat(responseList[i]);
						} catch(NumberFormatException e){
							valid = false;
							break;
						}
						responses.put(responseHeaders[i], value);
					}
				}
				
				if(id == -1){
					reader.close();
					String errMsg = "No ID value entered for this participant. Ensure you include a column named 'id'.";
					throw new ParticipantLoadException(errMsg, lineNum);
				}
				
				// Check that the participant is valid before adding.
				if(valid){
					Participant participant = new Participant();
					participant.setResponses(responses);
					participant.setId(id);
					participants.add(participant);
				} else {
					reader.close();
					String errMsg = "Non-numeric value detected.";
					throw new ParticipantLoadException(errMsg, lineNum);
				}
			}
			
			reader.close();
		} catch(IOException e){
			throw new ParticipantLoadException(e.getMessage(), 0);
		}
		
		return participants;
	}
	
}
