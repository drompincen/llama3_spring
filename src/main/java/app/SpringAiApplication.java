package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import app.model.LlamaLanguageModelWrapper;

import java.io.IOException;

@SpringBootApplication
public class SpringAiApplication {

//    @Bean
//    public LlamaLanguageModelWrapper llamaLanguageModel() throws IOException {
//        // Change the path if needed or make it configurable
//        return new LlamaLanguageModelWrapper();
//    }

    public static void main(String[] args) {
        SpringApplication.run(SpringAiApplication.class, args);
    }
}
