package example;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers.CancelandStopIntentHandler;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers.FindBookIntentHandler;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers.HelpIntentHandler;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers.SessionEndedRequestHandler;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers.LaunchRequestHandler;
import com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers.FallbackIntentHandler;

/**
 * This skill is the entry point the skill
 */
public class Hello extends SkillStreamHandler {

    private static Skill getSkill() {
        return Skills.standard()
                .addRequestHandlers(
                        // Handlers that are in com.amazon.audiblecambridgehshelloworldalexaskill.helloworld.handlers
                        // most of these are boilerplate.  The big one we care about is HelloWorldIntentHandler
                        new FindBookIntentHandler(),
                        new CancelandStopIntentHandler(),
                        new HelpIntentHandler(),
                        new LaunchRequestHandler(),
                        new SessionEndedRequestHandler(),
                        new FallbackIntentHandler())
                // Add your skill id below if you want? Not sure why.
                //.withSkillId("")
                .build();
    }

    /**
     * Luckily a lot of this logic is written for us in a class called SkillStreamHandler
     * We just extend it (https://www.tutorialspoint.com/java/java_inheritance.htm)
     */
    public Hello() {
        super(getSkill());
    }

}