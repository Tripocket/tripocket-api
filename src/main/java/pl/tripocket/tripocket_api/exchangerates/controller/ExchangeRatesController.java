package pl.tripocket.tripocket_api.exchangerates.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.tripocket.tripocket_api.exchangerates.dto.ExchangeRatesResponse;
import pl.tripocket.tripocket_api.exchangerates.service.ExchangeRatesService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/exchange/rates/")
public class ExchangeRatesController {
    private final ExchangeRatesService exchangeRatesService;
    
    public ExchangeRatesController(ExchangeRatesService exchangeRatesService) {
        this.exchangeRatesService = exchangeRatesService;
    }

    @GetMapping("/popular")
    public ResponseEntity<ExchangeRatesResponse> getPopularExchangeRates() {
        return ResponseEntity.ok(exchangeRatesService.getPopularExchangeRates());
    }

    @GetMapping("/all")
    public ResponseEntity<List<ExchangeRatesResponse>> getAllExchangeRates() {
        return ResponseEntity.ok(exchangeRatesService.getAllExchangeRates());
    }

    @GetMapping
    public ResponseEntity<List<ExchangeRatesResponse>> getExchangeRatesByCodes(@RequestParam List<String> codes) {
        return ResponseEntity.ok(exchangeRatesService.getExchangeRatesByCodes(codes));
    }

}
