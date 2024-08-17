package com.intuit.playerservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.intuit.playerservice.logging.ILogger;
import com.intuit.playerservice.service.interfaces.IFileWatcherService;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class FileWatcherService implements IFileWatcherService {
    private WatchService watchService;
    private final Map<Path, Consumer<Path>> fileListeners = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    private ILogger logger;

    public FileWatcherService(ILogger logger) throws IOException {
        this.logger = logger;
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    @Override
    public void registerFile(Path path, Consumer<Path> onChange) throws IOException {
        Path dir = path.getParent();
        dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        fileListeners.put(path, onChange);
        logger.info("Registered file: " + path.toFile());
    }

    @Override
    public void startWatching() {
        running = true;
        logger.info("Starting file watcher...");
        new Thread(this::watchFiles).start();
    }

    @Override
    public void stopWatching() {
        running = false;
        try {
            watchService.close();
            logger.info("Stopped file watcher.");
        } catch (IOException e) {
            logger.error("Error while stopping file watcher", e);
        }
    }

    private void watchFiles() {
        try {
            while (running) {
                WatchKey key = watchService.take(); // Or use watchService.poll() for a non-blocking call
                Path currentDir = (Path) key.watchable(); // Get the path to the watched directory
                
                List<WatchEvent<Path>> events = (List<WatchEvent<Path>>) (List<?>) key.pollEvents();
                for (WatchEvent<Path> event : events) {
                    Path relativePath = event.context();
                    Path fullPath = currentDir.resolve(relativePath);

                    Consumer<Path> listener = fileListeners.get(fullPath);
                    if (listener != null) {
                        logger.info("File changed: " + fullPath.toFile());
                        listener.accept(fullPath);
                    }
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("File watching interrupted", e);
        } catch (ClosedWatchServiceException e) {
            logger.info("Watch service closed.");
        }
    }
}