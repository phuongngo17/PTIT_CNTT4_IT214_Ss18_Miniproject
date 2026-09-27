package com.rikkeibank.identity;
import com.rikkeibank.common.api.GlobalExceptionHandler; import com.rikkeibank.common.security.JwtSecurityConfiguration; import org.springframework.boot.*; import org.springframework.boot.autoconfigure.SpringBootApplication; import org.springframework.context.annotation.Import;
@SpringBootApplication @Import({GlobalExceptionHandler.class, JwtSecurityConfiguration.class}) public class IdentityServiceApplication { public static void main(String[] a){SpringApplication.run(IdentityServiceApplication.class,a);} }

