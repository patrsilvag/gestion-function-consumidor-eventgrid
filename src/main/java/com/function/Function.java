package com.function;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
                context.getLogger()
                        .info("Acción: quitar automáticamente el rol a los usuarios asociados.");

                String sqlActualizarUsuarios = """
                        UPDATE USUARIOS
                        SET ID_ROL = 1
                        WHERE ID_ROL = ?
                        """;

                String sqlEliminarRol = """
                        DELETE FROM ROLES
                        WHERE ID_ROL = ?
                          AND ESTADO = 'INACTIVO'
                        """;

                try (Connection connection = OracleConnection.getConnection();
                        PreparedStatement statementUsuarios =
                                connection.prepareStatement(sqlActualizarUsuarios);
                        PreparedStatement statementRol =
                                connection.prepareStatement(sqlEliminarRol)) {

                    connection.setAutoCommit(false);

                    context.getLogger().info("Ejecutando UPDATE de usuarios en Oracle...");

                    statementUsuarios.setLong(1, idRol);

                    int usuariosActualizados = statementUsuarios.executeUpdate();

                    context.getLogger().info("Usuarios actualizados: " + usuariosActualizados);

                    context.getLogger().info("Ejecutando DELETE físico del rol INACTIVO...");

                    statementRol.setLong(1, idRol);

                    int rolesEliminados = statementRol.executeUpdate();

                    if (rolesEliminados == 0) {

                        connection.rollback();

                        context.getLogger()
                                .warning("No se eliminó el rol. Puede que ya haya sido procesado "
                                        + "o que el rol no esté INACTIVO.");

                        return;
                    }

                    connection.commit();

                    context.getLogger().info("USUARIOS ACTUALIZADOS CORRECTAMENTE");

                    context.getLogger().info("ID_ROL NUEVO PARA LOS USUARIOS: 1 (SIN ROL)");

                    context.getLogger().info("ROL ELIMINADO FÍSICAMENTE CORRECTAMENTE");

                    context.getLogger().info("ID_ROL ELIMINADO: " + idRol);

                    context.getLogger().info("COMMIT realizado correctamente.");

                } catch (Exception e) {

                    context.getLogger().severe("ERROR procesando RolEliminado para el rol " + idRol
                            + ": " + e.getMessage());

                    throw e;
                }

                context.getLogger().info("Procesamiento de RolEliminado finalizado.");

                context.getLogger().info("========================================");

                return;
            }

            if ("UsuarioCreado".equals(eventType)) {

                long idUsuario = data.get("idUsuario").getAsLong();

                long idRol = data.get("idRol").getAsLong();

                String nombreUsuario = data.get("nombreUsuario").getAsString();

                String email = data.get("email").getAsString();

                String estado = data.get("estado").getAsString();

                context.getLogger().info("========================================");
                context.getLogger().info("EVENTO - USUARIO CREADO");
                context.getLogger().info("========================================");

                context.getLogger().info("ID Usuario: " + idUsuario);

                context.getLogger().info("ID Rol recibido: " + idRol);

                context.getLogger().info("Nombre usuario: " + nombreUsuario);

                context.getLogger().info("Email: " + email);

                context.getLogger().info("Estado: " + estado);

                // ==========================================
                // Validar rol inicial
                // ==========================================

                if (idRol != 1) {

                    context.getLogger().warning("El evento UsuarioCreado no tiene "
                            + "ID_ROL = 1 (SIN ROL). " + "No se realizará actualización.");

                    return;
                }

                context.getLogger().info("ID_ROL actual: 1 (SIN ROL)");

                context.getLogger()
                        .info("Acción: asignar automáticamente " + "ID_ROL = 2 (CLIENTE)");

                // ==========================================
                // Actualizar usuario en Oracle
                // ==========================================

                String sql = """
                        UPDATE USUARIOS
                        SET ID_ROL = 2
                        WHERE ID_USUARIO = ?
                          AND ID_ROL = 1
                        """;

                try (Connection connection = OracleConnection.getConnection();
                        PreparedStatement statement = connection.prepareStatement(sql)) {

                    connection.setAutoCommit(false);

                    statement.setLong(1, idUsuario);

                    context.getLogger().info("Ejecutando UPDATE en Oracle...");

                    int filasActualizadas = statement.executeUpdate();

                    // ==========================================
                    // Verificar resultado
                    // ==========================================

                    if (filasActualizadas == 1) {

                        connection.commit();

                        context.getLogger().info("USUARIO ACTUALIZADO CORRECTAMENTE");

                        context.getLogger().info("ID_USUARIO: " + idUsuario);

                        context.getLogger().info("ID_ROL NUEVO: 2 (CLIENTE)");

                        context.getLogger().info("COMMIT realizado correctamente.");

                    } else {

                        connection.rollback();

                        context.getLogger().warning("El UPDATE no modificó ninguna fila.");

                        context.getLogger().warning(
                                "Puede que el usuario no exista " + "o que ya tenga otro rol.");
                    }

                } catch (Exception e) {

                    context.getLogger().severe("ERROR actualizando el rol del usuario " + idUsuario
                            + ": " + e.getMessage());

                    throw e;
                }

                context.getLogger().info("Procesamiento de UsuarioCreado finalizado.");

                context.getLogger().info("========================================");

                return;
            }

            // ==========================================
            // 8. Procesar UsuarioActualizado
            // ==========================================

            if ("UsuarioActualizado".equals(eventType)) {

                long idUsuario = data.get("idUsuario").getAsLong();

                long idRol = data.get("idRol").getAsLong();

                String nombreUsuario = data.get("nombreUsuario").getAsString();

                String email = data.get("email").getAsString();

                String estado = data.get("estado").getAsString();

                context.getLogger().info("========================================");
                context.getLogger().info("EVENTO - USUARIO ACTUALIZADO");
                context.getLogger().info("========================================");

                context.getLogger().info("ID Usuario: " + idUsuario);

                context.getLogger().info("ID Rol: " + idRol);

                context.getLogger().info("Nombre usuario: " + nombreUsuario);

                context.getLogger().info("Email: " + email);

                context.getLogger().info("Estado: " + estado);

                context.getLogger().info("Procesamiento de UsuarioActualizado finalizado.");

                context.getLogger().info("========================================");

                return;
            }

            // ==========================================
            // 9. Procesar UsuarioEliminado
            // ==========================================

            if ("UsuarioEliminado".equals(eventType)) {

                long idUsuario = data.get("idUsuario").getAsLong();

                context.getLogger().info("========================================");
                context.getLogger().info("EVENTO - USUARIO ELIMINADO");
                context.getLogger().info("========================================");

                context.getLogger().info("ID Usuario: " + idUsuario);

                context.getLogger().info("Procesamiento de UsuarioEliminado finalizado.");

                context.getLogger().info("========================================");

                return;
            }

            // ==========================================
            // 10. Evento no soportado
            // ==========================================

            context.getLogger().info("Evento recibido pero no procesado: " + eventType);

        } catch (Exception e) {

            context.getLogger().severe("Error procesando evento Event Grid: " + e.getMessage());

            throw new RuntimeException("Error procesando evento Event Grid.", e);
        }
    }
}
