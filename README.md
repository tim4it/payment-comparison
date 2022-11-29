# Payment comparison 
Payment comparison project
Files:

[Assignment](Welcome%20To%20Paymentology.pdf) document

[GUI data](mockup_Transaction%20Compare.png) picture

# Build an testing

Build application with tests:

```asciidoc
./gradlew clean build
```

Build application - without tests:

```asciidoc
./gradlew clean build -x test
```

Run application - starts application on port 8080:
```asciidoc
./gradlew run
```

# Test

Run the tests:
```asciidoc
./gradlew clean build
```

Test reporting (open in web browser):
```asciidoc
build/reports/tests/test/index.html
```

# Test coverage

Run the tests:
```asciidoc
./gradlew clean build
```

Test coverage - jacoco (open in web browser):
```asciidoc
build/reports/jacoco/html/index.html
```

# Run application with CURL command

Run application locally:
```asciidoc
./gradlew run
```

Go to project location and execute **Curl** command:
```asciidoc
curl -X POST 'http://localhost:8080/v1/upload' -F 'file=@src/test/resources/PaymentologyMarkoffFile20140113.csv' -F 'file=@src/test/resources/ClientMarkoffFile20140113.csv'
```

Measure request/response with timings:
```asciidoc
time curl -X POST 'http://localhost:8080/v1/upload' -F 'file=@src/test/resources/PaymentologyMarkoffFile20140113.csv' -F 'file=@src/test/resources/ClientMarkoffFile20140113.csv'
```

# Test application with swagger

Run application locally:
```asciidoc
./gradlew run
```
Open swagger documentation and testing:

```asciidoc
http://localhost:8080/swagger/views/swagger-ui/index.html
```
Endpoint **/upload**. Add any two CSV files for testing purpose.
Click **Add string item** and choose CSV file.
We can upload only two files.

# Test application using WEB UI

Run server:
```asciidoc
./gradlew run
```

Open link (opera, safari, chrome):
```asciidoc
http://localhost:8080/views/comparison
```