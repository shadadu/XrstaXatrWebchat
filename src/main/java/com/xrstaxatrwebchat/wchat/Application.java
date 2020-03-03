package com.xrstaxatrwebchat.wchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {


    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder){
        return builder.sources(Application.class);
    }


    @Bean
    UnicastProcessor<ChatMessage> publisher()
    {
        return  UnicastProcessor.create();
    }

    @Bean
    Flux<ChatMessage> messages(UnicastProcessor<ChatMessage> publisher)
    {
        return publisher.replay(30).autoConnect();
    }

}
