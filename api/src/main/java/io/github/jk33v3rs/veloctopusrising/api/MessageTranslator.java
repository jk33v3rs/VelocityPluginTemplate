package io.github.jk33v3rs.veloctopusrising.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Handles message translation between different platforms.
 * 
 * <p>This interface provides methods for converting messages between
 * Minecraft, Discord, Matrix, and other platforms while preserving
 * formatting and metadata.</p>
 * 
 * <h2>Supported Platforms:</h2>
 * <ul>
 *   <li>MINECRAFT - Velocity proxy and backend servers</li>
 *   <li>DISCORD - Discord bots (Security Bard, Flora, May, Librarian)</li>
 *   <li>MATRIX - Matrix protocol rooms</li>
 *   <li>AI_BRIDGE - Python AI tool integration</li>
 * </ul>
 * 
 * <h2>Format Conversion:</h2>
 * <p>The translator automatically handles format conversion:</p>
 * <ul>
 *   <li>MiniMessage (Minecraft) ↔ Markdown (Discord)</li>
 *   <li>Adventure Components ↔ Discord Embeds</li>
 *   <li>Emoji handling across platforms</li>
 *   <li>Mention translation (@player ↔ &lt;@userid&gt;)</li>
 * </ul>
 * 
 * <h2>Performance Characteristics:</h2>
 * <ul>
 *   <li><strong>Latency</strong>: &lt;50ms for standard message translation</li>
 *   <li><strong>Throughput</strong>: 1000+ messages/second sustained</li>
 *   <li><strong>Memory</strong>: &lt;1MB per 1000 cached translations</li>
 * </ul>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public interface MessageTranslator {
    
    /**
     * Supported communication platforms.
     * 
     * <p>Each platform has its own message format requirements and
     * capabilities for rich formatting and interactive elements.</p>
     * 
     * @since 1.0.0
     */
    enum Platform {
        /** Minecraft servers using Adventure Components and MiniMessage */
        MINECRAFT,
        
        /** Discord using JDA with rich embeds and markdown */
        DISCORD,
        
        /** Matrix protocol rooms with markdown formatting */
        MATRIX,
        
        /** Python AI bridge for LLM integration */
        AI_BRIDGE
    }
    
    /**
     * Represents a message that can be translated between platforms.
     * 
     * <p>This class encapsulates all message data including content,
     * metadata, sender information, and platform-specific formatting.</p>
     * 
     * @since 1.0.0
     */
    final class TranslatableMessage {
        private final String content;
        private final UUID senderId;
        private final String senderName;
        private final Map<String, Object> metadata;
        private final Instant timestamp;
        private final Platform originalPlatform;
        
        /**
         * Creates a new translatable message.
         * 
         * @param content The message content in the original platform format
         * @param senderId Unique identifier of the sender (player UUID, Discord ID, etc.)
         * @param senderName Display name of the sender
         * @param metadata Additional platform-specific metadata
         * @param originalPlatform The platform this message originated from
         */
        public TranslatableMessage(String content, UUID senderId, String senderName, 
                                 Map<String, Object> metadata, Platform originalPlatform) {
            this.content = content;
            this.senderId = senderId;
            this.senderName = senderName;
            this.metadata = Map.copyOf(metadata); // Immutable copy
            this.timestamp = Instant.now();
            this.originalPlatform = originalPlatform;
        }
        
        /** @return The message content in original format */
        public String getContent() { return content; }
        
        /** @return Unique sender identifier */
        public UUID getSenderId() { return senderId; }
        
        /** @return Display name of sender */
        public String getSenderName() { return senderName; }
        
        /** @return Immutable metadata map */
        public Map<String, Object> getMetadata() { return metadata; }
        
        /** @return Message creation timestamp */
        public Instant getTimestamp() { return timestamp; }
        
        /** @return Original platform of this message */
        public Platform getOriginalPlatform() { return originalPlatform; }
    }
    
    /**
     * Translates a message from one platform to another.
     * 
     * <p>This method converts message format, handles emoji translation,
     * processes mentions, and preserves as much formatting as possible
     * within the constraints of the target platform.</p>
     * 
     * <h3>Translation Process:</h3>
     * <ol>
     *   <li>Parse source format (MiniMessage, Markdown, etc.)</li>
     *   <li>Extract semantic elements (text, formatting, mentions)</li>
     *   <li>Convert to target platform format</li>
     *   <li>Apply platform-specific optimizations</li>
     *   <li>Validate output format and length constraints</li>
     * </ol>
     * 
     * @param message The message to translate
     * @param fromPlatform The source platform format
     * @param toPlatform The target platform format
     * @return CompletableFuture containing the translated message
     * 
     * @throws IllegalArgumentException if platforms are not supported
     * @since 1.0.0
     * @see TranslatableMessage
     */
    CompletableFuture<TranslatableMessage> translate(
        TranslatableMessage message,
        Platform fromPlatform,
        Platform toPlatform
    );
    
    /**
     * Translates and sends a message to the specified platform.
     * 
     * <p>This is a convenience method that combines translation and delivery.
     * It handles the complete workflow from translation to final delivery
     * with error handling and retry logic.</p>
     * 
     * @param message The message to translate and send
     * @param fromPlatform The source platform
     * @param toPlatform The target platform
     * @param targetChannel The target channel/room identifier
     * @return CompletableFuture that completes when the message is sent
     * 
     * @throws IllegalArgumentException if platforms or channel are invalid
     * @since 1.0.0
     */
    CompletableFuture<Void> translateAndSend(
        TranslatableMessage message,
        Platform fromPlatform,
        Platform toPlatform,
        String targetChannel
    );
    
    /**
     * Registers a custom format converter for specific platform pairs.
     * 
     * <p>This allows plugins to extend the translation system with
     * custom conversion logic for specific platform combinations.</p>
     * 
     * @param fromPlatform Source platform
     * @param toPlatform Target platform
     * @param converter Custom conversion function
     * @since 1.0.0
     */
    void registerConverter(
        Platform fromPlatform,
        Platform toPlatform,
        Function<TranslatableMessage, TranslatableMessage> converter
    );
}
