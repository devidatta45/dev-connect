# dev-connect

### Pre-requisite

1. Sbt needs to be installed.
2. Docker needs to be installed.

### Running Solution

1. Clone the Repository and run `sbt docker:publishLocal`. As I have added
   sbt native packager plugin this will create the appropriate Dockerfile
   and will build the docker image using that.
2. Run the already built image using `docker run -d -p 9000:9000 devconnect:0.1 ` .
   This will start the application which will expose the rest api.
3. Also, the application can be started by starting the `DevConnectApp` 
   class if opened from an IDE.
4. The postman script in the postman folder then can be used from any
   rest client for testing the application.

### Running the tests
1. Run `sbt test` for running the tests in the test folder.
2. Also, the tests can be run by running them directly after opening
   in an IDE sequentially or individually.