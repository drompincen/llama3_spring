package app.controller;

import app.agent.AssistantService;
import org.springframework.web.bind.annotation.*;
import dev.langchain4j.model.output.Response;
import app.model.LlamaLanguageModelWrapper;

@RestController
@RequestMapping("/api/llama")
public class LlamaController {

    private final LlamaLanguageModelWrapper llamaLanguageModel;
    private final AssistantService assistantService;

    public LlamaController(LlamaLanguageModelWrapper llamaLanguageModel, AssistantService assistantService) {
        this.llamaLanguageModel = llamaLanguageModel;
        this.assistantService = assistantService;
    }

    @GetMapping("/generate")
    public String generate(@RequestParam String prompt) {
        // Use the app.model.LlamaLanguageModel to generate a response
        String response = assistantService.chat(prompt);
        System.out.println("Response is : "+response);
        // Return the generated text
        return response;
    }
    @GetMapping("/time")
    public String getTime(@RequestParam String prompt) {
        return assistantService.chat(prompt);
    }
}
