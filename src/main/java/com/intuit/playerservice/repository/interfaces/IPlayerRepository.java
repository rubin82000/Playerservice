package com.intuit.playerservice.repository.interfaces;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.intuit.playerservice.model.Player;

public interface IPlayerRepository {
    List<Player> getAll();
    List<Player> getPlayersPage(int page, int size);
    void loadPlayers(Path path) throws IOException;
    Optional<Player> getById(String id);
}