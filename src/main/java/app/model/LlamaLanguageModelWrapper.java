package app.model;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.model.output.FinishReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class LlamaLanguageModelWrapper implements LanguageModel {

    protected final LlamaService llamaService;
    protected final ChatMemory chatMemory;

    @Autowired
    public LlamaLanguageModelWrapper(ChatMemory chatMemory) throws IOException {
        this.llamaService = new LlamaService(LlamaService.MODEL);
        this.chatMemory = chatMemory;
    }

    @Override
    public Response<String> generate(String text) {
        // Retrieve conversation history from chatMemory
        List<ChatMessage> history = chatMemory.messages();

        // Optionally, format the history into a single string if needed
        StringBuilder historyBuilder = new StringBuilder();
        for (ChatMessage message : history) {
            historyBuilder.append(message.toString()).append(": ").append(message).append("\n");
        }
        String conversationHistory = historyBuilder.toString();

        // Incorporate the conversation history into the prompt
        String prompt = conversationHistory + "User: " + text;

        // Generate a response using the LlamaService
        String generatedText = llamaService.generateResponse(prompt, 100, 0.7f);

        // Update chatMemory with the new user input and model response
        chatMemory.add(new UserMessage(text));
        chatMemory.add(new AiMessage(generatedText));

        // Calculate token usage (example implementation)
        int inputTokens = text.split("\\s+").length;
        int outputTokens = generatedText.split("\\s+").length;
        TokenUsage tokenUsage = new TokenUsage(inputTokens, outputTokens, inputTokens + outputTokens);

        // Wrap the generated text in a Response object with metadata
        return new Response<>(generatedText, tokenUsage, FinishReason.STOP);
    }
}
