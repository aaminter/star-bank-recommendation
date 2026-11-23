package com.aaminter.starbankrecommendation.service.cache;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class CacheManagementServiceTest {

    @Test
    void clearsEveryRegisteredCacheOwner() {
        CacheClearable first = mock(CacheClearable.class);
        CacheClearable second = mock(CacheClearable.class);
        CacheManagementService service =
                new CacheManagementService(List.of(first, second));

        service.clearAllCaches();

        verify(first).clearCache();
        verify(second).clearCache();
    }
}
