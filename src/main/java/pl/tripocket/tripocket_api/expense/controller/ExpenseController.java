package pl.tripocket.tripocket_api.expense.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import pl.tripocket.tripocket_api.expense.dto.ExpenseCreateRequest;
import pl.tripocket.tripocket_api.expense.dto.ExpenseResponse;
import pl.tripocket.tripocket_api.expense.service.ExpenseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips/{tripId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable UUID tripId,
            @RequestBody @Valid ExpenseCreateRequest request,
            JwtAuthenticationToken token) {
        return new ResponseEntity<>(expenseService.createExpense(tripId, request, token), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getTripExpenses(
            @PathVariable UUID tripId,
            JwtAuthenticationToken token) {
        return ResponseEntity.ok(expenseService.getTripExpenses(tripId, token));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpense(
            @PathVariable UUID tripId,
            @PathVariable UUID expenseId,
            JwtAuthenticationToken token) {
        return ResponseEntity.ok(expenseService.getExpense(tripId, expenseId, token));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID tripId,
            @PathVariable UUID expenseId,
            JwtAuthenticationToken token) {
        expenseService.deleteExpense(tripId, expenseId, token);
        return ResponseEntity.noContent().build();
    }
}