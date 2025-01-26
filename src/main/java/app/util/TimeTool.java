package app.util;

import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
public class TimeTool {

    @Tool
    public String getCurrentTime() {
        // Record the start time
        long start = System.nanoTime();
        System.out.println("Start: " + start);

        // Actual logic (print "Tool" and get the current time)
        System.out.println("Tool");
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Record the end time
        long end = System.nanoTime();
        System.out.println("End: " + end);

        // Calculate elapsed time in milliseconds
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(end - start);
        System.out.println("Time elapsed in getCurrentTime: " + elapsedMillis + " ms");

        // Return the current time string
        return currentTime;
    }

}
