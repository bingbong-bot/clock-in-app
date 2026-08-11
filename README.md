# Clock-In App

A professional JavaFX-based employee clock-in/clock-out system with photo verification, SQLite database backend, and administrative reporting features.

## Features

✅ **Employee Management**
- User registration with secure password hashing (bcrypt)
- Employee clock-in/clock-out with photo verification
- Automatic timestamp recording

✅ **Security**
- Password-based authentication with bcrypt hashing (12 rounds)
- Role-based access control (Employee/Admin)
- Photo verification for audit trails

✅ **Data Management**
- SQLite database with persistent storage
- Comprehensive clock record tracking
- Photo storage and retrieval
- Query interface for data analysis

✅ **Admin Features**
- View all employee clock records
- Filter records by employee and date
- Database viewer for advanced queries
- Audit logging for compliance

✅ **Production Ready**
- Comprehensive error handling
- SLF4J logging with file rotation
- Maven-based build system
- Full test coverage
- GitHub deployment ready

## Quick Start

### Using Maven
```bash
mvn clean javafx:run
```

### Using run.sh
```bash
./run.sh
```

### From IDE
See [SETUP.md](SETUP.md) for detailed IDE instructions.

## Project Structure

```
clock-in-app/
├── src/main/java/com/yourcompany/
│   ├── clockin/
│   │   ├── App.java
│   │   ├── Employee.java
│   │   └── ClockRecord.java
│   └── Datamanager.java
├── src/main/resources/logback.xml
├── src/test/java/com/yourcompany/DatamanagerTest.java
├── pom.xml
└── README.md
```

## Features & Documentation

- **Authentication**: Bcrypt password hashing
- **Database**: SQLite with automatic schema creation
- **Logging**: SLF4J with Logback (file rotation)
- **Testing**: Comprehensive JUnit test suite
- **Error Handling**: Production-grade exception handling

## Build & Test

```bash
# Run tests
mvn test

# Build JAR
mvn clean package

# Run application
mvn javafx:run
```

## Database Location

`~/clock-in-data/clockin.db`

## Troubleshooting

See [SETUP.md](SETUP.md) for detailed troubleshooting guide.

## License

MIT License
