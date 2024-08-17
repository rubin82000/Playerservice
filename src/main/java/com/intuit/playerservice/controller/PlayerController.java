package com.intuit.playerservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.intuit.playerservice.model.Player;
import com.intuit.playerservice.service.interfaces.IPlayerService;
import java.util.Optional;
import java.util.List;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    @Autowired
    private IPlayerService playerService;

    // Get player by ID
    @GetMapping("/{playerID}")
    public ResponseEntity<Player> getById(@PathVariable String playerID) {
        return handleRequest(() -> {
            Optional<Player> playerOpt = this.playerService.getById(playerID);
            return playerOpt.orElse(null);
        });
    }

    // Get all players
    @GetMapping
    public ResponseEntity<List<Player>> getAll() {
        return handleRequest(() -> this.playerService.getAll());
    }

    // Get players with pagination
    @GetMapping("/paged")
    public ResponseEntity<List<Player>> getPagedPlayers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return handleRequest(() -> this.playerService.getPlayersPage(page, size));
    }

    // Common method to handle requests
    private <T> ResponseEntity<T> handleRequest(Supplier<T> action) {
        try {
            T result = action.get();
            if (result == null || (result instanceof List && ((List<?>) result).isEmpty())) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            } else {
                return ResponseEntity.ok(result);
            }
        } catch (IndexOutOfBoundsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}