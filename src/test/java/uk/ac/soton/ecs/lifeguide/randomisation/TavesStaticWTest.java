package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.AllocationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */

public class TavesStaticWTest {

    @Test
    public void testPaperExample() {

        List<Treatment> treatments = new LinkedList<Treatment>();
        treatments.add(new Treatment("intervention", 1));
        treatments.add(new Treatment("control", 1));
        List<Attribute> attrs = new LinkedList<Attribute>();
        attrs.add(new Attribute("sex", 2, 1, false));
        attrs.add(new Attribute("age", 3, 1, false));
        attrs.add(new Attribute("risk", 2, 1, false));
        TrialDefinition trialDefinition = new TrialDefinition("hypo", Minimisation.class, "Taves", new HashMap<String, Float>(), attrs, treatments, new int[0]);
        StrategyStatistics stats = new StrategyStatistics();
        //treatment attr group count
        Assert.assertTrue(treatments.get(0).getName().compareTo("intervention") == 0);
        stats.putStatistic("interventionsex0", 3.f);
        stats.putStatistic("interventionsex1", 5.f);
        stats.putStatistic("interventionage0", 4.f);
        stats.putStatistic("interventionage1", 2.f);
        stats.putStatistic("interventionage2", 2.f);
        stats.putStatistic("interventionrisk0", 4.f);
        stats.putStatistic("interventionrisk1", 4.f);

        stats.putStatistic("controlsex0", 5.f);
        stats.putStatistic("controlsex1", 3.f);
        stats.putStatistic("controlage0", 4.f);
        stats.putStatistic("controlage1", 3.f);
        stats.putStatistic("controlage2", 1.f);
        stats.putStatistic("controlrisk0", 5.f);
        stats.putStatistic("controlrisk1", 3.f);

        Minimisation strat = new Minimisation();
        strat.put_stat(stats);
        Participant participant = new Participant();
        Map<String, Float> mymap = new HashMap<String, Float>();
        mymap.put("sex", 0f);
        mymap.put("age", 1f);
        mymap.put("risk", 0f);
        participant.setResponses(mymap);
        int asserts = 0;
        try {
            asserts = strat.allocateImplementation(trialDefinition, participant, null);
        } catch (AllocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Assert.assertEquals(0, asserts);


    }

    @Test
    public void testPaperExample2() {

        List<Treatment> treatments = new LinkedList<Treatment>();
        treatments.add(new Treatment("intervention", 1));
        int maxinterv = 50;
        treatments.get(0).setMaxParticipants(maxinterv);
        treatments.get(0).useParticipantLimit(true);
        ;
        treatments.add(new Treatment("control", 1));
        List<Attribute> attrs = new LinkedList<Attribute>();
        attrs.add(new Attribute("sex", 2, 1, false));
        attrs.add(new Attribute("age", 3, 1, false));
        attrs.add(new Attribute("risk", 2, 1, false));
        TrialDefinition trialDefinition = new TrialDefinition("hypo", Minimisation.class, "Taves", new HashMap<String, Float>(), attrs, treatments, new int[0]);
        StrategyStatistics stats = new StrategyStatistics();
        Assert.assertTrue(treatments.get(0).getName().compareTo("intervention") == 0);
        stats.putStatistic("interventionsex0", 0.f);
        stats.putStatistic("interventionsex1", 0.f);
        stats.putStatistic("interventionage0", 0.f);
        stats.putStatistic("interventionage1", 0.f);
        stats.putStatistic("interventionage2", 0.f);
        stats.putStatistic("interventionrisk0", 0.f);
        stats.putStatistic("interventionrisk1", 0.f);

        stats.putStatistic("controlsex0", 0.f);
        stats.putStatistic("controlsex1", 0.f);
        stats.putStatistic("controlage0", 0.f);
        stats.putStatistic("controlage1", 0.f);
        stats.putStatistic("controlage2", 0.f);
        stats.putStatistic("controlrisk0", 0.f);
        stats.putStatistic("controlrisk1", 0.f);
        stats.putStatistic("certainty", 1.0f);
        Minimisation strat = new Minimisation();
        strat.put_stat(stats);
        Participant participant = new Participant();
        Map<String, Float> mymap = new HashMap<String, Float>();
        mymap.put("sex", 0f);
        mymap.put("age", 1f);
        mymap.put("risk", 0f);
        participant.setResponses(mymap);
        try {
            int asserts = strat.allocateImplementation(trialDefinition, participant, null);
        } catch (AllocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        //Assert.assertEquals(0, asserts);
        int[] count = {0, 0};
        for (int i = 0; i < 1000; i++) {
            // asserts = strat.getAllocation(trialDefinition,participant,null);
            try {
                count[strat.allocateImplementation(trialDefinition, participant, null)]++;
            } catch (AllocationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            Assert.assertTrue(maxinterv >= count[0]);
        }

    }


    public double get_div(int[] sum) {
        return (double) sum[0] / sum[1];
    }

    @Test
    public void test_getMinIntervention() {
        Minimisation test = new Minimisation();
        double[] input = {0.1f, 1f};
        Assert.assertEquals(0, test.getMinIntervention(input));
        int[] sum = {0, 0};
        input[1] = 0.1f;
        double tot_avg = 0.0;
        int avg = 10000;
        for (int x = 0; x < avg; x++) {
            sum[0] = sum[1] = 0;
            for (int i = 0; i < 1000; i++) {
                sum[test.getMinIntervention(input)]++;
            }
            tot_avg += get_div(sum);
        }
        Assert.assertEquals(1, tot_avg / avg, 0.01);
    }


}
