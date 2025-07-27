/*
 * This file is part of VeloctopusProject, licensed under the MIT License.
 *
 * Copyright (c) 2025 VeloctopusProject Contributors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * Portions of this implementation are derived from:
 * - VPacketEvents (https://github.com/4drian3d/VPacketEvents) - GNU General Public License v3.0
 *   Original packet handling and event system patterns
 *   Copyright (c) 2023 4drian3d
 */

package org.veloctopus.source.vpacketevents.patterns;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.time.Instant;
import java.time.Duration;

/**
 * VPacketEvents Packet Handling Pattern
 * 
 * Extracted and adapted from VPacketEvents's packet handling and event systems.
 * This pattern provides comprehensive packet management, event handling,
 * and registration patterns for cross-platform communication.
 * 
 * Key adaptations for VeloctopusProject:
 * - Extended to multi-platform packet types (Minecraft + Discord + Matrix)
 * - Async pattern compliance with CompletableFuture
 * - Enhanced packet filtering and transformation
 * - Cross-platform event propagation
 * - Comprehensive packet analytics and monitoring
 * 
 * Original Features from VPacketEvents:
 * - PacketEvent sealed hierarchy (PacketReceiveEvent, PacketSendEvent)
 * - PacketRegistration builder pattern for protocol mapping
 * - ResultedEvent integration for packet allow/deny
 * - Protocol version mapping with multiple version support
 * - MinecraftPacket handling with supplier patterns
 * 
 * @author VeloctopusProject Team
 * @author 4drian3d (Original VPacketEvents implementation)
 * @since 1.0.0
 */
public class VPacketEventsHandlingPattern {

    /**
     * Packet event types adapted from VPacketEvents PacketEvent hierarchy
     */
    public enum PacketEventType {
        /**
         * Packet received from client - Original from PacketReceiveEvent
         */
        PACKET_RECEIVE,
        
        /**
         * Packet sent to client - Original from PacketSendEvent
         */
        PACKET_SEND,
        
        /**
         * VeloctopusProject extension: Cross-platform message received
         */
        CROSS_PLATFORM_RECEIVE,
        
        /**
         * VeloctopusProject extension: Cross-platform message sent
         */
        CROSS_PLATFORM_SEND,
        
        /**
         * VeloctopusProject extension: Packet transformation
         */
        PACKET_TRANSFORM,
        
        /**
         * VeloctopusProject extension: Packet filtering
         */
        PACKET_FILTER
    }

    /**
     * Packet direction adapted from VPacketEvents ProtocolUtils.Direction
     */
    public enum PacketDirection {
        /**
         * Server to client packets - Original from CLIENTBOUND
         */
        CLIENTBOUND,
        
        /**
         * Client to server packets - Original from SERVERBOUND
         */
        SERVERBOUND,
        
        /**
         * VeloctopusProject extension: Cross-platform bidirectional
         */
        BIDIRECTIONAL,
        
        /**
         * VeloctopusProject extension: Internal routing
         */
        INTERNAL
    }

    /**
     * Packet result types adapted from VPacketEvents ResultedEvent.GenericResult
     */
    public enum PacketResult {
        /**
         * Allow packet processing - Original from GenericResult.allowed()
         */
        ALLOWED,
        
        /**
         * Deny packet processing - Original from GenericResult.denied()
         */
        DENIED,
        
        /**
         * VeloctopusProject extension: Transform packet before processing
         */
        TRANSFORM,
        
        /**
         * VeloctopusProject extension: Route to different platform
         */
        ROUTE_CROSS_PLATFORM,
        
        /**
         * VeloctopusProject extension: Queue for batch processing
         */
        QUEUE_BATCH,
        
        /**
         * VeloctopusProject extension: Log and allow
         */
        LOG_AND_ALLOW
    }

    /**
     * Platform types for cross-platform packet handling
     */
    public enum PlatformType {
        MINECRAFT,
        DISCORD,
        MATRIX,
        PYTHON_BRIDGE,
        INTERNAL_API
    }

    /**
     * Packet container adapted from VPacketEvents PacketEvent
     */
    public static class PacketContainer {
        private final Object packet;
        private final String playerId;
        private final String playerName;
        private final PlatformType sourcePlatform;
        private final PlatformType targetPlatform;
        private final PacketEventType eventType;
        private final Instant timestamp;
        private final Map<String, Object> metadata;
        private PacketResult result;

        public PacketContainer(Object packet, String playerId, String playerName, 
                             PlatformType sourcePlatform, PacketEventType eventType) {
            this.packet = packet;
            this.playerId = playerId;
            this.playerName = playerName;
            this.sourcePlatform = sourcePlatform;
            this.targetPlatform = sourcePlatform; // Default to same platform
            this.eventType = eventType;
            this.timestamp = Instant.now();
            this.metadata = new ConcurrentHashMap<>();
            this.result = PacketResult.ALLOWED;
        }

        // Getters and utility methods
        public Object getPacket() { return packet; }
        public String getPlayerId() { return playerId; }
        public String getPlayerName() { return playerName; }
        public PlatformType getSourcePlatform() { return sourcePlatform; }
        public PlatformType getTargetPlatform() { return targetPlatform; }
        public PacketEventType getEventType() { return eventType; }
        public Instant getTimestamp() { return timestamp; }
        public PacketResult getResult() { return result; }
        public void setResult(PacketResult result) { this.result = result; }
        public Map<String, Object> getMetadata() { return metadata; }
        
        public boolean isAllowed() { return result == PacketResult.ALLOWED || result == PacketResult.LOG_AND_ALLOW; }
        public boolean isDenied() { return result == PacketResult.DENIED; }
        public boolean needsTransformation() { return result == PacketResult.TRANSFORM; }
        public boolean needsCrossPlatformRouting() { return result == PacketResult.ROUTE_CROSS_PLATFORM; }
    }

    /**
     * Packet registration system adapted from VPacketEvents PacketRegistration
     */
    public static class PacketRegistrationManager {
        private final Map<Class<?>, PacketTypeRegistration> registrations;
        private final Map<String, List<PacketEventListener>> listeners;

        public PacketRegistrationManager() {
            this.registrations = new ConcurrentHashMap<>();
            this.listeners = new ConcurrentHashMap<>();
        }

        /**
         * Register a packet type with platform and direction information
         * Core logic adapted from VPacketEvents.PacketRegistration builder pattern
         */
        public <T> PacketTypeRegistration<T> registerPacketType(Class<T> packetClass) {
            PacketTypeRegistration<T> registration = new PacketTypeRegistration<>(packetClass);
            registrations.put(packetClass, registration);
            return registration;
        }

        /**
         * Register a packet event listener
         */
        public void registerListener(String packetTypeName, PacketEventListener listener) {
            listeners.computeIfAbsent(packetTypeName, k -> new ArrayList<>()).add(listener);
        }

        /**
         * Process packet through registered listeners
         * Async version of VPacketEvents event handling
         */
        public CompletableFuture<PacketContainer> processPacket(PacketContainer container) {
            return CompletableFuture.supplyAsync(() -> {
                String packetTypeName = container.getPacket().getClass().getSimpleName();
                List<PacketEventListener> packetListeners = listeners.getOrDefault(packetTypeName, Collections.emptyList());

                PacketContainer processedContainer = container;
                for (PacketEventListener listener : packetListeners) {
                    try {
                        processedContainer = listener.handlePacket(processedContainer);
                        if (processedContainer.isDenied()) {
                            break;
                        }
                    } catch (Exception e) {
                        // Log error and continue with next listener
                        processedContainer.getMetadata().put("processing_error", e.getMessage());
                    }
                }

                return processedContainer;
            });
        }
    }

    /**
     * Packet type registration adapted from VPacketEvents PacketRegistration
     */
    public static class PacketTypeRegistration<T> {
        private final Class<T> packetClass;
        private Supplier<T> packetSupplier;
        private PacketDirection direction;
        private PlatformType platformType;
        private final List<ProtocolMapping> mappings;
        private boolean crossPlatformEnabled;

        public PacketTypeRegistration(Class<T> packetClass) {
            this.packetClass = packetClass;
            this.mappings = new ArrayList<>();
            this.crossPlatformEnabled = false;
        }

        /**
         * Set packet supplier - Original from VPacketEvents.packetSupplier()
         */
        public PacketTypeRegistration<T> packetSupplier(Supplier<T> supplier) {
            this.packetSupplier = supplier;
            return this;
        }

        /**
         * Set packet direction - Original from VPacketEvents.direction()
         */
        public PacketTypeRegistration<T> direction(PacketDirection direction) {
            this.direction = direction;
            return this;
        }

        /**
         * Set platform type - VeloctopusProject extension
         */
        public PacketTypeRegistration<T> platform(PlatformType platformType) {
            this.platformType = platformType;
            return this;
        }

        /**
         * Enable cross-platform routing - VeloctopusProject extension
         */
        public PacketTypeRegistration<T> crossPlatform(boolean enabled) {
            this.crossPlatformEnabled = enabled;
            return this;
        }

        /**
         * Add protocol mapping - Adapted from VPacketEvents.mapping()
         */
        public PacketTypeRegistration<T> mapping(int packetId, String version, boolean encodeOnly) {
            mappings.add(new ProtocolMapping(packetId, version, encodeOnly));
            return this;
        }

        /**
         * Complete registration
         */
        public void register() {
            if (packetSupplier == null) {
                throw new IllegalStateException("Packet supplier must be provided");
            }
            if (direction == null) {
                throw new IllegalStateException("Packet direction must be provided");
            }
            if (platformType == null) {
                throw new IllegalStateException("Platform type must be provided");
            }
            if (mappings.isEmpty()) {
                throw new IllegalStateException("At least one protocol mapping must be provided");
            }
            
            // Registration logic would go here
        }

        // Getters
        public Class<T> getPacketClass() { return packetClass; }
        public Supplier<T> getPacketSupplier() { return packetSupplier; }
        public PacketDirection getDirection() { return direction; }
        public PlatformType getPlatformType() { return platformType; }
        public boolean isCrossPlatformEnabled() { return crossPlatformEnabled; }
        public List<ProtocolMapping> getMappings() { return new ArrayList<>(mappings); }
    }

    /**
     * Protocol mapping adapted from VPacketEvents PacketMapping
     */
    public static class ProtocolMapping {
        private final int packetId;
        private final String version;
        private final boolean encodeOnly;

        public ProtocolMapping(int packetId, String version, boolean encodeOnly) {
            this.packetId = packetId;
            this.version = version;
            this.encodeOnly = encodeOnly;
        }

        public int getPacketId() { return packetId; }
        public String getVersion() { return version; }
        public boolean isEncodeOnly() { return encodeOnly; }
    }

    /**
     * Packet event listener interface
     */
    public interface PacketEventListener {
        /**
         * Handle packet event and return modified container
         */
        PacketContainer handlePacket(PacketContainer container);
        
        /**
         * Get listener priority (higher number = higher priority)
         */
        default int getPriority() { return 0; }
        
        /**
         * Check if listener supports the packet type
         */
        default boolean supports(Class<?> packetType) { return true; }
    }

    /**
     * Packet analytics and monitoring system
     */
    public static class PacketAnalytics {
        private final Map<String, Long> packetCounts;
        private final Map<String, Duration> processingTimes;
        private final Map<PacketResult, Long> resultCounts;

        public PacketAnalytics() {
            this.packetCounts = new ConcurrentHashMap<>();
            this.processingTimes = new ConcurrentHashMap<>();
            this.resultCounts = new ConcurrentHashMap<>();
        }

        /**
         * Record packet processing event
         */
        public void recordPacketEvent(PacketContainer container, Duration processingTime) {
            String packetType = container.getPacket().getClass().getSimpleName();
            
            packetCounts.merge(packetType, 1L, Long::sum);
            processingTimes.put(packetType, processingTime);
            resultCounts.merge(container.getResult(), 1L, Long::sum);
        }

        /**
         * Get packet statistics
         */
        public PacketStatistics getStatistics() {
            return new PacketStatistics(
                new HashMap<>(packetCounts),
                new HashMap<>(processingTimes),
                new HashMap<>(resultCounts)
            );
        }
    }

    /**
     * Packet statistics container
     */
    public static class PacketStatistics {
        private final Map<String, Long> packetCounts;
        private final Map<String, Duration> processingTimes;
        private final Map<PacketResult, Long> resultCounts;

        public PacketStatistics(Map<String, Long> packetCounts, 
                              Map<String, Duration> processingTimes,
                              Map<PacketResult, Long> resultCounts) {
            this.packetCounts = packetCounts;
            this.processingTimes = processingTimes;
            this.resultCounts = resultCounts;
        }

        public Map<String, Long> getPacketCounts() { return packetCounts; }
        public Map<String, Duration> getProcessingTimes() { return processingTimes; }
        public Map<PacketResult, Long> getResultCounts() { return resultCounts; }
        
        public long getTotalPackets() { 
            return packetCounts.values().stream().mapToLong(Long::longValue).sum(); 
        }
        
        public double getAverageProcessingTime() {
            return processingTimes.values().stream()
                .mapToLong(Duration::toNanos)
                .average()
                .orElse(0.0) / 1_000_000.0; // Convert to milliseconds
        }
    }
}
