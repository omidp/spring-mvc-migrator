package com.omidp.app;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class ExplicitWebAnnotationNamesTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new ExplicitWebAnnotationNames())
			.parser(JavaParser.fromJavaVersion().classpath("spring-web"));
	}

	@Test
	void transformTest() {
		rewriteRun(
			java(
				"""
						 import org.springframework.http.ResponseEntity;
					                                import org.springframework.web.bind.annotation.*;
					                               
					                                @RestController
					                                @RequestMapping("/users")
					                                public class UsersController {
					                                    @GetMapping("/{userId}/comments/{id}")
					                                    public ResponseEntity<String> getUser(@PathVariable(required=false) Long userId, @PathVariable Long id) {
					                                        System.out.println(id);
					                                    }
					                                    
					                                    @GetMapping("/comments")
					                                    public ResponseEntity<String> getUser(@RequestParam(required=false) Set<String> userIds, @RequestParam Long id) {
					                                        System.out.println(id);
					                                    }
					                                    
					                                }
					""",
				"""
						 import org.springframework.http.ResponseEntity;
					                                import org.springframework.web.bind.annotation.*;
					                               
					                                @RestController
					                                @RequestMapping("/users")
					                                public class UsersController {
					                                    @GetMapping("/{userId}/comments/{id}")
					                                    public ResponseEntity<String> getUser(@PathVariable(required=false, value="userId") Long userId, @PathVariable("id") Long id) {
					                                        System.out.println(id);
					                                    }
					                                    
					                                    @GetMapping("/comments")
					                                    public ResponseEntity<String> getUser(@RequestParam(required=false, value="userIds") Set<String> userIds, @RequestParam("id") Long id) {
					                                        System.out.println(id);
					                                    }
					                                    
					                                }
					"""
			)
		);
	}

}
