package jobs;

import models.*;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.MorphiaFixtures;
import utils.MongoDbUtils;

/**
 * Bootstrap class which is executed at the very beginning
 * when the application is started.
 *
 * @author Niko Schmuck
 * @since 29.01.2012
 */
@OnApplicationStart
public class Bootstrap extends Job {

    public void doJob() {
        Logger.info("Starting cmskern bootstrap job (Play env: %s)...", Play.id);
        Logger.info("     ... using database '%s' on MongoDB: %s", MongoDbUtils.getDBName(), MongoDbUtils.getDBServers());

        if (Role.count() == 0L && User.count() == 0L) {
            Logger.info("Importing initial roles and users ...");
            MorphiaFixtures.loadModels("initial-users_and_roles.yml");
        }
        if (Asset.count() == 0L) {
            Logger.info("Importing initial assets ...");
            MorphiaFixtures.loadModels("initial-assets.yml");
        }
        if (ContentType.count() == 0L) {
            Logger.info("Importing initial content types ...");
            MorphiaFixtures.loadModels("initial-contenttypes.yml");
        }
        if (ContentNode.count() == 0L) {
            Logger.info("Importing initial content nodes ...");
            MorphiaFixtures.loadModels("initial-content.yml");
        }

        // ~~
        Logger.info("Ensure MongoDB indexes are set ...");
        ContentNode.createIndexes();
    }

}
