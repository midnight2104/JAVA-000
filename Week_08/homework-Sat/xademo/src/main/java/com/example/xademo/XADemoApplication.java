package com.example.xademo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
public class XADemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(XADemoApplication.class, args);
	}

}
