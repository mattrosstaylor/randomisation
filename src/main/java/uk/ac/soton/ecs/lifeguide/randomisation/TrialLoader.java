package uk.ac.soton.ecs.lifeguide.randomisation;

import java.io.*;
import java.util.*;
import org.json.*;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.InvalidTrialException;

public class TrialLoader{

	private static final String STRATEGY_CLASS_PACKAGE = "uk.ac.soton.ecs.lifeguide.randomisation.";

	private static final int DEFAULT_ARM_WEIGHT = 1;
	private static final int DEFAULT_ARM_LIMIT = Integer.MAX_VALUE;

	public static Trial loadTrial(String filePath) throws InvalidTrialException, FileNotFoundException {

		String jsonString = new Scanner(new File(filePath)).useDelimiter("\\A").next();
		JSONObject json = new JSONObject(jsonString);

		Trial t = new Trial();

		Map<String,Double> parameters = new HashMap<String,Double>();

		Iterator iter = json.keys();
		while(iter.hasNext()) {
			String key = (String) iter.next();
			
			if (key.equals("method")) {
				t.setStrategy(json.getString("method"));
			} 
			else if (key.equals("arms")) {
				JSONArray armsData = json.getJSONArray("arms");
				for (int i=0; i<armsData.length(); i++) {
					JSONObject armData = armsData.getJSONObject(i);
					String name = null;
					int weight = DEFAULT_ARM_WEIGHT;
					int limit = DEFAULT_ARM_LIMIT;
					
					name = armData.getString("name");
					
					try {
						weight = armData.getInt("weight");
					}
					catch (JSONException e) {}

					try {
						limit = armData.getInt("limit");
					}
					catch (JSONException e) {}

					t.addArm(new Arm(name, weight, limit));
				}
				try {
					t.setDefaultArm(json.getString("default_arm"));
				}
				catch (JSONException e) {}
			} 
			else if (key.equals("attributes")) {
				JSONArray attributesData = json.getJSONArray("attributes");
				for (int i=0; i<attributesData.length(); i++) {
					JSONObject attributeData = attributesData.getJSONObject(i);

					List<Grouping> groupings = new ArrayList<Grouping>();
					JSONArray groupingsData = attributeData.getJSONArray("groupings");
					for (int j=0; j<groupingsData.length(); j++) {
						JSONObject groupingData = groupingsData.getJSONObject(j);
						double min = -Double.MAX_VALUE;
						double max = Double.MAX_VALUE;
						try {
							min = groupingData.getDouble("min");
						}
						catch (JSONException e) {}
						try {
							max = groupingData.getDouble("max");
						}
						catch (JSONException e) {}
						
						String name = null;
						if (min != -Double.MAX_VALUE && max != Double.MAX_VALUE) {
							name = "" +min +" to " +max;
						}
						else if (min != -Double.MAX_VALUE && max == Double.MAX_VALUE) {
							name = "min " +min;
						}
						else if (min == -Double.MAX_VALUE && max != Double.MAX_VALUE) {
							name = "max " +max;
						}
						
						groupings.add(new Grouping(name, min, max));
					}
					t.addAttribute(new Attribute(attributeData.getString("name"),groupings,1,true));
				}
			} else {
				parameters.put(key,json.getDouble(key));
			}
		}
		t.setParameters(parameters);
		return t;
	}

}
