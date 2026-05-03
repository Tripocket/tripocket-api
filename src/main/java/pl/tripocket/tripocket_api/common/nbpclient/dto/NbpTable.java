package pl.tripocket.tripocket_api.common.nbpclient.dto;

import java.util.List;

public record NbpTable(
    String table,
    String no,
    String effectiveDate,
    List<NbpRate> rates
) {}