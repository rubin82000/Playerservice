package com.intuit.playerservice.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.intuit.playerservice.logging.ILogger;
import com.intuit.playerservice.model.Player;
import com.intuit.playerservice.repository.interfaces.IPlayerRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PlayerRepository implements IPlayerRepository {
    private volatile List<Player> playersCache = new ArrayList<>();
    private ILogger logger;

    public PlayerRepository(ILogger logger) {
        this.logger = logger;
    }

    @Override
    public List<Player> getAll() {
        logger.info("Fetching all players from cache.");
        return this.playersCache;
    }

    public List<Player> getPlayersPage(int page, int size) {
        int fromIndex = (page - 1) * size;
        if (fromIndex >= playersCache.size()) {
            throw new IndexOutOfBoundsException("Page number out of bounds: " + page);
        }
        int toIndex = Math.min(fromIndex + size, playersCache.size());
        return playersCache.subList(fromIndex, toIndex);
    }

    @Override
    public void loadPlayers(Path path) throws IOException {
        logger.info("Loading players from file: " + path);

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        
        MappingIterator<Player> it = csvMapper
            .readerFor(Player.class)
            .with(schema)
            .readValues(path.toFile());
        logger.info("CSV reading completed: " + path);
        var tempCache = it.readAll();
        this.playersCache = tempCache;

        logger.info("Players loaded successfully from file: " + path);
    }

    @Override
    public Optional<Player> getById(String id) {
        logger.info("Fetching player by ID: " + id);
        return this.playersCache.stream()
            .filter(player -> player.getPlayerID().equals(id))
            .findFirst();
    }
}