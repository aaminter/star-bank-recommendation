package com.aaminter.starbankrecommendation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ManagementInfoControllerTest {

    @Test
    void returnsArtifactAndVersionFromBuildProperties() {
        BuildProperties buildProperties = mock(BuildProperties.class);
        when(buildProperties.getArtifact()).thenReturn("star-bank-recommendation");
        when(buildProperties.getVersion()).thenReturn("0.0.1-SNAPSHOT");

        ManagementInfoController controller =
                new ManagementInfoController(buildProperties);

        assertEquals(
                "star-bank-recommendation",
                controller.getInfo().get("name")
        );
        assertEquals(
                "0.0.1-SNAPSHOT",
                controller.getInfo().get("version")
        );
        verify(buildProperties).getArtifact();
        verify(buildProperties).getVersion();
    }
}
