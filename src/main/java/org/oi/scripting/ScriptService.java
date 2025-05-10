package org.oi.scripting;

import org.graalvm.polyglot.*;

public class ScriptService {

    public String runSimpleScript(String prompt) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            return "[ERROR] Missing OPENAI_API_KEY environment variable.";
        }

        String js = """
            const HttpClient = Java.type('java.net.http.HttpClient');
            const HttpRequest = Java.type('java.net.http.HttpRequest');
            const HttpResponse = Java.type('java.net.http.HttpResponse');
            const URI = Java.type('java.net.URI');
            const BodyPublishers = Java.type('java.net.http.HttpRequest.BodyPublishers');

            const apiKey = Java.type("java.lang.System").getenv("OPENAI_API_KEY");
            const userInput = prompt;

            const requestBody = JSON.stringify({
                model: "gpt-3.5-turbo",
                messages: [
                    { role: "system", content: "You are a helpful assistant." },
                    { role: "user", content: userInput }
                ]
            });

            const client = HttpClient.newHttpClient();
            const request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

                const response = client.send(request, HttpResponse.BodyHandlers.ofString());
                const json = JSON.parse(response.body());
            
                if (!json.choices || !json.choices[0]) {
                    if (json.error && json.error.message) {
                        throw new Error("OpenAI API error: " + json.error.message);
                    } else {
                        throw new Error("Unexpected response: " + response.body());
                    }
                }
            
            json.choices[0].message.content;
           
            """;

        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.ecmascript-version", "2022")
                .build()) {

            context.getBindings("js").putMember("prompt", prompt);
            Value result = context.eval("js", js);
            return result.asString();

        } catch (Exception e) {
            e.printStackTrace();
            return "[ERROR] GPT call failed: " + e.getMessage();
        }
    }
}
