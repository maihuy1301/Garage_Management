package com.garage.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.io.IOException;

@Configuration
@EnableAsync
public class FirebasePushConfig {
    @Bean(destroyMethod = "delete")
    @ConditionalOnProperty(name = "garage.push.enabled", havingValue = "true")
    FirebaseApp garageFirebaseApp(@Value("${garage.push.project-id}") String projectId) throws IOException {
        return FirebaseApp.initializeApp(FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setProjectId(projectId).build(), "garage-push");
    }

    @Bean
    @ConditionalOnProperty(name = "garage.push.enabled", havingValue = "true")
    FirebaseMessaging garageFirebaseMessaging(FirebaseApp garageFirebaseApp) {
        return FirebaseMessaging.getInstance(garageFirebaseApp);
    }

    @Bean(name = "pushExecutor")
    ThreadPoolTaskExecutor pushExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("garage-push-");
        return executor;
    }
}
