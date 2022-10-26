package de.xam.featdoc.system;

public interface SystemApi {

    Message step(Message.Direction direction, Timing timing, String name);

    default Message apiCall(String name) {
        return step(Message.Kind.API_CALL, name);
    }

    default Message asyncEventOutgoing(String name) {
        return step(Message.Kind.EVENT_OUTGOING, name);
    }

    default Message asyncEventIncoming(String name) {
        return step(Message.Kind.EVENT_INCOMING, name);
    }

    default Message step(Message.Kind kind, String name) {
        return step(kind.direction(), kind.timing(), name);
    }

    default Message uiInput(String name) {
        return step(Message.Kind.UI_INPUT, name);
    }

    default Message uiOutput(String name) {
        return step(Message.Kind.UI_INPUT, name);
    }

    default Message webHook(String name) {
        return step(Message.Kind.WEB_HOOK, name);
    }

}
