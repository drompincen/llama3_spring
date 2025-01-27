package app.model;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
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

    // Inject your tool if you want to call it programmatically
    private final TimeTool timeTool;

    @Autowired
    public LlamaChatLanguageModelWrapper(ChatMemory chatMemory, TimeTool timeTool) throws IOException {
        // Reuse your LlamaService with whichever model you want
        this.llamaService = new LlamaService(LlamaService.MODEL);
        this.chatMemory = chatMemory;
        this.timeTool = timeTool;
    }

    /**
     * Minimal method required by ChatLanguageModel:
     * Generate a response given a list of ChatMessage objects (no tools).
     */
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        // Add all incoming messages to memory
        for (ChatMessage msg : messages) {
            chatMemory.add(msg);
        }

        // Check if the user asked for "time"
        String userLastMessage = extractLastUserMessage(messages);
        if (userLastMessage != null && userLastMessage.toLowerCase().contains("time")) {
            // Force-call the TimeTool
            String currentTime = timeTool.getCurrentTime();

            // Put the tool’s response into memory so Llama sees it
            // We can add it as a SystemMessage or just an AiMessage.
            // SystemMessage is often used for context.
            chatMemory.add(new SystemMessage("Tool used: TimeTool => " + currentTime));
        }

        // Build the final prompt from memory
        String prompt = buildPromptFromMemory();

        // Generate response
        String generatedText = llamaService.generateResponse(prompt, 100, 0.7f);
        AiMessage aiMessage = new AiMessage(generatedText);

        // Save AI response in memory
        chatMemory.add(aiMessage);

        // Calculate token usage (dummy example)
        int inputTokens = prompt.split("\\s+").length;
        int outputTokens = generatedText.split("\\s+").length;
        TokenUsage tokenUsage = new TokenUsage(inputTokens, outputTokens, inputTokens + outputTokens);

        // Return final
        return new Response<>(aiMessage, tokenUsage, FinishReason.STOP);
    }

    /**
     * When multiple tools are provided.
     * You can choose how to interpret them. This is a trivial example.
     */
    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, List<ToolSpecification> toolSpecifications) {
        if (toolSpecifications == null || toolSpecifications.isEmpty()) {
            // No tools to consider => fallback to no-tool generate
            return generate(messages);
        }

        // For demonstration, we’ll just check if the user is asking for the time
        // If so, we call the timeTool and incorporate that into the final answer.
        String userLastMessage = extractLastUserMessage(messages);
        if (userLastMessage != null && userLastMessage.toLowerCase().contains("time")) {
            String currentTime = timeTool.getCurrentTime();
            // You can decide how the AI might incorporate the tool response
            // e.g. “The current time is X” or you can pass it into Llama as additional context
            userLastMessage += "\nUser wants the time: " + currentTime;
        }

        // Optionally, build a new prompt that includes the above “time” snippet
        // Or just pass the user messages as usual
        return generate(messages);
    }

    /**
     * When exactly one tool is "required".
     * Usually you’d do something more advanced, like pass usage instructions to Llama.
     * This example simply demonstrates how you *could* call that single tool if appropriate.
     */
    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, ToolSpecification toolSpecification) {
        // This default interface method *requires* the single tool, so let's interpret that:
        // We'll do the same check as above
        if (toolSpecification == null) {
            throw new UnsupportedFeatureException("No tool passed, but toolChoice=REQUIRED was used");
        }

        // Potentially check if toolSpecification corresponds to TimeTool
        // For now, just check user text
        String userLastMessage = extractLastUserMessage(messages);
        if (userLastMessage != null && userLastMessage.toLowerCase().contains("time")) {
            String currentTime = timeTool.getCurrentTime();
            userLastMessage += "\nUser wants the time: " + currentTime;
        }

        return generate(messages);
    }

    // ----------------------------------------------------------------------------
    // Optional: utility to build prompt from chatMemory
    // ----------------------------------------------------------------------------
    private String buildPromptFromMemory() {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage message : chatMemory.messages()) {

            if (message instanceof UserMessage) {
                sb.append("User: ").append(message.toString()).append("\n");

            } else if (message instanceof AiMessage) {
                sb.append("Assistant: ").append(message.toString()).append("\n");

            } else if (message instanceof SystemMessage) {
                sb.append("System: ").append(message.toString()).append("\n");

            } else {
                // For any other custom type, just prepend the role
                sb.append(message).append(": ").append(message.toString()).append("\n");
            }
        }
        return sb.toString();
    }


    private String extractLastUserMessage(List<ChatMessage> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (msg instanceof UserMessage) {
                // Return the "toString()" of the user message once
                return msg.toString();
            }
        }
        return null;
    }


}
