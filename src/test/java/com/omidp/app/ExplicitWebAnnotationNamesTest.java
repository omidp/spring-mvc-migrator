/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
					                                    public ResponseEntity<String> getUserComments(@RequestParam(required=false) Set<String> userIds, @RequestParam Long id) {
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
					                                    public ResponseEntity<String> getUserComments(@RequestParam(required=false, value="userIds") Set<String> userIds, @RequestParam("id") Long id) {
					                                        System.out.println(id);
					                                    }
					                                    
					                                }
					"""
			)
		);
	}

}
