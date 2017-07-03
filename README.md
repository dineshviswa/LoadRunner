# surya-soft-interview


Pre- Requisites

1. JDK 1.7 or above

2. Apache Ant 1.9.6 or above


Set Environmental Variables

1. Set environmental variables JAVA_HOME to your Java environment and ANT_HOME to the directory you uncompressed Ant

2. Add ${ANT_HOME}/bin (Unix) or %ANT_HOME%/bin (Windows) to your PATH

3. Add ${JAVA_HOME}/bin (Unix) or %JAVA_HOME%/bin (Windows) to your PATH


Build Packages:

1 . Clone the repository

2. Navigate to the folder /LoadTestRunner

3. There you can find the build.xml file

4. Open the command prompt and run ant

5. It will create the dist directory under LoadTestRunner directory(/LoadTestRunner/dist)

How to run the application :

1. Navigate to the folder /LoadTestRunner/dist

2. There you can find the "config.properties"

3. Edit "userEmailID" as your respective email id and save the file.

4. Open the command prompt and run "java -jar LoadTestRunner.jar"

5. You can the performance output in the terminal/command prompt window.
