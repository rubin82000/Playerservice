package com.intuit.playerservice.service.interfaces;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import com.intuit.playerservice.model.Player;

public interface IPlayerService {
    void loadPlayers(Path filePath);
    Optional<Player> getById(String id);
    List<Player> getAll();
    List<Player> getPlayersPage(int page, int size);
    void initFileWatching(Path testFilePath);
}