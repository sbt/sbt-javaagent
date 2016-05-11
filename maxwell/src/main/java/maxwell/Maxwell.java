package maxwell;

import java.lang.instrument.Instrumentation;

public class Maxwell {
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("Agent 86");
    }
}
