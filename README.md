## Gradle Configuration Roles

This repo is the companion to the blog post published at 
https://dev.to/autonomousapps/configuration-roles-and-the-blogging-industrial-complex-21mn

The most interesting code is in the [build-logic](https://github.com/autonomousapps/blog-configuration-roles/tree/main/build-logic/configuration-roles/src/main/kotlin/mutual/aid) 
project, particularly in [ConfigurationRolesPlugin](https://github.com/autonomousapps/blog-configuration-roles/blob/main/build-logic/configuration-roles/src/main/kotlin/mutual/aid/ConfigurationRolesPlugin.kt).

If you want to give it a spin, clone the repo and then run the following:

```bash
./gradlew :aggregator:printDependencySources
```

That will print a list of source files from the two "feature" modules in this repo (`:feature-1` and 
`:feature-2`).
