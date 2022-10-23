package de.xam.featdoc.system;

import org.jetbrains.annotations.Nullable;

public interface ScenarioApi {

    default Scenario asyncEvent(System source, System target, Message message) {
        return asyncEvent(source, target, message, null);
    }

    default Scenario asyncEvent(System source, System target, Message message, String comment) {
        if (!message.isAsynchronous())
            throw new IllegalStateException("asyncEvent must use an asynchronous event, not " + message);
        return step(source, target, message, comment);
    }

    default Scenario asyncEvent(System source, System target, String event) {
        return step(source, target, new Message(target, Timing.Asynchronous, event), null);
    }

    Scenario step(System source, System target, Message message, @Nullable String stepComment);

    default Scenario syncCall(System source, System target, Message message) {
        return syncCall(source, target, message, null);
    }

    default Scenario syncCall(System source, System target, Message message, String comment) {
        if (!message.isSynchronous())
            throw new IllegalStateException("syncCall must use a synchronous event, not " + message);
        return step(source, target, message, comment);
    }

    default Scenario syncCall(System source, System target, String callMessage) {
        return step(source, target, new Message(target, Timing.Synchronous, callMessage), null);
    }
}
