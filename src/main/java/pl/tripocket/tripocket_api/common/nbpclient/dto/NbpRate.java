package pl.tripocket.tripocket_api.common.nbpclient.dto;

public record NbpRate(
    String currency,
    String code,
    double mid
) {}
