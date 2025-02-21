package io.playground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
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
