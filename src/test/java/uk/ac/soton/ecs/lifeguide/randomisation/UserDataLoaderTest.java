package uk.ac.soton.ecs.lifeguide.randomisation;

import uk.ac.soton.ecs.lifeguide.randomisation.exception.InvalidUserDataException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Aleksandar Botev (ab9g10@ecs.soton.ac.uk)
 * @author Dinosion Perez-Mavrogenis (dpm3g10@ecs.soton.ac.uk)
 * @author Liam de Valmency (lpdv1g10@ecs.soton.ac.uk)
 * @author Kim Svensson (ks6g10@ecs.soton.ac.uk)
 * @since 1.7
 */

public class UserDataLoaderTest {

    private static final Logger logger = LoggerFactory.getLogger(UserDataLoaderTest.class);

    @Test
    public void testUserDataLoading() {
        String userData = "<data>"
                + "		<value baseType='string' identifier='username'>alowndes</value>"
                + "		<value baseType='integer' identifier='session1score'>50</value>"
                + "		<value baseType='integer' identifier='session1overall'>51</value>"
                + "		<value identifier='session1float' baseType='float'>5.0</value>"
                + "</data>";

        try {
            Participant participant = UserDataLoader.loadParticipant(userData);

            // Strings aren't stored in the response map.
            Assert.assertEquals(participant.getResponses().size(), 3);
            Assert.assertEquals(participant.getResponse("session1score"), 50.0f, 0.0001f);
            Assert.assertEquals(participant.getResponse("session1overall"), 51.0f, 0.0001f);
            Assert.assertEquals(participant.getResponse("session1float"), 5.0f, 0.0001f);

        } catch (InvalidUserDataException e) {
            logger.debug(e.getMessage());
        }
    }

}
