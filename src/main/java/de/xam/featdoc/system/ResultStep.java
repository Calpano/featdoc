package de.xam.featdoc.system;

import javax.annotation.Nullable;

/**
*  @param scenarioStep  initially causing a chain of system reactions
 * @param depth             depth in tree from initial scenario step. 0 = is defined just like this in the scenario; > 0: how indirect the action is triggered
 * @param sourceSystem produced the message
 * @param targetSystem consumed the message
 * @param message           result of applying a rule or scenario step
 * @param messageComment    an optional comment on the message
 * @param rule defined in any system
 */
public record ResultStep(ScenarioStep scenarioStep, int depth, System sourceSystem, Message message,
                         String messageComment,
                         System targetSystem,

                         @Nullable Rule rule
) {


    public static ResultStep direct(ScenarioStep scenarioStep, int depth, System sourceSystem, Message message, String messageComment, System targetSystem) {
        return new ResultStep(scenarioStep, depth, sourceSystem, message, messageComment, targetSystem,null);
    }

    public static ResultStep indirect(ScenarioStep scenarioStep, int depth, System sourceSystem, Message message, String messageComment, System targetSystem,  Rule rule) {
        return new ResultStep(scenarioStep, depth, sourceSystem, message, messageComment, targetSystem, rule);
    }

    public @Nullable Feature feature() {
        return rule == null ? null : rule.feature();
    }

    public boolean isScenario() {
        return depth == 0;
    }


    @Override
    public String toString() {
        return String.format("%-10s --> %-10s : Msg=%-40s | Feat=%-20s | depth=%s | %s",
                sourceSystem().label,
                rule()==null? message.system().label :  rule.feature().system().label,
                message().system()+"."+message().name()+"--"+message().direction(),
                feature()==null ? "--" : feature().label(),
                depth(),
                messageComment()
        );
    }
}
