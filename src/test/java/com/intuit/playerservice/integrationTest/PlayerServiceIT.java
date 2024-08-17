package com.intuit.playerservice.integrationTest;

import com.intuit.playerservice.logging.ILogger;
import com.intuit.playerservice.model.Player;
import com.intuit.playerservice.repository.PlayerRepository;
import com.intuit.playerservice.repository.interfaces.IPlayerRepository;
import com.intuit.playerservice.service.FileWatcherService;
import com.intuit.playerservice.service.PlayerService;
import com.intuit.playerservice.service.interfaces.IFileWatcherService;
import com.intuit.playerservice.service.interfaces.IPlayerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PlayerServiceIT {

    private IPlayerService playerService;
    private IPlayerRepository playerRepository;
    private IFileWatcherService fileWatcherService;
    @Mock
    private ILogger logger;

    @Value("${player.file.path}")
    private String filePathString;
    private Path testFilePath;

    @BeforeEach
    public void setUp() throws IOException {
        // Create a temporary CSV file with test data
        this.testFilePath = Paths.get(filePathString);
        Files.write(testFilePath, "playerID,birthYear,birthMonth\n1,1980,12\n2,1976,2".getBytes());

        fileWatcherService = new FileWatcherService(logger);
        playerRepository = new PlayerRepository(logger);
        playerService = new PlayerService(playerRepository, fileWatcherService, logger);
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Delete the temporary file after tests are done
        Files.deleteIfExists(testFilePath);
    }

    @Test
    public void testLoadPlayers() {
        playerService.loadPlayers(testFilePath);

        // Load players and verify they are loaded correctly
        List<Player> players = playerRepository.getAll();
        assertNotNull(players);
        assertEquals(2, players.size());

        Player player1 = players.get(0);
        assertEquals("1", player1.getPlayerID());
        assertEquals(1980, player1.getBirthYear());
        assertEquals(12, player1.getBirthMonth());

        Player player2 = players.get(1);
        assertEquals("2", player2.getPlayerID());
        assertEquals(1976, player2.getBirthYear());
        assertEquals(2, player2.getBirthMonth());
    }

    @Test
    public void testGetById() {
        this.playerService.loadPlayers(testFilePath);

        // Verify getting a player by ID
        Optional<Player> player = playerService.getById("1");
        assertTrue(player.isPresent());
        assertEquals(1980, player.get().getBirthYear());
    }

    @Test
    public void testGetAll() {
        this.playerService.loadPlayers(testFilePath);

        // Verify getting all players
        List<Player> players = playerService.getAll();
        assertNotNull(players);
        assertEquals(2, players.size());
    }

    @Test
    public void testFileWatcherTriggersReload() throws IOException, InterruptedException {
        playerService.initFileWatching(testFilePath);

        // Update the CSV file and verify the changes are reloaded
        Files.write(testFilePath, "playerID,birthYear,birthMonth\n1,1960,11\n2,1979,5\n3,1945,12".getBytes());

        // Wait a bit to allow the FileWatcher to detect changes
        Thread.sleep(500);

        List<Player> players = playerService.getAll();
        assertEquals(3, players.size());

        Player player3 = players.stream().filter(p -> p.getPlayerID().equals("3")).findFirst().orElse(null);
        assertNotNull(player3);
        assertEquals(1945, player3.getBirthYear());
        assertEquals(12, player3.getBirthMonth());
    }

    @Test
    public void testLoadPlayersWithError() {
        // Verify error handling when loading players
        Path invalidPath = Paths.get("invalid.csv");
        playerService.loadPlayers(invalidPath);

        assertThrows(IllegalStateException.class, () -> playerService.getAll());
    }

    @Test
    public void testGetByIdWithError() {
        // Verify behavior of getById method when loading fails
        Path invalidPath = Paths.get("invalid.csv");
        playerService.loadPlayers(invalidPath);

        Optional<Player> player = playerService.getById("1");
        assertTrue(player.isEmpty());
    }
}