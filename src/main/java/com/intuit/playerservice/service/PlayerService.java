package com.intuit.playerservice.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.intuit.playerservice.model.Player;
import com.intuit.playerservice.repository.interfaces.IPlayerRepository;
import com.intuit.playerservice.service.interfaces.IFileWatcherService;
import com.intuit.playerservice.service.interfaces.IPlayerService;
import com.intuit.playerservice.logging.ILogger;
import java.nio.file.Path;
import jakarta.annotation.PostConstruct;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import java.util.function.Supplier;

@Service
public class PlayerService implements IPlayerService {

    @Value("${player.file.path}")
    private String filePathString;
    
    @Autowired
    private Environment env;

    private Path filePath;
    private IPlayerRepository playerRepository;
    private IFileWatcherService fileWatcherService;
    private ILogger logger;
    private String loadErrorMessage = null;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public PlayerService(IPlayerRepository playerRepository, 
        IFileWatcherService fileWatcherService, 
        ILogger logger ) {
        this.playerRepository = playerRepository;
        this.fileWatcherService = fileWatcherService;
        this.logger = logger;
    }

    @PostConstruct
    public void init() {
        this.filePath = Paths.get(filePathString);

        if (Arrays.asList(env.getActiveProfiles()).contains("test")) 
            return;

        initFileWatching(filePath);
    }

    public void initFileWatching(Path path)
    {
        try {
            logger.info("Initializing PlayerService...");

            this.fileWatcherService.registerFile(path, this::loadPlayers);
            this.loadPlayers(path);
            this.fileWatcherService.startWatching();
            
            logger.info("PlayerService initialized successfully.");
        } catch (IOException e) {
            this.loadErrorMessage = "Error initializing FileWatcherService: " + e.getMessage();
            logger.error("Error initializing FileWatcherService", e);
        }
    }

    public void loadPlayers(Path path) {
        lock.writeLock().lock();
        try {
            this.playerRepository.loadPlayers(path);
            this.loadErrorMessage = null;
        } catch (IOException e) {
            this.loadErrorMessage = "Error loading data: " + e.getMessage();
            logger.error("Error loading data: ", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<Player> getById(String id) {
        return executeWithReadLock(() -> {
            if (this.loadErrorMessage != null) {
                return Optional.empty();
            }
            return this.playerRepository.getById(id);
        });
    }

    public List<Player> getAll() {
        return executeWithReadLock(() -> {
            if (this.loadErrorMessage != null) {
                throw new IllegalStateException(this.loadErrorMessage);
            }
            return this.playerRepository.getAll();
        });
    }

    public List<Player> getPlayersPage(int page, int size) {
        return executeWithReadLock(() -> {
            if (this.loadErrorMessage != null) {
                throw new IllegalStateException(this.loadErrorMessage);
            }
            return this.playerRepository.getPlayersPage(page, size);
        });
    }

    private <T> T executeWithReadLock(Supplier<T> action) {
        lock.readLock().lock();
        try {
            return action.get();
        } finally {
            lock.readLock().unlock();
        }
    }
}