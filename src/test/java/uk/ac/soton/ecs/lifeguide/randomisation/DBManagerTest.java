package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.AllocationException;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */

public class DBManagerTest {
    private static final Logger logger = LoggerFactory.getLogger(DBManager.class);

    private static DBManager dbm;
    private Treatment t1 = new Treatment("d", 3, 5);
    private Treatment t2 = new Treatment("d", 3, 5);
    private Treatment t3 = new Treatment("d", 3, 5);
    private ArrayList<Treatment> treatments = new ArrayList<Treatment>();
    private ArrayList<Group> l1 = new ArrayList<Group>();
    private String trial1 = "trial1" + Math.random();
    private String trial2 = "trial2" + Math.random();
    private String nope = "nope" + Math.random();
    private TrialDefinition tr1;
    private TrialDefinition tr2;
    private TrialDefinition tr4;
    private ArrayList<Attribute> attrs1 = new ArrayList<Attribute>();
    private ArrayList<Attribute> attrs2 = new ArrayList<Attribute>();
    private String random_1 = "yay" + Math.random();
    private HashMap<String, Float> blah = new HashMap<String, Float>();
    private boolean setUp = true;

    @BeforeClass
    public static void setUpConn() {
        try {
            dbm = new DBManager("root", "", "randomisation", "127.0.0.1");
            dbm.connect();
            if (!dbm.checkTablesExist()) {
                dbm.createTables();
            } else {
                logger.debug("Tables match");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUpThings() throws Exception {
        if (setUp) {
            treatments.add(t1);
            treatments.add(t2);
            treatments.add(t3);

            l1.add(new Group("derp1", 0, 3));
            l1.add(new Group("derp2", 4, 6));

            attrs1.add(new Attribute("param1", l1, 2, true));
            attrs1.add(new Attribute("param2", l1, 1, false));
            attrs1.add(new Attribute("param3", l1, 4, false));
            attrs1.add(new Attribute("param4", l1, 3, false));

            attrs2.add(new Attribute("b1", l1, 2, false));
            attrs2.add(new Attribute("b2", l1, 2, false));
            attrs2.add(new Attribute("b3", l1, 2, false));
            attrs2.add(new Attribute("b4", l1, 2, false));

            blah.put("bah", Float.valueOf(42.5f));
            blah.put("bah2", Float.valueOf(42.5f));
            blah.put("bah3", Float.valueOf(42.5f));
            blah.put("bah4", Float.valueOf(42.5f));
            blah.put("bah5", Float.valueOf(42.5f));
            blah.put("bah6", Float.valueOf(42.5f));

            tr1 = new TrialDefinition(trial1, SimpleRandomisation.class, "SimpleRandomization", blah, attrs1, treatments, new int[]{0, 2, 3});
            tr2 = new TrialDefinition(trial2, SimpleRandomisation.class, "SimpleRandomization", blah, attrs1, treatments, new int[]{0, 2, 3});
            tr4 = new TrialDefinition(random_1, SimpleRandomisation.class, "SimpleRandomization", blah, attrs1, treatments, new int[]{0, 2, 3});

            setUp = false;
        }
    }

    @Test
    public void assertUpdate() {

        //this is for simple randomisation
        String rand1 = "SimpleRandomization" + Math.random();

        TrialDefinition def1 = new TrialDefinition(rand1, SimpleRandomisation.class, rand1, blah, attrs1, treatments, new int[]{0, 2, 3});
        Assert.assertTrue(dbm.registerTrial(def1));

        Participant p = new Participant();
        HashMap<String, Float> resps = new HashMap<String, Float>();

        for (int i = 0; i < 150; i++) {
            int rand = (int) Math.round(Math.random()) % 6;
            p.setId(rand);
            resps.put("param1", 1.f);
            resps.put("param2", 4.f);
            resps.put("param3", 3.f);
            resps.put("param4", 0.5f);
            p.setResponses(resps);
            try {
                Assert.assertTrue(dbm.update(def1, p, new StrategyStatistics(), 0));
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        resps.clear();
        resps.put("param1", 1.f);
        resps.put("param2", 4.f);
        resps.put("param33", 3.f);
        p.setResponses(resps);
        try {
            Assert.assertTrue(dbm.update(def1, p, new StrategyStatistics(), 0));
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void assertRegisterIntervention() {
        Assert.assertTrue(dbm.registerTrial(tr1));
        Assert.assertTrue(dbm.registerTrial(tr2));
        //Assert.assertFalse(dbm.registerTrial(tr4)); 
    }

    @Test
    public void assertInterventionExists() {
        String nope = "nope" + Math.random();
        TrialDefinition tr3 = new TrialDefinition(nope, SimpleRandomisation.class, "SimpleRandomization", blah, attrs1, treatments, new int[]{0, 2, 3});

        Assert.assertFalse(dbm.trialExists(tr4));
        Assert.assertFalse(dbm.trialExists(tr3));
        Assert.assertTrue(dbm.registerTrial(tr3));
        Assert.assertTrue(dbm.trialExists(tr3));
    }

    @Test
    public void assertGetCount() {
        try {
            dbm.registerTrial(tr4);

            dbm.registerResponse(6, dbm.getTrialDefinitionId(tr4.getTrialName()), "param1", 1);
            dbm.registerResponse(4, dbm.getTrialDefinitionId(tr4.getTrialName()), "param2", 1);
            dbm.registerResponse(5, dbm.getTrialDefinitionId(tr4.getTrialName()), "param1", 2);
            dbm.registerResponse(4, dbm.getTrialDefinitionId(tr4.getTrialName()), "param2", 2);
            dbm.registerResponse(4, dbm.getTrialDefinitionId(tr4.getTrialName()), "param1", 3);
            dbm.registerResponse(4, dbm.getTrialDefinitionId(tr4.getTrialName()), "param2", 3);

            HashMap<String, Integer> map = new HashMap<String, Integer>();
            map.put("param1", 6);
            map.put("param2", 4);

            Assert.assertEquals(1, dbm.getCount(tr4, map));
            map.put("param1", 7);
            Assert.assertEquals(0, dbm.getCount(tr4, map));

            map.clear();
            map.put("param2", 4);
            Assert.assertEquals(3, dbm.getCount(tr4, map));

            map.put("param2", 7);
            Assert.assertEquals(0, dbm.getCount(tr4, map));

            map.clear();
            Assert.assertEquals(0, dbm.getCount(tr4, map));

            dbm.registerResponse(1, dbm.getTrialDefinitionId(random_1), "male", 1);
            dbm.registerResponse(1, dbm.getTrialDefinitionId(random_1), "smokes", 1);
            dbm.registerResponse(0, dbm.getTrialDefinitionId(random_1), "male", 2);
            dbm.registerResponse(1, dbm.getTrialDefinitionId(random_1), "smokes", 2);
            dbm.registerResponse(1, dbm.getTrialDefinitionId(random_1), "male", 3);
            dbm.registerResponse(0, dbm.getTrialDefinitionId(random_1), "smokes", 3);
            dbm.registerResponse(1, dbm.getTrialDefinitionId(random_1), "male", 4);
            dbm.registerResponse(1, dbm.getTrialDefinitionId(random_1), "smokes", 4);
            dbm.registerResponse(0, dbm.getTrialDefinitionId(random_1), "male", 5);
            dbm.registerResponse(1, dbm.getTrialDefinitionId(random_1), "smokes", 5);

            map.put("male", 1);
            map.put("smokes", 1);
            Assert.assertEquals(2, dbm.getCount(tr4, map));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void assertGetStrategyStatistics() throws SQLException {

        HashMap<String, Float> proper = new HashMap<String, Float>();
        proper.put("blocksize", 10f);
        proper.put("actualsize", 0f);
        proper.put("counter", 0f);
        proper.put("delta", 5f);
        proper.put("seed", (float) new Random().nextInt());

        TrialDefinition trial1 = new TrialDefinition("stats" + Math.random(), SimpleRandomisation.class, "SimpleRandomisation", proper, attrs1, treatments, new int[]{0, 2, 3});
        TrialDefinition trial2 = new TrialDefinition("stats2" + Math.random(), BlockedRandomisation.class, "BlockedRandomisation", new HashMap<String, Float>(), attrs1, treatments, new int[]{0, 2, 3});
        LifeGuideAPI lifeGuideAPI = new ParticipantGenerator(trial1);
        Statistics statistics = null;
        Participant p;
        Map<String, Float> parameters;

        dbm.registerTrial(trial1);
        dbm.setLifeGuideAPI(lifeGuideAPI);
        parameters = Strategy.getStoredParameters(SimpleRandomisation.class, trial1);
        try {
            statistics = dbm.getStrategyStatistics(trial1);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Assert.assertEquals(statistics.getAllNames().size(), trial1.getStratifiedCount() * trial1.getTreatmentCount());
        for (String key : statistics.getAllNames())
            Assert.assertEquals(statistics.getStatistic(key), parameters.get(key));

        lifeGuideAPI = new ParticipantGenerator(trial2);
        dbm.registerTrial(trial2);
        dbm.setLifeGuideAPI(lifeGuideAPI);
        p = lifeGuideAPI.getParticipant(0);
        parameters = Strategy.getStoredParameters(BlockedRandomisation.class, trial2);
        statistics = dbm.getStrategyStatistics(trial2);
        Assert.assertEquals(statistics.getAllNames().size(), trial2.getStratifiedCount() * (3 + trial2.getTreatmentCount()) + 3);
        for (String key : statistics.getAllNames())
            if (!key.contains("seed"))
                Assert.assertEquals(statistics.getStatistic(key), parameters.get(key));

        try {
            Strategy.allocate(trial2.getTrialName(), 0, dbm);
        } catch (AllocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        int strata = trial2.getStratifiedEnumeration(p);
        statistics = dbm.getStrategyStatistics(trial2);

        float[] seed = new float[trial2.getStratifiedCount()];
        float[] actualSize = new float[trial2.getStratifiedCount()];
        float[] counter = new float[trial2.getStratifiedCount()];
        counter[strata]++;
        actualSize[strata] = statistics.getStatistic(strata + "_actualsize");

        Assert.assertEquals(statistics.getStatistic("blocksize"), 10f, 0f);
        Assert.assertEquals(statistics.getStatistic("delta"), 5f, 0f);
        for (int i = 0; i < trial2.getStratifiedCount(); i++) {
            Assert.assertEquals(statistics.getStatistic(i + "_counter"), counter[i], 0f);
            Assert.assertEquals(statistics.getStatistic(i + "_actualsize"), actualSize[i], 0f);
            seed[i] = statistics.getStatistic(i + "_seed");
        }

        try {
            Strategy.allocate(trial2.getTrialName(), 1, dbm);
        } catch (AllocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        strata = trial2.getStratifiedEnumeration(lifeGuideAPI.getParticipant(1));
        try {
            statistics = dbm.getStrategyStatistics(trial2);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        counter[strata]++;
        if (actualSize[strata] == 0) {
            actualSize[strata] = statistics.getStatistic(strata + "_actualsize");
            seed[strata] = statistics.getStatistic(strata + "_seed");
        }

        Assert.assertEquals(statistics.getStatistic("blocksize"), 10f, 0f);
        Assert.assertEquals(statistics.getStatistic("delta"), 5f, 0f);
        for (int i = 0; i < trial2.getStratifiedCount(); i++) {
            Assert.assertEquals(statistics.getStatistic(i + "_counter"), counter[i], 0f);
            Assert.assertEquals(statistics.getStatistic(i + "_actualsize"), actualSize[i], 0f);
            Assert.assertEquals(statistics.getStatistic(i + "_seed"), seed[i], 0f);
        }


    }

    @Test
    public void assertRegisterStrategy() {
        String kim1 = "KimSvensson" + Math.random();
        Assert.assertTrue(dbm.registerStrategy(kim1, "org.sweden.ks"));
        //Assert.assertFalse(dbm.registerStrategy(kim1, "org.sweden.ks")); mrt - I've deleted this on account of it probably being shit
    }

    @Test
    public void assertStrategyExists() {
        String kim1 = "KimSvensson" + Math.random();
        Assert.assertFalse(dbm.strategyExists(kim1));
        Assert.assertTrue(dbm.registerStrategy(kim1, "org.sweden.ks"));
        Assert.assertTrue(dbm.strategyExists(kim1));
        if (!dbm.strategyExists("SimpleRandomisation"))
            dbm.registerStrategy("SimpleRandomisation", "SimpleRandomisation");
        if (!dbm.strategyExists("BlockedRandomisation"))
            dbm.registerStrategy("BlockedRandomisation", "BlockedRandomisation");
        Assert.assertTrue(dbm.strategyExists("SimpleRandomisation"));
        Assert.assertTrue(dbm.strategyExists("BlockedRandomisation"));
    }

    @Test
    public void assertGetTrialDefinition() throws SQLException, ClassNotFoundException {

        String rand1 = "SimpleRandomization" + Math.random();
        String rand2 = "SimpleRandomization2" + Math.random();
        Assert.assertTrue(dbm.registerStrategy(rand1, "SimpleRandomisation"));
        TrialDefinition def1 = new TrialDefinition(rand1, SimpleRandomisation.class, rand1, blah, attrs1, treatments, new int[]{0, 2, 3});
        TrialDefinition def2 = new TrialDefinition(rand2, SimpleRandomisation.class, rand1, blah, attrs1, treatments, new int[]{0, 2, 3});

        Assert.assertTrue(dbm.registerTrial(def1));
        Assert.assertTrue(dbm.registerTrial(def2));
        Assert.assertFalse(def1.toString().equals(dbm.getTrialDefinition(def2.getTrialName()).toString()));
        Assert.assertTrue(def1.toString().equals(dbm.getTrialDefinition(def1.getTrialName()).toString()));
    }

    @Test
    public void assertRegisterStrategyMethods() {
        String rand = "" + Math.random();
        Assert.assertFalse(dbm.strategyExists("SimpleRandomization" + rand));
        Assert.assertTrue(dbm.registerStrategy("SimpleRandomization" + rand, "SimpleRandomisation"));
        //Assert.assertFalse(dbm.registerStrategy("SimpleRandomization" + rand, "SimpleRandomisation")); mrt - same reason
        Assert.assertTrue(dbm.strategyExists("SimpleRandomization" + rand));
    }

    @AfterClass
    public static void assertDisconnect() {
        Assert.assertTrue(dbm.disconnect());
    }
}
