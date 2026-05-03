package pl.tripocket.tripocket_api.common.nbpclient.client;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import pl.tripocket.tripocket_api.common.nbpclient.dto.NbpTable;

@Component
public class CachedNbpClient {

    public static final String TABLE_A_CACHE = "nbpTableA";
    public static final String TABLE_B_CACHE = "nbpTableB";

    private final NbpClient nbpClient;

    public CachedNbpClient(NbpClient nbpClient) {
        this.nbpClient = nbpClient;
    }

    @Cacheable(TABLE_A_CACHE)
    public NbpTable getTableA() {
        return nbpClient.getTableA().getFirst();
    }

    @Cacheable(TABLE_B_CACHE)
    public NbpTable getTableB() {
        return nbpClient.getTableB().getFirst();
    }
}
