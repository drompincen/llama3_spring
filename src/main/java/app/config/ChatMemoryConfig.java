package app.config;
import app.model.LlamaLanguageModelWrapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

@Configuration
public class ChatMemoryConfig {

    @Bean
    public ChatMemory chatMemory() {
        // Configure a chat memory with a window of the last 100 messages
        return MessageWindowChatMemory.withMaxMessages(100);
    }

}
