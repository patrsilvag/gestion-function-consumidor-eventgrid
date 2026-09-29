package com.function;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

public class Function {

    @FunctionName("ProcessEventGridEvent")
    public void run(@EventGridTrigger(name = "eventGridEvent") String content,
            final ExecutionContext context) {

        context.getLogger().info("========================================");
        context.getLogger().info("Evento recibido desde Azure Event Grid");
        context.getLogger().info("========================================");

        try {

            // ==========================================
            // 1. Convertir el evento recibido a JSON
            // ==========================================

            JsonObject eventGridEvent = JsonParser.parseString(content).getAsJsonObject();

            // ==========================================
            // 2. Obtener tipo de evento
            // ==========================================

            String eventType = eventGridEvent.get("eventType").getAsString();

            context.getLogger().info("Tipo de evento: " + eventType);

            // ==========================================
            // 3. Obtener datos del evento
            // ==========================================

            JsonObject data = eventGridEvent.getAsJsonObject("data");

            context.getLogger().info("Datos recibidos: " + data);

            // ==========================================
            // 4. Procesar RolCreado
            // ==========================================

            if ("RolCreado".equals(eventType)) {

                String nombreRol = data.get("nombreRol").getAsString();

                String estado = data.get("estado").getAsString();

                context.getLogger().info("========================================");

                context.getLogger().info("EVENTO - ROL CREADO");

                context.getLogger().info("========================================");

                context.getLogger().info("Nombre rol: " + nombreRol);

                context.getLogger().info("Estado: " + estado);

                context.getLogger().info("Procesamiento de RolCreado finalizado.");

                context.getLogger().info("========================================");

                return;
            }

            // ==========================================
            // 5. Procesar RolActualizado
            // ==========================================

            if ("RolActualizado".equals(eventType)) {

                long idRol = data.get("idRol").getAsLong();

                String nombreRol = data.get("nombreRol").getAsString();

                String estado = data.get("estado").getAsString();

                context.getLogger().info("========================================");

                context.getLogger().info("EVENTO - ROL ACTUALIZADO");

                context.getLogger().info("========================================");

                context.getLogger().info("ID Rol: " + idRol);

                context.getLogger().info("Nombre rol: " + nombreRol);

                context.getLogger().info("Estado: " + estado);

                context.getLogger().info("Procesamiento de RolActualizado finalizado.");

                context.getLogger().info("========================================");

                return;
            }

            // ==========================================
            // 6. Procesar RolEliminado
            // ==========================================

            if ("RolEliminado".equals(eventType)) {

                long idRol = data.get("idRol").getAsLong();

                context.getLogger().info("========================================");

                context.getLogger().info("EVENTO - ROL ELIMINADO");

                context.getLogger().info("========================================");

                context.getLogger().info("ID Rol: " + idRol);

                context.getLogger().info("Procesamiento de RolEliminado finalizado.");

                context.getLogger().info("========================================");

                return;
            }

            // ==========================================
            // 7. Evento no soportado
            // ==========================================

            context.getLogger().info("Evento recibido pero no procesado: " + eventType);

        } catch (Exception e) {

            context.getLogger().severe("Error procesando evento Event Grid: " + e.getMessage());

            throw e;
        }
    }
}
