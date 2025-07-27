/**
 * Performance baseline testing framework for borrowed code implementations.
 *
 * <p>This package provides comprehensive performance testing, benchmarking, and
 * regression detection capabilities for all borrowed and adapted code components
 * in VeloctopusRising. It establishes performance baselines and monitors for
 * degradation during integration and operation.</p>
 *
 * <h2>Step 10: Performance Baseline Testing Framework</h2>
 * <p>This package completes Step 10 of the 400-step implementation plan by providing:</p>
 * <ul>
 *     <li><strong>Baseline Establishment:</strong> {@link PerformanceBaselineFramework#establishBaseline}</li>
 *     <li><strong>Performance Testing:</strong> {@link PerformanceBaselineFramework#runPerformanceTest}</li>
 *     <li><strong>Async Adapter Testing:</strong> {@link PerformanceBaselineFramework#runAdapterPerformanceTest}</li>
 *     <li><strong>Regression Detection:</strong> Automated comparison against baselines</li>
 * </ul>
 *
 * <h2>Core Components</h2>
 *
 * <h3>Performance Baseline Framework</h3>
 * <p>The {@link PerformanceBaselineFramework} class provides the main testing interface:</p>
 * <ul>
 *     <li>Establishes performance baselines for borrowed components</li>
 *     <li>Runs performance tests with configurable concurrency and duration</li>
 *     <li>Compares test results against established baselines</li>
 *     <li>Provides detailed metrics and performance analysis</li>
 * </ul>
 *
 * <h3>Test Configuration</h3>
 * <p>The {@link PerformanceBaselineFramework.TestConfiguration} class configures test parameters:</p>
 * <ul>
 *     <li><strong>Duration:</strong> How long to run the performance test</li>
 *     <li><strong>Concurrency:</strong> Number of concurrent operations</li>
 *     <li><strong>Delay:</strong> Time between operations (for rate limiting)</li>
 * </ul>
 *
 * <h3>Metrics and Results</h3>
 * <p>Comprehensive metrics collection includes:</p>
 * <ul>
 *     <li><strong>Response Times:</strong> Average, median, P95, P99, maximum</li>
 *     <li><strong>Throughput:</strong> Operations per second</li>
 *     <li><strong>Error Rates:</strong> Success/failure ratios</li>
 *     <li><strong>Performance Comparison:</strong> Percentage changes from baseline</li>
 * </ul>
 *
 * <h2>Integration with Borrowed Code</h2>
 *
 * <h3>Async Adapter Testing</h3>
 * <p>Specialized testing for {@link io.github.jk33v3rs.veloctopusrising.api.async.AsyncAdapter} implementations:</p>
 * <ul>
 *     <li>Lifecycle performance (initialization, startup, shutdown)</li>
 *     <li>Operational performance (health checks, metrics collection)</li>
 *     <li>Resource utilization monitoring</li>
 * </ul>
 *
 * <h3>Borrowed Component Integration</h3>
 * <p>Framework integrates with borrowed code from reference projects:</p>
 * <ul>
 *     <li><strong>Spicord Discord Bot:</strong> Message processing performance</li>
 *     <li><strong>HuskChat Messaging:</strong> Cross-server message routing speed</li>
 *     <li><strong>EpicGuard Security:</strong> Rate limiting and bot detection performance</li>
 *     <li><strong>Other Components:</strong> Standardized testing approach</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Performance Testing</h3>
 * <pre>{@code
 * PerformanceBaselineFramework framework = new PerformanceBaselineFramework();
 *
 * // Establish baseline
 * BaselineResult baseline = framework.establishBaseline(
 *     "discord-message-processing",
 *     () -> discordBot.processMessage("test message"),
 *     Duration.ofMinutes(2)
 * ).join();
 *
 * // Run performance test
 * PerformanceResult result = framework.runPerformanceTest(
 *     "discord-message-processing",
 *     () -> discordBot.processMessage("test message"),
 *     TestConfiguration.builder()
 *         .duration(Duration.ofMinutes(5))
 *         .concurrency(10)
 *         .delayBetweenOperations(100)
 *         .build()
 * ).join();
 *
 * // Check for performance degradation
 * if (result.getComparison().getStatus() == PerformanceStatus.DEGRADED) {
 *     System.out.println("Performance degraded by " + 
 *         result.getComparison().getAverageResponseTimeChangePercent() + "%");
 * }
 * }</pre>
 *
 * <h3>Async Adapter Testing</h3>
 * <pre>{@code
 * AsyncAdapter<DiscordBot> discordAdapter = new DiscordBotAdapter(botConfig);
 * 
 * AdapterPerformanceResult result = framework.runAdapterPerformanceTest(
 *     "discord-bot-adapter",
 *     discordAdapter,
 *     TestConfiguration.builder()
 *         .duration(Duration.ofMinutes(3))
 *         .build()
 * ).join();
 *
 * // Analyze lifecycle performance
 * TestMetrics lifecycle = result.getLifecycleMetrics();
 * System.out.println("Startup time: " + lifecycle.getAverageResponseTime() + "ns");
 *
 * // Analyze operational performance
 * TestMetrics operational = result.getOperationalMetrics();
 * System.out.println("Health check throughput: " + operational.getThroughput() + " ops/sec");
 * }</pre>
 *
 * <h2>Performance Standards</h2>
 *
 * <h3>Acceptable Performance Thresholds</h3>
 * <ul>
 *     <li><strong>Response Time Degradation:</strong> &lt; 20% increase acceptable</li>
 *     <li><strong>Throughput Degradation:</strong> &lt; 15% decrease acceptable</li>
 *     <li><strong>Error Rate Increase:</strong> &lt; 10% increase acceptable</li>
 *     <li><strong>Memory Usage:</strong> Monitored for resource leaks</li>
 * </ul>
 *
 * <h3>Continuous Integration</h3>
 * <p>Framework integrates with CI/CD pipelines:</p>
 * <ul>
 *     <li>Automated baseline establishment during builds</li>
 *     <li>Performance regression detection in pull requests</li>
 *     <li>Performance reporting and trending</li>
 * </ul>
 *
 * @since 1.0.0
 * @author VeloctopusRising Development Team
 * @see io.github.jk33v3rs.veloctopusrising.api.async.AsyncPattern
 * @see io.github.jk33v3rs.veloctopusrising.api.async.AsyncAdapter
 * @see io.github.jk33v3rs.veloctopusrising.api.borrowed
 */
package io.github.jk33v3rs.veloctopusrising.api.testing;
