package pl.tripocket.tripocket_api.common.nbpclient.client;

import java.util.List;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import pl.tripocket.tripocket_api.common.nbpclient.dto.NbpTable;

@HttpExchange
public interface NbpClient {

    @GetExchange("/exchangerates/tables/A/?format=json")
    List<NbpTable> getTableA();

    @GetExchange("/exchangerates/tables/B/?format=json")
    List<NbpTable> getTableB();
}
