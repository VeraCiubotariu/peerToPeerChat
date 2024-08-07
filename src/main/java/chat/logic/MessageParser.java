package chat.logic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageParser {
    public static boolean parseMessage(String message, Operation operation) {
        final String regex = operation.regex;
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            if (matcher.group(0) != null) {
                return true;
            }
        }
        return false;
    }
}
