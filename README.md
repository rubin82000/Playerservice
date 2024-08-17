
# PlayService

## Overview

PlayService is a Java application based on Spring, designed to manage and monitor a collection of player data. The project includes features for loading, retrieving, and updating player data stored in a CSV file, as well as a file-watching service that automatically reloads the data when the file is modified.

## Key Features

- Loading player data from a CSV file.
- Retrieving player information by ID.
- Paginated browsing of the player list.
- Automatic reloading of data when the source CSV file is modified.
- Unit and integration tests for ensuring reliability.

**Note**: Database integration and additional tests were not implemented, but I'm open to discussing them during our meeting.

## Installation

### Requirements

- Java 11 or higher
- Maven 3.6 or higher

### Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/rubin82000/Playerservice.git
   cd Playerservice
   ```

2. **Build the project using Maven:**
   ```bash
   mvn clean install
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

## Usage

### API Endpoints

- **Get all players (with pagination):**
  ```
  GET /api/players?page={page}&size={size}
  ```
  Example:
  ```
  http://localhost:8080/api/players?page=1&size=10
  ```
  **Parameters:**
  - `page`: the page number (starting from 1).
  - `size`: the number of records on one page.

- **Get player by ID:**
  ```
  GET /api/players/{playerID}
  ```
  Example:
  ```
  http://localhost:8080/api/players/1
  ```

## Configuration

The application uses a properties file (`application.properties`) to configure settings such as the path to the CSV file, logging, and server details. These can be modified as needed.

### Example Configuration:
```properties
spring.devtools.restart.exclude=**
spring.application.name=playerservice
logging.file.name=logs/app.log
logging.level.root=INFO
player.file.path=src/main/resources/player.csv
```

## Testing

### Unit Tests

To run the unit tests, use the following Maven command:
```bash
mvn test
```

### Integration Tests

Integration tests are also included to ensure that the application interacts with its environment correctly. They can be run using the same Maven command as above.

**Note**: Additional tests can be discussed during the meeting.

## Contact

If you have any questions or need support, please contact Rubin at rubin82000@hotmail.com.
