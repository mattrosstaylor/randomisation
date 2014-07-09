package uk.ac.soton.ecs.lifeguide.randomisation.app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.ParticipantLoadException;


public class ParticipantWriter{

	public static void write(String filePath, Map<Integer, Integer> allocations) throws ParticipantLoadException{
		int lineNum = 1;
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

			
			writer.append("id,allocation");
			writer.newLine();
			
			for(Integer id: allocations.keySet()){
				++lineNum;
				writer.append(id + "," + allocations.get(id));
				writer.newLine();
			}

			writer.close();
		}catch(IOException e){
			throw new ParticipantLoadException(e.getMessage(), lineNum);
		}
	}

}
