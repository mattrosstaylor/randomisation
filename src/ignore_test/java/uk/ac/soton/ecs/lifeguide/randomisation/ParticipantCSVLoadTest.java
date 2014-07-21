package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.app.ParticipantLoader;
import uk.ac.soton.ecs.lifeguide.randomisation.exception.ParticipantLoadException;
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

public class ParticipantCSVLoadTest {

    private static final Logger logger = LoggerFactory.getLogger(ParticipantCSVLoadTest.class);

    @Test
    public void testCSVLoading() {
        try {
            List<Participant> participants = ParticipantLoader.load("src/test/resources/participants.csv");

            Assert.assertEquals(participants.size(), 6);

            Participant p = participants.get(0);
            Assert.assertEquals(p.getResponse("age"), 2, 0.0001f);
            Assert.assertEquals(p.getResponse("gender"), 0, 0.0001f);
            Assert.assertEquals(p.getResponse("ethnicity"), 8, 0.0001f);
            Assert.assertEquals(p.getResponse("bmi"), 16.2f, 0.0001f);
            Assert.assertEquals(p.getId(), 1);

            p = participants.get(1);
            Assert.assertEquals(p.getResponse("age"), 1, 0.0001f);
            Assert.assertEquals(p.getResponse("gender"), 0, 0.0001f);
            Assert.assertEquals(p.getResponse("ethnicity"), 4, 0.0001f);
            Assert.assertEquals(p.getResponse("bmi"), 21.3f, 0.0001f);
            Assert.assertEquals(p.getId(), 2);

            p = participants.get(2);
            Assert.assertEquals(p.getResponse("age"), 1, 0.0001f);
            Assert.assertEquals(p.getResponse("gender"), 1, 0.0001f);
            Assert.assertEquals(p.getResponse("ethnicity"), 0, 0.0001f);
            Assert.assertEquals(p.getResponse("bmi"), 27.7f, 0.0001f);
            Assert.assertEquals(p.getId(), 3);

            p = participants.get(3);
            Assert.assertEquals(p.getResponse("age"), 2, 0.0001f);
            Assert.assertEquals(p.getResponse("gender"), 1, 0.0001f);
            Assert.assertEquals(p.getResponse("ethnicity"), 3, 0.0001f);
            Assert.assertEquals(p.getResponse("bmi"), 24.0f, 0.0001f);
            Assert.assertEquals(p.getId(), 4);

            p = participants.get(4);
            Assert.assertEquals(p.getResponse("age"), 3, 0.0001f);
            Assert.assertEquals(p.getResponse("gender"), 1, 0.0001f);
            Assert.assertEquals(p.getResponse("ethnicity"), 2, 0.0001f);
            Assert.assertEquals(p.getResponse("bmi"), 19.8f, 0.0001f);
            Assert.assertEquals(p.getId(), 5);

            p = participants.get(5);
            Assert.assertEquals(p.getResponse("age"), 0, 0.0001f);
            Assert.assertEquals(p.getResponse("gender"), 0, 0.0001f);
            Assert.assertEquals(p.getResponse("ethnicity"), 6, 0.0001f);
            Assert.assertEquals(p.getResponse("bmi"), 25.2f, 0.0001f);
            Assert.assertEquals(p.getId(), 6);

        } catch (ParticipantLoadException e) {
            logger.error(e.getMessage());
            Assert.assertTrue(false);
        }
    }

}
