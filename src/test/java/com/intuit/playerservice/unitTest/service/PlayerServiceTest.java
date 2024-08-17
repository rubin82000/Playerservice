package com.intuit.playerservice.unitTest.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.intuit.playerservice.logging.ILogger;
import com.intuit.playerservice.model.Player;
import com.intuit.playerservice.repository.interfaces.IPlayerRepository;
import com.intuit.playerservice.service.PlayerService;
import com.intuit.playerservice.service.interfaces.IFileWatcherService;
import com.intuit.playerservice.service.interfaces.IPlayerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.lang.reflect.Field;

@SpringBootTest
@ActiveProfiles("test")
public class PlayerServiceTest {
    @Autowired
    private IPlayerService playerService;
    private IPlayerRepository playerRepository;
    private IFileWatcherService fileWatcherService;
    private ILogger logger;
    @Value("${player.file.path}")
    private String filePathString;
    private Path testFilePath;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock dependencies
        playerRepository = mock(IPlayerRepository.class);
        fileWatcherService = mock(IFileWatcherService.class);
        logger = mock(ILogger.class);
        
        // Set up the test file path
        this.testFilePath = Paths.get(filePathString);
        
        // Manually create the PlayerService instance with mocks
        playerService = new PlayerService(playerRepository, fileWatcherService, logger);
        
        // Set the filePath directly (simulate @Value injection)
        updatePrivateFieldValue(playerService, testFilePath, "filePath");
    }

    @Test
    void testInitFileWatching() throws IOException {
        // Execute the method
        playerService.initFileWatching(testFilePath);

        // Verify that fileWatcherService methods are called correctly
        verify(fileWatcherService).registerFile(eq(testFilePath), any());
        verify(fileWatcherService).startWatching();

        // Verify that loadPlayers is called correctly
        verify(playerRepository).loadPlayers(testFilePath);

        // Verify logging
        verify(logger).info("Initializing PlayerService...");
        verify(logger).info("PlayerService initialized successfully.");
    }

    @Test
    void testInitFileWatchingHandlesIOException() throws IOException {
        // Arrange: Simulate IOException when registering the file
        doThrow(new IOException("Test IOException")).when(fileWatcherService).registerFile(any(Path.class), any());

        // Act: Execute the method
        playerService.initFileWatching(testFilePath);

        // Assert: Verify that the error message is set correctly
        assertEquals("Error initializing FileWatcherService: Test IOException", getPrivateFieldValue(playerService, "loadErrorMessage"));

        // Assert: Verify that the logger logs the error
        verify(logger).error(eq("Error initializing FileWatcherService"), any(IOException.class));

        // Assert: Verify that startWatching is never called due to the exception
        verify(fileWatcherService, never()).startWatching();
    }

    @Test
    public void testLoadPlayers() throws IOException {
        // Arrange
        doNothing().when(playerRepository).loadPlayers(this.testFilePath);

        // Act
        playerService.loadPlayers(this.testFilePath);
        Object errorMessage = getPrivateFieldValue(playerService, "loadErrorMessage");

        // Assert
        verify(playerRepository).loadPlayers(this.testFilePath);
        assertNull(errorMessage);
    }

    @Test
    public void testLoadPlayersWithError() throws IOException {
        // Arrange
        var filePath = Paths.get("src/main/resources/fakeFile.csv");
        doThrow(new IOException("Test error")).when(playerRepository).loadPlayers(filePath);

        // Act
        playerService.loadPlayers(filePath);

        // Capture and assert the error message
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);

        verify(logger).error(messageCaptor.capture(), throwableCaptor.capture());

        // Assert the captured error message
        assertNotNull(getPrivateFieldValue(playerService, "loadErrorMessage"));
        assertEquals("Error loading data: ", messageCaptor.getValue());
        assertEquals("Test error", throwableCaptor.getValue().getMessage());
    }

    @Test
    public void testGetByIdWhenNoError() {
        // Arrange
        String playerId = "player1";
        Player player = new Player();
        when(playerRepository.getById(playerId)).thenReturn(Optional.of(player));

        // Act
        Optional<Player> result = playerService.getById(playerId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(player, result.get());
    }

    @Test
    public void testGetByIdWhenError() {
        // Arrange
        String playerId = "player1";
        updatePrivateFieldValue(playerService, "Load error", "loadErrorMessage");

        // Act
        Optional<Player> result = playerService.getById(playerId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetAllWhenNoError() {
        // Arrange
        List<Player> players = List.of(new Player(), new Player());
        when(playerRepository.getAll()).thenReturn(players);

        // Act
        List<Player> result = playerService.getAll();

        // Assert
        assertEquals(players, result);
    }

    @Test
    public void testGetAllWhenError() {
        // Arrange
        updatePrivateFieldValue(playerService, "Load error", "loadErrorMessage");

        // Act
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            playerService.getAll();
        });

        // Assert
        assertEquals("Load error", exception.getMessage());
    }

    public static Object getPrivateFieldValue(Object object, String fieldName) {
        try {
            Class<?> clazz = object.getClass();
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true); // Make private fields accessible
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null; // Return NULL if there's an error
        }
    }
    
    public static void updatePrivateFieldValue(Object object, Object newObject, String fieldName) {
        try {
            Class<?> clazz = object.getClass();
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true); // Make private fields accessible
            field.set(object, newObject);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return; // Return if there's an error
        }
    }
}