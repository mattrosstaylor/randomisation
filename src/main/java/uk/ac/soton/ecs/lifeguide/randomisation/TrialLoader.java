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
			else if (key.equals("stratification_variables")) {
				JSONArray variablesData = json.getJSONArray("stratification_variables");
				for (int i=0; i<variablesData.length(); i++) {
					JSONObject variableData = variablesData.getJSONObject(i);

					List<Stratum> strata = new ArrayList<Stratum>();
					JSONArray strataData = variableData.getJSONArray("strata");
					for (int j=0; j<strataData.length(); j++) {
						JSONObject stratumData = strataData.getJSONObject(j);
						double min = RangeStratum.DEFAULT_MIN;
						double max = RangeStratum.DEFAULT_MAX;
						try {
							min = stratumData.getDouble("min");
						}
						catch (JSONException e) {}
						try {
							max = stratumData.getDouble("max");
						}
						catch (JSONException e) {}
						try {
							strata.add(new Stratum(stratumData.getString("value")));
						}
						catch (JSONException e) {
							strata.add(new RangeStratum(min, max));
						}
					}

					double priority = 1.0;

					try {
						priority = variableData.getDouble("priority");
					}
					catch (JSONException e) {}

					t.addVariable(new Variable(variableData.getString("name"),strata,priority));
				}
			} 
			else if (key.equals("default_arm")) {
			}
			else {
				parameters.put(key,json.getDouble(key));
			}
		}
		t.setParameters(parameters);
		return t;
	}

}
