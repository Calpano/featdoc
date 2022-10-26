package de.xam.featdoc.system;

import javax.annotation.Nullable;

/**
 * @param feature
 * @param rule
 * @param message          result of applying a rule or scenario step
 * @param messageComment  an optional comment on the message
 * @param depth             0 = is defined just like this in the scenario; > 0: how indirect the action is triggered
 * @param causeFromScenario
 * @param source
 * @param target
 */
public record ResultStep(@Nullable Feature feature, @Nullable Rule rule, Message message, String messageComment, int depth,
                         ScenarioStep causeFromScenario, System source, System target) {

    public boolean isScenario() {
        return depth == 0;
    }

    @Override
    public String toString() {
        return String.format("%-10s --> %-10s : Msg=%-40s | Feat=%-20s | depth=%s",
                source().label,
                target().label,
                message().system()+"."+message().name()+"--"+message().direction(),
                feature()==null ? "--" : feature().label(),
                depth()
        );
    }
}
