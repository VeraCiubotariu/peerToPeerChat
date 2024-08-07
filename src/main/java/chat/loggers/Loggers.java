package chat.loggers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loggers {
    public static final Logger errorLogger = LoggerFactory.getLogger("ErrorLogger");
    public static final Logger warningLogger = LoggerFactory.getLogger("WarnLogger");
    public static final Logger infoLogger = LoggerFactory.getLogger("InfoLogger");
    public static final Logger debugLogger = LoggerFactory.getLogger("DebugLogger");
    public static final Logger traceLogger = LoggerFactory.getLogger("TraceLogger");
}
