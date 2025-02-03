package io.playground.exception;

import static java.text.MessageFormat.format;

public class NotFoundException extends BusinessException {

    private static final String namedAndIdentifiedMessageTemplate = "{0}({1}) not found";

    private NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String message){
        return new NotFoundException(message);
    }

    public static NotFoundException of(String elementName, Object id){
        return new NotFoundException(format(namedAndIdentifiedMessageTemplate, elementName, String.valueOf(id)));
    }
}
