package com.app;

public class App {

    public int add(int a, int b){
    	return a+b;
    }
    public static void main( String[] args ) {
        System.out.println(new App().add(40,50));
    }
}


/*
 
What is build process?
    Keeping resources ready > Arranging them in various folders > add libraries/jar to classpath > 
    compilation > execution > testing > packing for release or deployment is continuously required in 
    project development and delivery process.

Core Java Project build process
===============================
 a. develop java resources and other files
 b. keep them in different folders
 c. add jar files to classpath
 d. compilation
 e. execution/testing[performing testing on his own piece of code is called "UnitTesting"]
 f. packing the app for release.

Performing build process activities manually is having lots of limitations
a. remembering compilated and repeatitive operations is very tough.
b. we may mismatch order.
c. we may forget certain activities
d. doing multiple activities of build process manually will waste the time.

To automate this process activities we can use .bat file
========================================================
run.bat
-------
cd e:
md xyz
cd xyz
copy ... ...
copy ... ...
set path=...
set classpath = ...
javac -d *.java
java <pkg>.<MainClass>
 cmd> run.bat 

batch file is given to combine all related commands into single command[by using 
single command we can automate the process]

limitations of batch files
--------------------------
 a. Conditional execution is not possible.
 b. we can not create dependancy among the operation.
 c. jar files must be added dynamically no dynamic downloading of jar file from internet
 d. if one command files is batch file .next command will not execute.
 e. It is not declarative[ not self intelligent ie, we need to tell everything to do]

To overcome some of these problems we got ant tool[Another Neat Tool]
 a. It is same as batch files, but we can keep operations as conditional 
 operations and we can create dependency among the operations.

To overcome both these tools problem we got "Maven" tools with lots of advanced features
=> Maven is just not a build tool it is also called as "Project Management tool".

KeyFeatures of maven
====================
1. Maven tries to avoid so much configurations as possible by chooosing real 
   default values and supplying project templates[archetypes]
2. Can download jars automatically.
3. Can maintatin mulitple repositories having jar files, plugins etc
4. Provides standard project directory structure.
5. Gives maven inheritance to share jar files and plugin among the multiple projects.
6. Allows to develop multi module projects.
7. Can generate the war,jar,ear file based on the application componenets.
8. Can generate the project documentation.
9. Can run unit tests and can generate unit test reports.
10. Can clean and install the projects in the local servers or remote servers.

Archetypes[Project templates]
=============================
 1. maven-archetype-quickstart[for standalone projects]
 2. maven-archetype-webapp[for webapplications]
    
    Note: archetypes are project directory structure models

Maven can be build in 2 ways
a. In command line mode
b. From IDE Like Eclipse,Intelij,Netbeans,Eclipselink,....

To keep maven in our system
============================
a. Download zip file and extract it from the following link
https://maven.apache.org/download.cgi(send one in the link => apachemaven-3.9.1-bin.zip)
b. Create the following environment variables
 a. Add <maven_home>\bin to path environment variables. 
    set path=D:\jars\apache-maven-3.9.1\bin
 b. Add java installation folder to JAVA_HOME environment variables.
    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202

To check whether maven is installed properly or not just type mvn -version in command prompt

Maven repositories
==================
    > Repositories is a small db or folder which holds items.
    > Maven repositories can hold
        a. jar files/libraries/dependancies
        b. plugins(patch software to provide additional functionalities)
        c. old/sample projects[Springboot==> Old project==> inherited through => Maveninheritance]

In Maven everything (jar/plugin/project) is identified with 3 details[GAV]
a. groupId(company name)
b. artifactId(jar file name/plugin name/projectname)
c. version(jar/plugin/project version)[stable(currently in use),SNAPSHOT(next version not stable),RELEASE(next version ready)]

eg#1.
GroupId => pivotal team
artifactId => spring-aspects
version => 5.3.17

Repositories
============
a. Central Repositories(given by maven people)
Central Repository:
Avaialble in internet, managed by Apache Maven Community. When Maven does not find 
any dependency in local repository, it starts searching in central repository.
 URL for central repository: http://repo.maven.apache.org/maven2
Generally maintains free jars and plugins of open source technologies

b. Local Repositories(In every machine where maven is required)
It is user specific repository, generally it will be collected from TL/PL who
creates Maven project directory structure. 
Contains jars, plugins, current project related packings and etc..
Default location: C:\users\<usernmae>\.m2 
Will be created automatically for any maven command apart from (mvn -version)
Location can be changed through <maven_home>\conf\settings.xml file using
<localRepository>d:\\maven</localRepository>

c. Remote Repositories(Give by third party companies)

Creating an Project using MAVEN in CLI Mode
===========================================

1. Open the Command Prompt and change directory where you want to create your 
project and call 
 mvn archetype:generate (goal to begin the process).
D:\maven>mvn archetype:generate(press enter key)
Choose a number or apply filter (format: [groupId:]artifactId, case sensitive 
contains): 2036:(press enter key)
Choose org.apache.maven.archetypes:maven-archetype-quickstart version:
1: 1.0-alpha-1
2: 1.0-alpha-2
3: 1.0-alpha-3
4: 1.0-alpha-4
5: 1.0
6: 1.1
7: 1.3
8: 1.4
Choose a number: 8:(press enter key)
Define value for property 'groupId': app
Define value for property 'artifactId': maven001
Define value for property 'version' 1.0-SNAPSHOT: : 1.0
Define value for property 'package' app: : com.app
Confirm properties configuration:
groupId: app
artifactId: maven001
version: 1.0
package: com.app



maven001
    |=> src/main/java[source code]
                    |=> com.app
                            |=> App.java(source code)
    |=> src/test/java[test code-> unit testing]
                    |=> com.app
                            |=> AppTest.java(test code)
    |=> pom.xml(build file)===> groupId,artifactId,version


In the command prompt execute the following life cycle actions
a. mvn package
» Generates jar files in target folder having <projectname>-ver. jar file
b. mvn clean
» Cleans the project .. deletes target folder
c. mvn clean package
» Cleans the project and also creates jar file with latest code
d. mvn compile
>> compile the project code and generate the .class file in target folder.
To run jar file App manually
D:\mavenpgms\MathProjl>java -cp target/MathProj1-1.0.jar com.app.App

The Maven Life cycles are
a. clean(3 phases)
b. default(23 phases)
c. site (4 phases)

> Each life cycle of maven will have lot of phases.
> These phases are already linked with plugins to peform certain operations, but 
we can configure extra plugins to perform more operations.

D:\Mavenpgms\MathProj1>mvn test
=> runs all the test cases and generates the report in command line.
D:\Mavenpgms\MathProj1>mvn surefire-report:report
=> go to target folder search for a file called surefire-report.html
D:\Mavenpgms\MathProj1>mvn site
=> go to target folder search for a file called index.html

Maven can't execute the java app directly becoz there is no life cycle phases for 
that.
To use that we need to use an extra plugin called :: exec-maven-plugin
<build>
    <plugins>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>exec-maven-plugin</artifactId>
            <version>3.1.0</version>
            <executions>
                <execution>
                    <id>ArithmeticApp</id>
                    <phase>package</phase>
                    <goals>
                        <goal>java</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <mainClass>in.ineuron.MathApp</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>

 */