# Clock-In App - Setup & Development Guide

## Table of Contents
- [System Requirements](#system-requirements)
- [Installation](#installation)
- [IDE Setup](#ide-setup)
- [Database Configuration](#database-configuration)
- [Running Tests](#running-tests)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

## System Requirements

### Minimum Requirements
- **Java**: 21 or higher
- **Maven**: 3.6.0 or higher
- **RAM**: 2 GB minimum
- **Disk Space**: 500 MB
- **OS**: macOS 10.15+, Windows 10+, or Linux (Ubuntu 18.04+)

### Optional
- **Git**: 2.20+ (for version control)
- **IDE**: IntelliJ IDEA, Eclipse, VS Code, or NetBeans

### Verify Installation

```bash
# Check Java version (should be 21+)
java -version

# Check Maven version (should be 3.6+)
mvn -version
```

## Installation

### 1. Clone Repository

```bash
git clone https://github.com/yourusername/clock-in-app.git
cd clock-in-app
```

### 2. Install Dependencies

```bash
mvn clean install
```

This downloads:
- JavaFX 21.0.2
- SQLite JDBC 3.45.3.0
- bcrypt (password hashing)
- SLF4J + Logback (logging)
- JUnit 4.13.2 (testing)

### 3. Verify Installation

```bash
# Run tests to verify everything works
mvn test
```

If all tests pass, you're ready to go!

## IDE Setup

### IntelliJ IDEA

**Step 1: Open Project**
1. File → Open
2. Select the `clock-in-app` folder
3. Click "Open as Project"

**Step 2: Configure JDK**
1. File → Project Structure
2. Select "Project" → "SDK"
3. Choose Java 21 (or download it)
4. Click "Apply" and "OK"

**Step 3: Create Run Configuration**
1. Run → Edit Configurations
2. Click "+" and select "Application"
3. Configure as follows:
   - **Name**: Clock-In App
   - **Main class**: com.yourcompany.clockin.App
   - **Working directory**: `$PROJECT_DIR$`
   - **Module name**: (should auto-populate)
   - **VM options**: `-Dfile.encoding=UTF-8`
4. Click "Apply" and "OK"

**Step 4: Run Application**
- Click the green ▶ button or press Ctrl+F10

### VS Code

**Step 1: Install Extensions**
1. Click Extensions (Ctrl+Shift+X)
2. Search and install:
   - Extension Pack for Java
   - Maven for Java
   - JavaFX Support

**Step 2: Open Project**
1. File → Open Folder
2. Select the `clock-in-app` folder
3. Trust the workspace when prompted

**Step 3: Create Run Configuration**
1. Create `.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Clock-In App",
      "request": "launch",
      "mainClass": "com.yourcompany.clockin.App",
      "projectName": "clock-in-app",
      "cwd": "${workspaceFolder}",
      "console": "integratedTerminal"
    }
  ]
}
```

**Step 4: Run Application**
1. Press F5 or click Run → Start Debugging
2. Click "Maven" when prompted for Java environment

### Eclipse

**Step 1: Import Project**
1. File → Import → Existing Maven Projects
2. Browse to `clock-in-app` folder
3. Click "Finish"

**Step 2: Configure Project**
1. Right-click project → Properties
2. Java Build Path → Libraries tab
3. Ensure JRE System Library is Java 21
4. Click "Apply and Close"

**Step 3: Create Run Configuration**
1. Run → Run Configurations
2. Right-click "Java Application" → New
3. Configure:
   - **Name**: Clock-In App
   - **Project**: clock-in-app
   - **Main class**: com.yourcompany.clockin.App
4. Click "Apply" and "Run"

### NetBeans

**Step 1: Open Project**
1. File → Open Project
2. Select `clock-in-app` folder
3. Click "Open Project"

**Step 2: Run Application**
1. Right-click project → Run
2. NetBeans will auto-configure and launch

## Database Configuration

### Default Configuration

The application creates and manages the database automatically:
- **Location**: `~/clock-in-data/clockin.db`
- **Schema**: Auto-created on first run
- **Tables**: `users`, `clock_records`

### Custom Database Location

Edit `src/main/java/com/yourcompany/Datamanager.java`:

```java
private static final Path DATA_DIR = Paths.get("/custom/path/to/data");
```

### Database Schema

**Users Table:**
```sql
CREATE TABLE users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  created_at TEXT DEFAULT CURRENT_TIMESTAMP,
  last_login TEXT
);
```

**Clock Records Table:**
```sql
CREATE TABLE clock_records (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL,
  clock_in_time TEXT,
  clock_out_time TEXT,
  clock_in_photo TEXT,
  clock_out_photo TEXT
);
```

### Backing Up Data

```bash
# Create backup
cp -r ~/clock-in-data ~/clock-in-data.backup

# Restore backup
rm -rf ~/clock-in-data
cp -r ~/clock-in-data.backup ~/clock-in-data
```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=DatamanagerTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=DatamanagerTest#testVerifyUserSuccess
```

### Generate Coverage Report
```bash
mvn clean test jacoco:report
# Report available at: target/site/jacoco/index.html
```

## Building & Packaging

### Build JAR File
```bash
mvn clean package
```

Creates: `target/clock-in-app-1.0-SNAPSHOT.jar`

### Run JAR
```bash
java -jar target/clock-in-app-1.0-SNAPSHOT.jar
```

### Create Executable (macOS)
```bash
# Create app bundle
mvn clean package appbundle:create

# Run app
open target/Clock-In\ App.app
```

## Troubleshooting

### Java Version Issues

**Error**: `java.lang.UnsupportedClassVersionError`

**Solution**:
```bash
# Verify Java version
java -version

# Update JAVA_HOME if needed
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
java -version  # Should now show Java 21
```

### Maven Issues

**Error**: `Cannot run program "mvn"`

**Solution**:
```bash
# Install Maven (macOS with Homebrew)
brew install maven

# Verify installation
mvn -version
```

### JavaFX Module Not Found

**Error**: `module javafx.controls not found`

**Solution**:
Maven plugin should handle this. If not:
```bash
# Clear Maven cache and reinstall
rm -rf ~/.m2/repository
mvn clean install
```

### Camera/Photo Issues

**Error**: `Cannot open camera device`

**Solution**:
1. Check system permissions:
   - macOS: System Preferences → Security & Privacy → Camera
   - Windows: Settings → Privacy → Camera
   - Linux: `sudo usermod -a -G video $USER` (then logout/login)

2. Restart application
3. Try different USB camera if available

### Database Locked

**Error**: `database is locked`

**Solution**:
```bash
# Check for running processes
ps aux | grep java

# Kill hanging Java process if needed
kill -9 <PID>

# Delete lock files
rm ~/clock-in-data/clockin.db-journal
```

### Out of Memory

**Error**: `java.lang.OutOfMemoryError`

**Solution**:
```bash
# Increase heap size when running
java -Xmx2g -jar target/clock-in-app-1.0-SNAPSHOT.jar

# Or set environment variable
export MAVEN_OPTS="-Xmx2g"
mvn javafx:run
```

## Development Workflow

### Making Changes

1. **Modify code**
   ```bash
   # Edit source files
   vim src/main/java/com/yourcompany/...
   ```

2. **Test changes**
   ```bash
   mvn test
   ```

3. **Run application**
   ```bash
   mvn javafx:run
   ```

4. **Build package**
   ```bash
   mvn clean package
   ```

### Code Style Guidelines

- Use 4-space indentation
- Follow JavaBean naming conventions
- Use try-with-resources for connections
- Add logging instead of `System.out.println()`
- Document public methods with Javadoc

Example:
```java
/**
 * Authenticates user and updates last login timestamp.
 * 
 * @param username the user's username
 * @param password the user's plain-text password
 * @return true if authentication succeeds, false otherwise
 */
public boolean verifyUser(String username, String password) {
    // Implementation
}
```

### Commit Changes

```bash
# Stage changes
git add src/

# Commit with descriptive message
git commit -m "Add user authentication with bcrypt"

# Push to remote
git push origin main
```

## Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/your-feature`
3. Make changes and test: `mvn test`
4. Commit: `git commit -am 'Add feature'`
5. Push: `git push origin feature/your-feature`
6. Create Pull Request on GitHub

## Performance Optimization

### Improve Startup Time
- Use Spring Native for GraalVM compilation
- Pre-load UI components

### Reduce Memory Usage
- Limit photo resolution
- Implement image compression
- Archive old records

### Database Optimization
- Add indexes on frequently queried columns
- Implement record archiving
- Regular VACUUM maintenance

```bash
# In app:
sqlite3 ~/clock-in-data/clockin.db "VACUUM;"
```

## Security Checklist

- [ ] Change default admin password
- [ ] Use strong password policy
- [ ] Enable database encryption
- [ ] Restrict file permissions on ~/clock-in-data/
- [ ] Review audit logs regularly
- [ ] Keep dependencies updated

## Next Steps

1. **Customize Application**
   - Update company branding
   - Configure authentication methods
   - Set password policies

2. **Deploy**
   - Create executable JAR
   - Deploy to server
   - Configure reverse proxy (optional)

3. **Monitor**
   - Review logs: `~/clock-in-data/logs/`
   - Track user activity
   - Analyze performance metrics

## Getting Help

1. Check this guide and README.md
2. Review logs: `cat ~/clock-in-data/logs/clock-in-app.log`
3. Open issue on GitHub with:
   - Java version
   - OS and version
   - Error message
   - Steps to reproduce

## Additional Resources

- [JavaFX Documentation](https://openjfx.io/)
- [SQLite Documentation](https://www.sqlite.org/docs.html)
- [Maven Guide](https://maven.apache.org/guides/)
- [bcrypt Documentation](https://github.com/patrickfav/bcrypt)
- [SLF4J Guide](http://www.slf4j.org/manual.html)

---

**Last Updated**: 2026-08-10
**Version**: 1.0.0
