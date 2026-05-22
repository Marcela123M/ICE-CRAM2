package com.example.IceCream_SpringBoot.controller;

import com.example.IceCream_SpringBoot.service.HeladoTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;
    private final ChatClient mcpChatClient;
    private final ChatClient dbChatClient;

    public ChatController(@Qualifier("chatClient") ChatClient chatClient,
                          ChatClient.Builder chatClientBuilder,
                          ToolCallbackProvider toolCallbackProvider,
                          HeladoTools heladoTools) {
        this.chatClient = chatClient;
        this.mcpChatClient = chatClientBuilder
                .defaultTools(toolCallbackProvider)
                .build();
        this.dbChatClient = chatClientBuilder
                .defaultTools(heladoTools)
                .build();
    }

    @Value("classpath:/promptTemplates/userPromptTemplate.st")
    Resource userPromptTemplate;

    @GetMapping("/email")
    public String emailResponse(@RequestParam("customerName") String customerName,
            @RequestParam("customerMessage") String customerMessage) {
        return chatClient
                .prompt()
                .system("""
                        Eres un asistente profesional de atención al cliente que ayuda a redactar correos
                        electrónicos para mejorar la productividad del equipo de atención al cliente.
                        """)
                .user(promptTemplateSpec -> promptTemplateSpec.text(userPromptTemplate)
                        .param("customerName", customerName)
                        .param("customerMessage", customerMessage))
                .call().content();
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        List<Message> messages = new ArrayList<>();

        if (request.history() != null) {
            for (HistoryMessage hm : request.history()) {
                if ("user".equals(hm.role())) {
                    messages.add(new UserMessage(hm.content()));
                } else if ("assistant".equals(hm.role())) {
                    messages.add(new AssistantMessage(hm.content()));
                }
            }
        }

        return chatClient
                .prompt()
                .system("""
                        Eres el asistente virtual de Ice Cream, un software de gestión para heladerías en Cartagena.
                        SOLO puedes responder preguntas relacionadas con:
                        - Qué es Ice Cream y para qué sirve
                        - Los servicios del software (inventario, ventas, reportes, predicciones, Power BI, Weka)
                        - El equipo de desarrollo (Jean Paul, Dilan, Oscar, Sebastian)
                        - La heladería Ice Cream en Cartagena
                        Si el usuario pregunta cualquier otra cosa que NO tenga relación con Ice Cream o la heladería, responde exactamente: "Solo puedo responder preguntas relacionadas con Ice Cream y nuestra heladería."
                        No respondas preguntas de tecnología, ciencia, historia, matemáticas ni ningún otro tema ajeno al proyecto.
                        Responde siempre en español.
                        """)
                .messages(messages)
                .user(request.message())
                .call()
                .content();
    }

    @GetMapping("/mcp")
    public String mcpChat(@RequestParam("message") String message) {
        try {
            return mcpChatClient
                    .prompt()
                    .system("""
                            Eres el asistente técnico del sistema Ice Cream para administradores.
                            Tienes acceso directo a la base de datos y al sistema de archivos.
                            
                            HERRAMIENTAS DISPONIBLES - ÚSALAS DIRECTAMENTE SIN PEDIR CONFIRMACIÓN EXTRA:
                            - listarHelados: lista todo el inventario de helados
                            - buscarHeladoPorNombre: busca un helado específico
                            - verUnidades: muestra unidades de un helado por ubicación
                            - moverHeladoAHeladeria: mueve unidades del almacén a la heladería
                            - agregarHelado: agrega un nuevo helado (nombre, sabor, tipo, precio, unidades, ubicacion)
                            - eliminarHelado: deshabilita un helado y guarda historial
                            - modificarHelado: cambia precio o unidades de un helado
                            - consultarUltimasVentas: muestra las últimas 10 ventas
                            - consultarResumenVentas: total de ventas y monto total
                            - verificarStockBajo: alerta helados con menos de 5 unidades
                            - generarInformeInventario: crea TXT con el inventario
                            - generarInformeVentas: crea TXT con las ventas
                            - generarInformeCompleto: crea TXT con inventario y ventas
                            
                            REGLAS:
                            - Ejecuta las acciones directamente sin preguntar demasiado
                            - Si el usuario dice "agrega", "elimina", "mueve", "modifica" → llama la herramienta inmediatamente
                            - Si faltan datos necesarios, pide SOLO lo que falta en una sola pregunta
                            - Responde siempre en español de forma concisa y profesional
                            """)
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            System.err.println("ERROR EN MCP CHAT: " + e.getMessage());
            e.printStackTrace();
            String msg = e.getMessage();
            if (msg != null && msg.contains("failed_generation")) {
                int start = msg.indexOf("failed_generation\":\"") + 20;
                int end = msg.lastIndexOf("\"}}");
                if (start > 20 && end > start) {
                    return msg.substring(start, end)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"");
                }
            }
            return "Error al procesar la solicitud.";
        }
    }

    @GetMapping("/db")
    public String dbChat(@RequestParam("message") String message) {
        try {
            return dbChatClient
                    .prompt()
                    .system("Eres un asistente técnico del sistema de gestión Ice Cream para administradores. Cuando uses tools que requieren números, SIEMPRE envía números reales (no strings): nuevoPrecio debe ser Double como 15.0, nuevasUnidades debe ser Integer como 10. Ayuda con consultas sobre inventario, ventas, archivos del sistema y gestión del negocio. Responde siempre en español de forma profesional y técnica.")
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            return "Error al procesar la solicitud: " + e.getMessage();
        }
    }

    @GetMapping("/informe/{tipo}")
    public ResponseEntity<org.springframework.core.io.Resource> descargarInforme(
            @PathVariable String tipo) throws Exception {
        String ruta = "info/informe_" + tipo + ".txt";
        Path path = Path.of(ruta);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        org.springframework.core.io.Resource resource =
                new org.springframework.core.io.FileSystemResource(path);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=informe_" + tipo + ".txt")
                .body(resource);
    }

    public record ChatRequest(String message, List<HistoryMessage> history) {}
    public record HistoryMessage(String role, String content) {}
}