package org.oi.scripting;

import java.util.Optional;

public class OiPromptDetector {

    private final ScriptService scriptService;

    public OiPromptDetector() {
        this.scriptService = new ScriptService();
    }

    public Optional<String> handleIfTriggered(String fullText, int caretPos) {
        String[] lines = fullText.split("\n");

        // Find current line based on caret position
        int charCount = 0;
        int currentLineIndex = 0;
        for (int i = 0; i < lines.length; i++) {
            charCount += lines[i].length() + 1;
            if (charCount >= caretPos) {
                currentLineIndex = i;
                break;
            }
        }

        // Search backwards to find "oi " line and make sure no --- END GPT --- between
        int oiStartIndex = -1;
        for (int i = currentLineIndex; i >= 0; i--) {
            if (lines[i].trim().equals("--- END GPT ---")) {
                break; // this block was already handled
            }
            if (lines[i].trim().startsWith("oi ")) {
                oiStartIndex = i;
                break;
            }
        }

        if (oiStartIndex == -1) {
            return Optional.empty(); // no new oi block found
        }

        // Build the full prompt block from oiStartIndex to currentLineIndex
        StringBuilder promptBuilder = new StringBuilder();
        for (int i = oiStartIndex; i <= currentLineIndex; i++) {
            if (i == oiStartIndex) {
                promptBuilder.append(lines[i].trim().substring(3)); // remove "oi "
            } else {
                promptBuilder.append("\n").append(lines[i]);
            }
        }

        String prompt = promptBuilder.toString();
        String response = scriptService.runSimpleScript(prompt);
        return Optional.of(response + "\n--- END GPT ---");
    }
}
