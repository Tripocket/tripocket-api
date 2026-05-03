package pl.tripocket.tripocket_api.exchangerates.dto;

import java.util.List;

import pl.tripocket.tripocket_api.common.nbpclient.dto.NbpRate;

public record ExchangeRatesResponse(
    String effectiveDate,
    List<NbpRate> rates
) {

}
