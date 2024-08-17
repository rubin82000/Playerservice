package com.intuit.playerservice.service.interfaces;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface IFileWatcherService {
    void registerFile(Path path, Consumer<Path> onChange) throws IOException;
    void startWatching();
    void stopWatching();
}