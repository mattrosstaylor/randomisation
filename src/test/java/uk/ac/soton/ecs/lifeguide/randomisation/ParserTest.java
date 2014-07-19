package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.InvalidTrialException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */

public class ParserTest {

    private static final Logger logger = LoggerFactory.getLogger(ParserTest.class);

    @Test
    public void testAlphanumericConversion() {
        Assert.assertEquals(ParserUtils.toAlphaNumeric("ABCde fg!h04~{];`,"), "abcdefgh04");
        Assert.assertEquals(ParserUtils.toAlphaNumeric(""), "");
        Assert.assertEquals(ParserUtils.toAlphaNumeric("        "), "");
        Assert.assertEquals(ParserUtils.toAlphaNumeric("Test/*-+/\\#¬!\"£$%^&*()_+"), "test");
    }

    @Test
    public void testFileNameTrimming() {
        String test1 = ParserUtils.getAlphanumericFileName("C:/Documents/lol/Look at ThIsFile!!!..txt");
        Assert.assertEquals(test1, "lookatthisfile");

        String test2 = ParserUtils.getAlphanumericFileName("areyoukiddingme");
        Assert.assertEquals(test2, "areyoukiddingme");

        String test3 = ParserUtils.getAlphanumericFileName("");
        Assert.assertEquals(test3, "");
    }

    @Test
    public void testTokenSeparation() {
        Assert.assertEquals(ParserUtils.getTokenAt("? Question : id :  weight    ", ":", 0), "? Question");
        Assert.assertEquals(ParserUtils.getTokenAt("? Question : id :  weight    ", ":", 1), "id");
        Assert.assertEquals(ParserUtils.getTokenAt("? Question : id :  weight    ", ":", 2), "weight");

        Assert.assertEquals(ParserUtils.getTokenAt("TDD 4 LYFE", ":", 0), "TDD 4 LYFE");
        Assert.assertEquals(ParserUtils.getTokenAt("TDD 4 LYFE", ":", 1), "");
        Assert.assertEquals(ParserUtils.getTokenAt("TDD 4 LYFE", ":", 2), "");

        Assert.assertEquals(ParserUtils.getTokenAt("   Negatives   ", ":", -1), "Negatives");

        Assert.assertEquals(ParserUtils.getTokenAt("Testing : 1 : 2 : 3", ":", 3), "3");
        Assert.assertEquals(ParserUtils.getTokenAt("Testing : 1 : 2 : 3", ":", 4), "");

        Assert.assertEquals(ParserUtils.getTokenAt("Multiple    spaces    test", " ", 0), "Multiple");
        Assert.assertEquals(ParserUtils.getTokenAt("Multiple    spaces    test", " ", 1), "spaces");
        Assert.assertEquals(ParserUtils.getTokenAt("Multiple    spaces    test", " ", 2), "test");

        Assert.assertEquals(ParserUtils.getTokenAt("Tokenise with words", "with", 0), "Tokenise");
        Assert.assertEquals(ParserUtils.getTokenAt("Tokenise   with   words", " with  ", 1), "words");
    }

    @Test
    public void testIntParsing() {
        Assert.assertEquals(ParserUtils.stringToIntLimit("", 0), 0);
        Assert.assertEquals(ParserUtils.stringToIntLimit("abc", 12), 12);
        Assert.assertEquals(ParserUtils.stringToIntLimit("4", 0), 4);
        Assert.assertEquals(ParserUtils.stringToIntLimit("2.5", 0), 0);
        Assert.assertEquals(ParserUtils.stringToIntLimit("5000", Integer.MAX_VALUE), 5000);
    }

    @Test
    public void testFloatParsing() {
        Assert.assertEquals(ParserUtils.stringToFloatLimit("", 0), 0, 0.1f);
        Assert.assertEquals(ParserUtils.stringToFloatLimit("abc", 100), 100, 0.1f);
        Assert.assertEquals(ParserUtils.stringToFloatLimit("1.2", 4.5f), 1.2f, 0.1f);
        Assert.assertEquals(ParserUtils.stringToFloatLimit("593.102f", 0), 593.102f, 0.1f);
        Assert.assertEquals(ParserUtils.stringToFloatLimit("1.2e06", 1), 1200000, 0.1f);
    }

    @Test
    public void testParser() {
        try {
            TrialDefinition tDef = TrialLoader.loadTrial("src/test/resources/smoking-survey.txt");
            Assert.assertEquals(tDef.getTrialName(), "smokingsurvey");
            Assert.assertEquals(tDef.getStrategyID(), "BlockedRandomisation");

            List<Attribute> attributes = tDef.getAttributes();
            Assert.assertEquals(attributes.size(), 5);
            Assert.assertEquals(attributes.get(0).getAttributeName(), "gender");
            Assert.assertEquals(attributes.get(1).getAttributeName(), "smokes");
            Assert.assertEquals(attributes.get(2).getAttributeName(), "smokeFreq");
            Assert.assertEquals(attributes.get(3).getAttributeName(), "age");
            Assert.assertEquals(attributes.get(4).getAttributeName(), "bmi");
            Assert.assertTrue(attributes.get(0).isGroupingFactor());
            Assert.assertTrue(attributes.get(3).isGroupingFactor());

            Assert.assertFalse(attributes.get(0).isRawValue());
            Assert.assertFalse(attributes.get(1).isRawValue());
            Assert.assertFalse(attributes.get(2).isRawValue());
            Assert.assertTrue(attributes.get(3).isRawValue());
            Assert.assertTrue(attributes.get(4).isRawValue());

            Assert.assertEquals(attributes.get(0).getGroupCount(), 3);
            Assert.assertEquals(attributes.get(1).getGroupCount(), 2);
            Assert.assertEquals(attributes.get(2).getGroupCount(), 4);

            List<Group> ranges = attributes.get(3).getRanges();
            Assert.assertEquals(ranges.get(0).getRangeMin(), -Float.MAX_VALUE, 0.001f);
            Assert.assertEquals(ranges.get(1).getRangeMin(), 20, 0.001f);
            Assert.assertEquals(ranges.get(2).getRangeMin(), 50, 0.001f);
            Assert.assertEquals(ranges.get(0).getRangeMax(), 20, 0.001f);
            Assert.assertEquals(ranges.get(1).getRangeMax(), 50, 0.001f);
            Assert.assertEquals(ranges.get(2).getRangeMax(), Float.MAX_VALUE, 0.001f);

            ranges = attributes.get(4).getRanges();
            Assert.assertEquals(ranges.get(0).getRangeMin(), -Float.MAX_VALUE, 0.001f);
            Assert.assertEquals(ranges.get(1).getRangeMin(), 27.5, 0.001f);
            Assert.assertEquals(ranges.get(0).getRangeMax(), 27.5, 0.001f);
            Assert.assertEquals(ranges.get(1).getRangeMax(), Float.MAX_VALUE, 0.001f);

/* mrt - this feature is not implemented so who the hell cares?!
            List<Attribute> clusters = tDef.getClusterFactors();
            Assert.assertEquals(clusters.size(), 1);
            Assert.assertEquals(clusters.get(0).getAttributeName(), "smokes");
*/
            List<Treatment> treatments = tDef.getTreatments();
            Assert.assertEquals(treatments.size(), 3);
            Assert.assertEquals(treatments.get(0).getName(), "pill1");
            Assert.assertEquals(treatments.get(1).getName(), "pill2");
            Assert.assertEquals(treatments.get(2).getName(), "placebo");

            Treatment defTreatment = tDef.getDefaultTreatment();
            Assert.assertEquals(defTreatment.getName(), "pill1");
            Assert.assertEquals(defTreatment, treatments.get(0));

            Assert.assertEquals(tDef.getParamCount(), 1);
            Assert.assertEquals(tDef.getStrategyParam("blocksize"), Float.valueOf(20.0f));

            logger.info(tDef.toString());
        } catch (InvalidTrialException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            Assert.assertTrue(false); // Flag the test as a failure.
        }
    }

}
