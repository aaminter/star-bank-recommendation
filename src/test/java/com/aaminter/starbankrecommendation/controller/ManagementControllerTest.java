package com.aaminter.starbankrecommendation.controller;

import com.aaminter.starbankrecommendation.service.cache.CacheManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ManagementControllerTest {

    @Test
    void clearsCachesAndReturnsNoContent() {
        CacheManagementService service = mock(CacheManagementService.class);
        ManagementController controller = new ManagementController(service);

        var response = controller.clearCaches();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).clearAllCaches();
    }
}
