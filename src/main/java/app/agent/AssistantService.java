package app.agent;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.SystemMessage;

@AiService
public interface AssistantService {

    @SystemMessage("You are a helpful assistant capable of providing the current time.")
    String chat(String userMessage);
}