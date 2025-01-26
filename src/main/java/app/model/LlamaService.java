package app.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class LlamaService {
    // Path to the model file
    public static final String MODEL = "C:\\Users\\drom\\llama3\\Llama-3.2-1B-Instruct-Q8_0.gguf";

    private Llama model;

    // Constructor to load the model
    public LlamaService(String modelPath) throws IOException {
        // Check if the file exists
        if (!Files.exists(Path.of(modelPath))) {
            throw new IOException("Model file not found at: " + modelPath);
        }

        // Load the model
        this.model = ModelLoader.loadModel(Path.of(modelPath), 2048, true); // Default max tokens: 2048
    }

    // Method to generate a response and track time
    public String generateResponse(String prompt, int maxTokens, float temperature) {

        // Start timing
        long startTime = System.nanoTime();

        // Create a sampler with specified temperature
        Sampler sampler = Llama3.selectSampler(
                model.configuration().vocabularySize,
                temperature,
                0.95f, // Top-p sampling
                System.nanoTime()
        );

        // Tokenize the input prompt
        List<Integer> promptTokens = model.tokenizer().encodeAsList(prompt);

        // Generate response tokens
        List<Integer> tokens = Llama.generateTokens(
                model,
                model.createNewState(16), // Batch size
                0,
                promptTokens, // Prompt tokens
                Set.of(), // Stop tokens
                maxTokens, // Maximum response tokens
                sampler,
                false, // Echo mode
                null // No token callback
        );

        // Decode the tokens to get the response
        String response = model.tokenizer().decode(tokens);

        // End timing
        long endTime = System.nanoTime();
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        // Print or log the duration
        System.out.println("Time taken to generate response: " + durationMillis + " ms");

        return response;
    }

    // Main method for quick testing
    public static void main(String[] args) {
        try {
            // Initialize the model with the path
            LlamaService llamaService = new LlamaService(MODEL);

            // Define the input prompt
            String prompt = "Tell me a joke";

            // Generate a response with the desired parameters
            int maxTokens = 100;       // Maximum number of tokens
            float temperature = 0.7f;  // Creativity level
            String response = llamaService.generateResponse(prompt, maxTokens, temperature);

            // Print the response
            System.out.println("Llama's Response: " + response);

        } catch (IOException e) {
            // Handle exceptions related to loading the model or file paths
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            // Handle unexpected errors
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
