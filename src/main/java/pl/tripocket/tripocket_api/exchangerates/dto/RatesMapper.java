package pl.tripocket.tripocket_api.exchangerates.dto;

import java.util.List;

import org.mapstruct.Mapper;

import pl.tripocket.tripocket_api.common.nbpclient.dto.NbpRate;

@Mapper(componentModel = "spring")
public interface RatesMapper {
    ExchangeRatesResponse toExchangeRateResponse(String effectiveDate, List<NbpRate> rates);
}
