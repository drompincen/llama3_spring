//JAVA 21+
//PREVIEW
//COMPILE_OPTIONS --add-modules=jdk.incubator.vector
//RUNTIME_OPTIONS --add-modules=jdk.incubator.vector -Djdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK=0

import app.model.Llama3;

import java.io.IOException;

public class Demo {
    public static void main(String[] args) throws IOException {
        Llama3.main(new String[]{
                "--model","C:\\Users\\drom\\llama3\\Llama-3.2-1B-Instruct-Q8_0.gguf",
                "--prompt","you are the best scrum master" +
                "and you have to plan a week" +
                "what would you do if you have to run daily scrums, start sprints and end sprints with retrospectives"
        });
    }
}
