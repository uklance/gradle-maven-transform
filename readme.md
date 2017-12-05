# gradle-maven-transform [![Build Status](https://travis-ci.org/uklance/gradle-maven-transform.svg?branch=master)](https://travis-ci.org/uklance/gradle-maven-transform)

## What does this Gradle plugin do?

This plugin allows you to transform Maven `pom.xml` files during your build. A typical use case for this is when you are trying to migrate your build from Maven to Gradle and you want both builds to work during the migration. You can generate Gradle scripts which contain the Maven dependencies from the `pom.xml` in a format that can be [applied](https://docs.gradle.org/current/javadoc/org/gradle/api/plugins/PluginAware.html#apply(java.util.Map)) in a Gradle build. Once you are happy with the Gradle build you can copy paste the generated Gradle scripts into your Gradle build and ditch Maven for ever!

## How does it work?

The `gradle-maven-transform` plugin uses the [Maven ModelBuilder API](https://maven.apache.org/ref/3.5.2/maven-model-builder/apidocs/org/apache/maven/model/building/ModelBuilder.html) to read `pom.xml` files and generate an [Effective POM](http://maven.apache.org/plugins/maven-help-plugin/usage.html#The_help:effective-pom_Goal) for each which are then passed to the [Transformers](https://github.com/uklance/gradle-maven-transform/blob/master/plugin/src/main/groovy/com/lazan/maven/transform/Transformer.java) specified in the Gradle build. The `Effective POM` is a "denormalized" view of the model with property substitutions applied and any versions inherited from parent POMs or BOMs are explicitly declared in each [Model](https://maven.apache.org/ref/3.5.2/maven-model/apidocs/org/apache/maven/model/Model.html).

## Is there an example?

Sure, there's a sample multi-module Maven build [here](https://github.com/uklance/gradle-maven-transform/tree/master/example/maven-root). The `pom.xml` files from the Maven build are transformed by the [example-transform](https://github.com/uklance/gradle-maven-transform/tree/master/example/example-transform) project. The generated Gradle scripts are applied in the `build.gradle` [here](https://github.com/uklance/gradle-maven-transform/blob/master/example/maven-root/build.gradle). So it's possible to build [maven-root](https://github.com/uklance/gradle-maven-transform/tree/master/example/maven-root) with both Maven and Gradle.

## What Transformers are available?

There's out-of-the-box support for [Freemarker](https://freemarker.apache.org/) transformations which should suit most use cases. Users can implement their own custom transformations by implementing [Transformer](https://github.com/uklance/gradle-maven-transform/blob/master/plugin/src/main/groovy/com/lazan/maven/transform/Transformer.java)

## Transform context

Each [Transformer](https://github.com/uklance/gradle-maven-transform/blob/master/plugin/src/main/groovy/com/lazan/maven/transform/Transformer.java) is passed a `Map<String, Object>` context. There will be objects in the context map by default but custom context objects can be added and the defaults can be overridden.

## Projects Transform

Confguring a `projectsTransform { ... }` will generate a single file for all the `pom.xml` files configured. Typically you would use this transformation to perform aggregate functionality. Eg: to generate a script common to all projects which will be applied in the root `build.gradle` of a multi-module build. By default the transform context map will contain the following:

| Name             | Type         |
|------------------|--------------|
| projectsContext   | [ProjectsContext](https://github.com/uklance/gradle-maven-transform/blob/master/plugin/src/main/groovy/com/lazan/maven/transform/ProjectsContext.java)|
| dependencyAggregator | [DependencyAggregator](https://github.com/uklance/gradle-maven-transform/blob/master/plugin/src/main/groovy/com/lazan/maven/transform/DependencyAggregator.java)


## Project Transform

Configuring a `projectTransform { ... }` will generate a separate file for each of the `pom.xml` files configured. An example use case for this is to generate a gradle script per project, each containing the dependencies for a single project. Each generated script would be applied by a single Gradle project.

| Name             | Type         |
|------------------|--------------|
| projectContext   | [ProjectContext](https://github.com/uklance/gradle-maven-transform/blob/master/plugin/src/main/groovy/com/lazan/maven/transform/ProjectContext.java)|
| projectsContext   | [ProjectsContext](https://github.com/uklance/gradle-maven-transform/blob/master/plugin/src/main/groovy/com/lazan/maven/transform/ProjectsContext.java)|
| dependencyAggregator | [DependencyAggregator](https://github.com/uklance/gradle-maven-transform/blob/master/plugin/src/main/groovy/com/lazan/maven/transform/DependencyAggregator.java)

### Sample usage

```groovy
plugins {
   id "com.lazan.maven-transform" version "0.3"
}
mavenTransform {
   pomXmls 'path1/pom.xml', 'path2/pom.xml', 'path3/pom.xml'
   outputDirectory "$buildDir/mavenTransform"
   transformClasspath files('src/main/freemarker')
   
   projectsTransform {
      // produce a single output file for all pom.xmls
      outputPath 'aggregate.gradle'
      freemarkerTransform 'aggregate.ftl'
      context 'customContext', { projectsContext ->
         return new MyCustomProjectsContext(projectsContext)
      }
   }

   projectTransform {
      // produce an output file for each pom.xml
      outputPath { context -> "${context.artifactId}.gradle" }
      freemarkerTransform 'project-dependencies.ftl'
      context 'customContext', { projectContext ->
         return new MyCustomProjectContext(projectContext)
      }
   }
}
```
