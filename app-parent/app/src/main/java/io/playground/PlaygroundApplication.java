package io.playground;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication

public class PlaygroundApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlaygroundApplication.class, args);
    }

//    	@Bean
//    	public ApplicationRunner applicationRunner(ApplicationContext ctx) {
//    		return args -> {
//    			System.out.println("Let's inspect the beans provided by Spring Boot:");
//
//    			String[] beanNames = ctx.getBeanDefinitionNames();
//    			Arrays.sort(beanNames);
//    			for (String beanName : beanNames) {
//    				System.out.println(beanName);
//    			}
//    		};
//    	}
}
