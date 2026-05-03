package pl.tripocket.tripocket_api.exchangerates.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import pl.tripocket.tripocket_api.common.nbpclient.client.CachedNbpClient;
import pl.tripocket.tripocket_api.common.nbpclient.dto.NbpRate;
import pl.tripocket.tripocket_api.common.nbpclient.dto.NbpTable;
import pl.tripocket.tripocket_api.exchangerates.dto.ExchangeRatesResponse;
import pl.tripocket.tripocket_api.exchangerates.dto.RatesMapper;

@Service
public class ExchangeRatesService {

    private final CachedNbpClient nbpClient;
    private final RatesMapper ratesMapper;

    public ExchangeRatesService(CachedNbpClient nbpClient, RatesMapper ratesMapper) {
        this.nbpClient = nbpClient;
        this.ratesMapper = ratesMapper;
    }

    public ExchangeRatesResponse getPopularExchangeRates() {
        NbpTable tableA = nbpClient.getTableA();

        return ratesMapper.toExchangeRateResponse(tableA.effectiveDate(), tableA.rates());
    }

    public List<ExchangeRatesResponse> getAllExchangeRates() {
        NbpTable tableA = nbpClient.getTableA();
        NbpTable tableB = nbpClient.getTableB();

        return List.of(ratesMapper.toExchangeRateResponse(tableA.effectiveDate(), tableA.rates()), ratesMapper.toExchangeRateResponse(tableB.effectiveDate(), tableB.rates()));
    }

    public List<ExchangeRatesResponse> getExchangeRatesByCodes(List<String> codes) {
        Set<String> wanted = codes.stream().map(String::toUpperCase).collect(Collectors.toSet());

        NbpTable tableA = nbpClient.getTableA();
        NbpTable tableB = nbpClient.getTableB();

        Map<String, List<NbpRate>> ratesByDate = new LinkedHashMap<>();
        Stream.of(tableA, tableB).forEach(table -> {
            List<NbpRate> filtered = table.rates().stream()
                    .filter(rate -> wanted.contains(rate.code().toUpperCase()))
                    .toList();
            if (!filtered.isEmpty()) {
                ratesByDate.computeIfAbsent(table.effectiveDate(), k -> new ArrayList<>()).addAll(filtered);
            }
        });

        return ratesByDate.entrySet().stream()
                .map(entry -> ratesMapper.toExchangeRateResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

}
