package org.oi.scripting;

import java.util.Optional;

public class OiPromptDetector {

    private final ScriptService scriptService;

    public OiPromptDetector() {
        this.scriptService = new ScriptService();
    }

    public Optional<ResponseBlock> handleIfTriggered(String fullText, int caretPos) {
        String[] lines = fullText.split("\n");

        // Determine current line index
        int charCount = 0;
        int currentLineIndex = 0;
        for (int i = 0; i < lines.length; i++) {
            charCount += lines[i].length() + 1;
            if (charCount >= caretPos) {
                currentLineIndex = i;
                break;
            }
        }

        // Search backward for most recent unhandled "oi " line
        int oiStartIndex = -1;
        for (int i = currentLineIndex; i >= 0; i--) {
            if (lines[i].trim().startsWith("me:") || lines[i].trim().startsWith("GPT:")) {
                break; // already handled
            }
            if (lines[i].trim().startsWith("oi ")) {
                oiStartIndex = i;
                break;
            }
        }

        if (oiStartIndex == -1) return Optional.empty(); // no valid block

        // Build the prompt from that line to current line
        StringBuilder promptBuilder = new StringBuilder();
        for (int i = oiStartIndex; i <= currentLineIndex; i++) {
            String line = lines[i];
            if (i == oiStartIndex) {
                promptBuilder.append(line.trim().substring(3)); // skip "oi "
            } else {
                promptBuilder.append("\n").append(line);
            }
        }

        String prompt = promptBuilder.toString();
        String response = scriptService.runSimpleScript(prompt);

        return Optional.of(new ResponseBlock(oiStartIndex, response));
    }

    public static class ResponseBlock {
        public final int lineToReplace;
        public final String gptResponse;

        public ResponseBlock(int lineToReplace, String gptResponse) {
            this.lineToReplace = lineToReplace;
            this.gptResponse = gptResponse;
        }
    }
}
