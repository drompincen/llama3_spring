package app.model;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.exception.UnsupportedFeatureException;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import app.util.TimeTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class LlamaChatLanguageModelWrapper implements ChatLanguageModel {

    private final ChatMemory chatMemory;
    private final LlamaService llamaService;
    private final TimeTool timeTool;

    @Autowired
    public LlamaChatLanguageModelWrapper(ChatMemory chatMemory, TimeTool timeTool) throws IOException {
        this.llamaService = new LlamaService(LlamaService.MODEL);
        this.chatMemory = chatMemory;
        this.timeTool = timeTool;
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {

        // ----------------------------------------------------------
        // 1) SINGLE-TURN usage: clear old memory each time
        // ----------------------------------------------------------
        chatMemory.clear();

        // 2) Add only the incoming messages
        for (ChatMessage msg : messages) {
            chatMemory.add(msg);
        }

        // If last user message mentions "time", add a SystemMessage with current time
        String lastUserMessage = extractLastUserMessage(messages);
        if (lastUserMessage != null && lastUserMessage.toLowerCase().contains("time")) {
            String currentTime = timeTool.getCurrentTime();
            chatMemory.add(new SystemMessage("Tool used: TimeTool => " + currentTime));
        }

        // ----------------------------------------------------------
        // 3) Build the final prompt from memory & print for debugging
        // ----------------------------------------------------------
        String prompt = buildPromptFromMemory();
        System.out.println("******** PROMPT ********\n" + prompt + "\n************************");

        // ----------------------------------------------------------
        // 4) Generate Llama response
        // ----------------------------------------------------------
        String rawResponse = llamaService.generateResponse(prompt, 100, 0.7f);

        // Optionally strip out those special tokens if you like
        String cleanedResponse = removeSpecialTokens(rawResponse);

        // Print the raw or cleaned response for debugging
        System.out.println("******** RAW RESPONSE ********\n" + rawResponse + "\n******************************");
        System.out.println("******** CLEANED RESPONSE ********\n" + cleanedResponse + "\n*********************************");

        // Construct an AiMessage from the cleaned text
        AiMessage aiMessage = new AiMessage(cleanedResponse);

        // ----------------------------------------------------------
        // 5) Decide if you want to store the AI response in memory
        //    For single-turn usage, you can skip storing it.
        // ----------------------------------------------------------
        // chatMemory.add(aiMessage); // Omit for single-turn usage

        // ----------------------------------------------------------
        // 6) Token usage (just a rough example)
        // ----------------------------------------------------------
        int inputTokens = prompt.split("\\s+").length;
        int outputTokens = cleanedResponse.split("\\s+").length;
        TokenUsage tokenUsage = new TokenUsage(inputTokens, outputTokens, inputTokens + outputTokens);

        // Return the final
        return new Response<>(aiMessage, tokenUsage, FinishReason.STOP);
    }

    // If you handle multiple tools
    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, List<ToolSpecification> toolSpecifications) {
        if (toolSpecifications == null || toolSpecifications.isEmpty()) {
            return generate(messages);
        }
        return generate(messages);
    }

    // If exactly one tool is required
    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, ToolSpecification toolSpecification) {
        if (toolSpecification == null) {
            throw new UnsupportedFeatureException("No tool passed, but toolChoice=REQUIRED was used");
        }
        return generate(messages);
    }

    // ----------------------------------------------------------------
    // Helper: builds a single prompt string from the memory
    // ----------------------------------------------------------------
    private String buildPromptFromMemory() {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage msg : chatMemory.messages()) {
            String content = extractMessageContent(msg);

            if (msg instanceof UserMessage) {
                sb.append("User: ").append(content).append("\n");
            } else if (msg instanceof AiMessage) {
                sb.append("Assistant: ").append(content).append("\n");
            } else if (msg instanceof SystemMessage) {
                sb.append("System: ").append(content).append("\n");
            } else {
                sb.append(msg.type()).append(": ").append(content).append("\n");
            }
        }
        return sb.toString();
    }

    // ----------------------------------------------------------------
    // Helper: get last user message
    // ----------------------------------------------------------------
    private String extractLastUserMessage(List<ChatMessage> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (msg instanceof UserMessage) {
                return extractMessageContent(msg);
            }
        }
        return null;
    }

    // ----------------------------------------------------------------
    // Helper: get actual text from each message
    // ----------------------------------------------------------------
    private String extractMessageContent(ChatMessage msg) {
        if (msg instanceof UserMessage userMsg) {
            return userMsg.text();
        } else if (msg instanceof AiMessage aiMsg) {
            return aiMsg.text();
        } else if (msg instanceof SystemMessage sysMsg) {
            return sysMsg.text();
        }
        return "[Unknown message type: " + msg.type() + "]";
    }

    // ----------------------------------------------------------------
    // Helper: remove special <|start_header_id|> tokens, etc.
    // ----------------------------------------------------------------
    private String removeSpecialTokens(String input) {
        // Remove anything in form <|something|>
        // Adjust the regex if you want to be more selective
        return input.replaceAll("<\\|.*?\\|>", "");
    }
}
