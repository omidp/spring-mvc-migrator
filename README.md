## What is MVC migrator

This project helps you to refactor you spring MVC PathVariable and RequestParam and add explicit names to those annotations.

for example : 

From 

```
@GetMapping("/{userId}/comments/{id}")
public ResponseEntity<String> getUser(@PathVariable(required=false) Long userId, @PathVariable Long id) {
	 System.out.println(id);
}
```

To 

```
@GetMapping("/{userId}/comments/{id}")
public ResponseEntity<String> getUser(@PathVariable(required=false, value="userId") Long userId, @PathVariable("id") Long id) {
	System.out.println(id);
}
```

## How to build

- Clone and build the project 

```
mvn clean install
```

## How to use

- Add the open rewrite plugin to your maven pom file 

```
<plugin>
				<groupId>org.openrewrite.maven</groupId>
				<artifactId>rewrite-maven-plugin</artifactId>
				<version>5.15.4</version>
				<configuration>
					<activeRecipes>
						<recipe>com.omidp.app.ExplicitWebAnnotationNames</recipe>
					</activeRecipes>					
				</configuration>
				<dependencies>
					<!-- This module isn't packaged with OpenRewrite -->
					<dependency>
						<groupId>com.omidp.app</groupId>
						<artifactId>mvc-migrator</artifactId>
						<version>1.0-SNAPSHOT</version>
					</dependency>
				</dependencies>
</plugin>
```

- Execute the plugin 

```
mvn rewrite:run
```

Or 

```
mvn org.openrewrite.maven:rewrite-maven-plugin:run
```
