# Payment comparison project

1. The concept behind the project is to perform a financial reconciliation between two different sets of data
2. You’ll have received these sets of data as two sample CSV files
3. All you need to do is compare the two files, and report on how many transactions match perfectly, versus
transactions which cannot be matched
4. And those transactions which cannot be matched will need to be reported on, so that a third party could refer to
the report and investigate the exceptions
5. If a transaction cannot be matched perfectly, you should attempt to look for any close matches and suggest them
as possibilities
6. Note that this is **not** a file comparison project, this is a transaction matching/reconciliation project. In other
words, the project should do its best to identify for users all non-perfectly matched records possible matches
based on a reconciliation strategy you come up with , it might be you consider only the most important fields, or
consider all fields but at different level of importance... Defining the important fields is up to you, the
TransactionID for example might be the only field you consider or one important field out of several, or it may
not. There is no right or wrong answer, please feel free to make logical assumptions in your reconciliation
strategy, detail what they are and deliver that documentation alongside the project code.
7. You do not need to store the results, or provide any further functionality once you have listed the exceptions
(and potential matches where possible)
8. Provided [Screen mockup](mockup_Transaction%20Compare.png) to give an example of the process flow

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
curl -X POST 'http://localhost:8080/v1/upload' -F 'file=@src/test/resources/PaymentMarkoffFile20140113.csv' -F 'file=@src/test/resources/ClientMarkoffFile20140113.csv'
```

Measure request/response with timings:
```asciidoc
time curl -X POST 'http://localhost:8080/v1/upload' -F 'file=@src/test/resources/PaymentMarkoffFile20140113.csv' -F 'file=@src/test/resources/ClientMarkoffFile20140113.csv'
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