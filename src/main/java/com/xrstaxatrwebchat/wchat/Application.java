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
//        System.out.println("directories: ");
//        System.out.println(System.getProperty("user.dir"));
//        System.out.println(System.getProperty("project.dir"));
//        System.out.println(System.getProperty("basedir"));
//        System.out.println(System.getProperty("user.basedir"));
//        System.out.println(System.getProperty("user.dir"));
//
//
//        System.setProperty("user.dir", "/var/lib/tomcat8/webapps/xrstaxatrwebchat");
//        System.setProperty("project.basedir", "/var/lib/tomcat8/webapps/xrstaxatrwebchat");
//        System.setProperty("vaadin.frontend.generated.folder", "/var/lib/tomcat8/webapps/xrstaxatrwebchat/node_modules");
//        System.setProperty("vaadin.frontend.frontend.folder", "/var/lib/tomcat8/webapps/xrstaxatrwebchat/frontend");
//        System.setProperty("project.dir", "/var/lib/tomcat8/webapps/xrstaxatrwebchat");
//        System.setProperty("Dpreload.project.path", "/var/lib/tomcat8/webapps/xrstaxatrwebchat");
//
//
//        System.out.println(System.getProperty("user.dir"));
//        System.out.println(System.getProperty("project.dir"));
//        System.out.println(System.getProperty("Dpreload.project.path"));


        return  UnicastProcessor.create();
    }

    @Bean
    Flux<ChatMessage> messages(UnicastProcessor<ChatMessage> publisher)
    {
        return publisher.replay(30).autoConnect();
    }

}
