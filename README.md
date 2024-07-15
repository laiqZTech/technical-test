## About
This is a Spring Boot application to process a file of define format and produce a download output json.
Includes a toggle flag to enable/disable validation of input file and filter the block IPs and ISPs.
Also Include swagger for easy API management.

## Building application
Latest executable JAR file is stored in the 'dist' folder of project directory.

To rebuild project using maven
```bash
$ mvn clean package
```

## Running Application 
To start application from parent directory of executable jar
```bash
$> java -jar gng-test-0.0.1-SNAPSHOT.jar
```

# To configure feature flag using command line
```bash
java -jar gng-test-0.0.1-SNAPSHOT.jar --entry.file.flag.validation=true
```

# Open API Documentation
Once the application is running, Swagger UI documentation can found at:
[Swagger UI](http://localhost:8080/swagger-ui/index.html)



