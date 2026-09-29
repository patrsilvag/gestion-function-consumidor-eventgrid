package com.function;

import com.microsoft.azure.functions.ExecutionContext;

import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class FunctionTest {

    @Test
    public void testProcesarEventoCitaCreada() {

        // ==========================================
        // Evento simulado de Azure Event Grid
        // ==========================================

        String evento = """
                {
                  "id": "test-cita-001",
                  "eventType": "CitaCreada",
                  "subject": "/veterinaria/citas",
                  "eventTime": "2026-09-24T21:00:00Z",
                  "dataVersion": "1.0",
                  "data": {
                    "idCita": 6,
                    "fechaCita": "30/09/2026 10:00",
                    "idUsuario": 2,
                    "idCliente": 1,
                    "idMascota": 1,
                    "estado": "BLOQUEADA"
                  }
                }
                """;

        // ==========================================
        // Mock del ExecutionContext
        // ==========================================

        final ExecutionContext context = mock(ExecutionContext.class);

        doReturn(Logger.getGlobal()).when(context).getLogger();

        // ==========================================
        // Ejecutar Function
        // ==========================================

        assertDoesNotThrow(() -> new Function().run(evento, context));
    }
}
