package pl.tripocket.tripocket_api.common.nbpclient.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class NbpClientConfig {

    @Bean
    NbpClient nbpClient() {
        RestClient restClient = RestClient.builder()
            .baseUrl("https://api.nbp.pl/api")
            .build();

        return HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build()
            .createClient(NbpClient.class);
    }
}