package org.oi.scripting;

import org.graalvm.polyglot.*;

public class ScriptService {

    public String runSimpleScript(String name) {
        try (Context context = Context.create("js")) {
            String script = """
                function reply(name) {
                    return "Hello, " + name + "! This is JavaScript.";
                }
                reply("%s");
                """.formatted(name);

            Value result = context.eval("js", script);
            return result.asString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Script error: " + e.getMessage();
        }
    }
}
