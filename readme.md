# gradle-maven-transform [![Build Status](https://travis-ci.org/uklance/gradle-maven-transform.svg?branch=master)](https://travis-ci.org/uklance/gradle-maven-transform)

Dynamically generate Gradle scripts (and more) by transforming Maven pom.xml's. Provides out-of-the-box support for [Freemarker](https://freemarker.apache.org/) templates but custom transforms can be applied via a custom [template](https://github.com/uklance/gradle-maven-transform/blob/master/plugin/src/main/groovy/com/lazan/maven/transform/Template.java)

Sample usage

```groovy
plugins {
   id "com.lazan.maven-transform" version "0.1"
}
mavenTransform {
   pomXmls 'path1/pom.xml', 'path2/pom.xml', 'path3/pom.xml'
   outputDirectory "$buildDir/mavenTransform"
   templateClasspath files('src/main/freemarker')
   
   projectsTransform {
      // produce a single output file for all pom.xmls
      outputPath 'aggregate.gradle'
      freemarkerTemplate 'aggregate.ftl'
   }

   projectTransform {
      // produce an output file for each pom.xml
      outputPath { context -> "${context.artifactId}.gradle" }
      freemarkerTemplate 'project-dependencies.ftl'
   }
}
```

## Projects Transform

By default, an instance of type [ProjectsContext](https://github.com/uklance/gradle-maven-transform/blob/master/plugin/src/main/groovy/com/lazan/maven/transform/ProjectsContext.java) is available in the template context named 'projectsContext'. The closure configures an instance of [ProjectsTransformModel](https://github.com/uklance/gradle-maven-transform/blob/master/plugin/src/main/groovy/com/lazan/maven/transform/ProjectsTransformModel.java)

## Project Transform

By default, an instance of type [ProjectContext](https://github.com/uklance/gradle-maven-transform/blob/master/plugin/src/main/groovy/com/lazan/maven/transform/ProjectContext.java) is available in the template context named 'projectContext'. The closure configures an instance of [ProjectTransformModel](https://github.com/uklance/gradle-maven-transform/blob/master/plugin/src/main/groovy/com/lazan/maven/transform/ProjectTransformModel.java)
