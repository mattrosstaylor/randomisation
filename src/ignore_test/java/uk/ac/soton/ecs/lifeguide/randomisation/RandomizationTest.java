package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.AllocationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */

public class RandomizationTest {
    List<TrialDefinition> trialDefinitions;
    List<Integer> populations;

    @Test
    public void testSimpleRandomization() {

        MemoryDBConnector connector = new MemoryDBConnector();
        ParticipantGenerator participantGenerator = new ParticipantGenerator(trialDefinitions.get(0));
        for (int population : populations) {
            connector.connect();
            for (TrialDefinition trialDefinition : trialDefinitions) {
                trialDefinition.setStrategy(SimpleRandomisation.class);
                //participantGenerator = new ParticipantGenerator(trialDefinition);
                connector.setLifeGuideAPI(participantGenerator);
                connector.registerTrial(trialDefinition);

                double error = population / 10;
                List<Treatment> treatments = trialDefinition.getTreatments();
                int[] results = new int[treatments.size()];
                int[] expectedResults = new int[treatments.size()];

                //Sum of weights
                int sum = 0;
                for (int i = 0; i < treatments.size(); i++)
                    sum += treatments.get(i).getWeight();

                //Set expected results for treatments which are suppose to reach their limit
                for (int i = 0; i < expectedResults.length; i++)
                    if (population * treatments.get(i).getWeight() / sum > treatments.get(i).getParticipantLimit())
                        expectedResults[i] = treatments.get(i).getParticipantLimit();

                //Distribute the rest population among the other treatments
                int rest = 0;
                for (int i = 0; i < expectedResults.length; i++)
                    if (expectedResults[i] > 0) {
                        sum -= treatments.get(i).getWeight();
                        rest += expectedResults[i];
                    }
                for (int i = 0; i < expectedResults.length; i++)
                    if (expectedResults[i] == 0) {
                        expectedResults[i] = (population - rest) * treatments.get(i).getWeight() / sum;
                        if (expectedResults[i] > treatments.get(i).getParticipantLimit())
                            expectedResults[i] = treatments.get(i).getParticipantLimit();
                    }

                sum = 0;
                for (int i = 0; i < expectedResults.length; i++)
                    sum += expectedResults[i];
                for (int i = 0; i < population; i++) {
                    Participant participant = participantGenerator.getParticipant(i);
                    int arm = 0;
                    try {
                        arm = Strategy.allocate(trialDefinition.getTrialName(), participant.getId(), connector);
                    } catch (AllocationException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    if (arm == -1)
                        break;
                    results[arm]++;
                }
                int checksum = 0;
                for (int i = 0; i < results.length; i++) {
                    Assert.assertEquals(expectedResults[i], results[i], error);
                    checksum += results[i];
                }
                Assert.assertEquals(sum, checksum, 10);

            }
            connector.disconnect();
        }
    }

    @Test
    public void testBlockedRandomization() {
        MemoryDBConnector connector = new MemoryDBConnector();
        ParticipantGenerator participantGenerator = new ParticipantGenerator(trialDefinitions.get(0));
        for (int population : populations) {
            connector.connect();
            for (TrialDefinition trialDefinition : trialDefinitions) {
                trialDefinition.setStrategy(BlockedRandomisation.class);
                //participantGenerator = new ParticipantGenerator(trialDefinition);
                connector.setLifeGuideAPI(participantGenerator);
                connector.registerTrial(trialDefinition);

                double error = population / 10;
                List<Treatment> treatments = trialDefinition.getTreatments();
                int[] results = new int[treatments.size()];
                int[] expectedResults = new int[treatments.size()];

                //Sum of weights
                int sum = 0;
                for (int i = 0; i < treatments.size(); i++)
                    sum += treatments.get(i).getWeight();

                //Set expected results for treatments which are suppose to reach their limit
                for (int i = 0; i < expectedResults.length; i++)
                    if (population * treatments.get(i).getWeight() / sum > treatments.get(i).getParticipantLimit())
                        expectedResults[i] = treatments.get(i).getParticipantLimit();

                //Distribute the rest population among the other treatments
                int rest = 0;
                for (int i = 0; i < expectedResults.length; i++)
                    if (expectedResults[i] > 0) {
                        sum -= treatments.get(i).getWeight();
                        rest += expectedResults[i];
                    }
                for (int i = 0; i < expectedResults.length; i++)
                    if (expectedResults[i] == 0) {
                        expectedResults[i] = (population - rest) * treatments.get(i).getWeight() / sum;
                        if (expectedResults[i] > treatments.get(i).getParticipantLimit())
                            expectedResults[i] = treatments.get(i).getParticipantLimit();
                    }

                sum = 0;
                for (int i = 0; i < expectedResults.length; i++)
                    sum += expectedResults[i];
                for (int i = 0; i < population; i++) {
                    Participant participant = participantGenerator.getParticipant(i);
                    int arm = 0;
                    try {
                        arm = Strategy.allocate(trialDefinition.getTrialName(), participant.getId(), connector);
                    } catch (AllocationException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    if (arm == -1)
                        break;
                    results[arm]++;
                }
                int checksum = 0;
                for (int i = 0; i < results.length; i++) {
                    Assert.assertEquals(expectedResults[i], results[i], error);
                    checksum += results[i];
                }
                Assert.assertEquals(sum, checksum, 10);

            }
            connector.disconnect();
        }
    }

    @Before
    public void setUp() {
        populations = Arrays.asList(new Integer[]{100, 500, 1000, 5000, 10000});
        trialDefinitions = new ArrayList<TrialDefinition>(2);

        TrialDefinition trialDefinition = new TrialDefinition();
        trialDefinition.setTrialName("test1");
        List<Treatment> treatments = new ArrayList<Treatment>();
        treatments.add(new Treatment("ControlGroup", 1, 500));
        treatments.add(new Treatment("Treatment 1", 2, 20000));
        treatments.add(new Treatment("Treatment 2", 1, 10000));
        treatments.add(new Treatment("Treatment 3", 1, 10000));
        treatments.add(new Treatment("Treatment 4", 2, 20000));
        trialDefinition.setTreatments(treatments);
        trialDefinition.setStrategyParam("blocksize", 21.0f);
        trialDefinitions.add(trialDefinition);


        trialDefinition = new TrialDefinition();
        trialDefinition.setTrialName("test2");
        treatments = new ArrayList<Treatment>();
        treatments.add(new Treatment("Treatment 1", 2, 250));
        treatments.add(new Treatment("Treatment 2", 3, 40000));
        treatments.add(new Treatment("Treatment 3", 4, 40000));
        trialDefinition.setStrategyParam("blocksize", 27.0f);
        trialDefinition.setTreatments(treatments);
        trialDefinitions.add(trialDefinition);

    }

    @After
    public void cleanUp() {
        trialDefinitions = null;
        populations = null;
    }
}