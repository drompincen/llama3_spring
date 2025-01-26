package app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.env.Environment;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SwaggerConfig implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerConfig.class);

    @Autowired
    private Environment environment;

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String serverPort = String.valueOf(webServerAppCtxt.getWebServer().getPort());
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        String swaggerPath = environment.getProperty("springdoc.swagger-ui.path", "/swagger-ui/index.html");

        String swaggerUrl = String.format("http://localhost:%s%s%s", serverPort, contextPath, swaggerPath);
        logger.info("Swagger UI is available at: {}", swaggerUrl);
    }
}
