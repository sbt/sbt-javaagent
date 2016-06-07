package maxwell;

import java.lang.instrument.Instrumentation;
import java.util.regex.Pattern;

public class Maxwell {
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        String[] output = new String[] { "Agent 86" };
        if (agentArgs != null) {
            output = agentArgs.split(Pattern.quote(";"));
        }
        for (String string: output)
            System.out.println(string);
    }
}
