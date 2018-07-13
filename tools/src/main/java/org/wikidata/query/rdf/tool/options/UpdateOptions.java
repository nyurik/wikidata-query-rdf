package org.wikidata.query.rdf.tool.options;

import static org.wikidata.query.rdf.tool.options.OptionsUtils.splitByComma;
import static org.wikidata.query.rdf.tool.wikibase.WikibaseRepository.INPUT_DATE_FORMATTER;
import static org.wikidata.query.rdf.tool.wikibase.WikibaseRepository.OUTPUT_DATE_FORMATTER;
import static org.wikidata.query.rdf.tool.wikibase.WikibaseRepository.Uris.DEFAULT_ENTITY_NAMESPACES;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.http.client.utils.URIBuilder;
import org.wikidata.query.rdf.tool.exception.FatalException;
import org.wikidata.query.rdf.tool.wikibase.WikibaseRepository;

import com.lexicalscope.jewel.cli.Option;

/**
 * CLI options for use with JewelCli.
 */
@SuppressWarnings("checkstyle:javadocmethod")
public interface UpdateOptions extends OptionsUtils.BasicOptions, OptionsUtils.MungerOptions, OptionsUtils.WikibaseOptions {

    @Option(shortName = "s", defaultToNull = true, description = "Start time in 2015-02-11T17:11:08Z or 20150211170100 format.")
    String start();

    @Option(shortName = "S", defaultValue = "https", description = "Wikidata url scheme")
    String wikibaseScheme();

    @Option(shortName = "W", defaultToNull = true, description = "Wikibase instance base URL")
    String wikibaseUrl();

    @Option(shortName = "I", longName = "init", description = "Initialize last update time to start time")
    boolean init();

    @Option(shortName = "u", description = "URL to post updates and queries.")
    String sparqlUrl();

    @Option(shortName = "d", defaultValue = "10", description = "Poll delay when no updates found")
    int pollDelay();

    @Option(shortName = "t", defaultValue = "10", description = "Thread count")
    int threadCount();

    @Option(shortName = "b", defaultValue = "100", description = "Number of recent changes fetched at a time.")
    int batchSize();

    @Option(shortName = "V", longName = "verify", description = "Verify updates (may have performance impact)")
    boolean verify();

    @Option(shortName = "T", defaultValue = "0",  longName = "tailPoller",
            description = "Use secondary poller with given gap (seconds) to catch up missed updates. Applies only to RecentChanges poller.")
    int tailPollerOffset();

    @Option(shortName = "K", defaultToNull = true, longName = "kafka", description = "If set, use Kafka polling with the argument as the broker server")
    String kafkaBroker();

    @Option(shortName = "C", defaultToNull = true, longName = "consumer", description = "Set consumer ID for Kafka poller")
    String consumerId();

    @Option(shortName = "c", defaultToNull = true, longName = "clusters", description = "Kafka cluster prefixes (e.g. eqiad, codfw), comma or space separated")
    List<String> clusters();

    @Option(defaultToNull = true, description = "If specified must be <id> or list of <id>, comma or space separated.")
    List<String> ids();

    @Option(defaultToNull = true, description = "If specified must be <start>-<end>. Ids are iterated instead of recent "
            + "changes. Start and end are inclusive.")
    String idrange();

    @Option(defaultToNull = true, description = "If specified must be numerical indexes of Item and Property namespaces"
            + " that defined in Wikibase repository, comma separated.")
    String entityNamespaces();

    @Option(description = "Run Updater in test mode - only report updates but do not record")
    boolean testMode();

    @Option(description = "Load Wikibase constraints data")
    boolean constraints();

    @Option(description = "Reset Kafka offsets")
    boolean resetKafka();

    default long[] longEntityNamespaces() {
        if (entityNamespaces() == null) return DEFAULT_ENTITY_NAMESPACES;
        return splitByComma(Arrays.asList(entityNamespaces())).stream().
                mapToLong(option -> Long.parseLong(option))
                .toArray();
    }

    /**
     * Produce base Wikibase URL from options.
     */
    default URI getWikibaseUrl() {
        if (wikibaseUrl() != null) {
            try {
                return new URI(wikibaseUrl());
            } catch (URISyntaxException e) {
                throw new FatalException("Unable to build Wikibase url", e);
            }
        }
        URIBuilder baseUrl = new URIBuilder();
        baseUrl.setHost(wikibaseHost());
        baseUrl.setScheme(wikibaseScheme());
        try {
            return baseUrl.build();
        } catch (URISyntaxException e) {
            throw new FatalException("Unable to build Wikibase url", e);
        }
    }

    /**
     * Create the sparql URI from the given configuration.
     *
     * @return a newly created sparql URI
     */
    default URI sparqlUri() {
        URI sparqlUri;
        try {
            sparqlUri = new URI(sparqlUrl());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid url:  " + sparqlUrl(), e);
        }
        return sparqlUri;
    }

    @Nullable
    default Instant startInstant() {
        if (start() == null) return null;
        return parseDate(start());
    }

    @Nullable
    default String[] parsedIds() {
        List<String> split = splitByComma(ids());
        if (split == null) return null;
        return split.toArray(new String[split.size()]);
    }

    default boolean ignoreStoredOffsets() {
        // If we have explicit start time, we ignore kafka offsets
        return start() != null || resetKafka();
    }

    default WikibaseRepository.Uris uris() {
        return new WikibaseRepository.Uris(getWikibaseUrl(), longEntityNamespaces());
    }

    default List<String> clusterNames() {
        return splitByComma(clusters());
    }

    /**
     * Parse a string to a date, trying output format or the input format.
     *
     * @throws IllegalArgumentException if the date cannot be parsed with either format.
     */
    static Instant parseDate(String dateStr) {
        try {
            return OUTPUT_DATE_FORMATTER.parse(dateStr, Instant::from);
        } catch (DateTimeParseException e) {
            try {
                return INPUT_DATE_FORMATTER.parse(dateStr, Instant::from);
            } catch (DateTimeParseException e2) {
                throw  new IllegalArgumentException("Invalid date: " + dateStr, e2);
            }
        }
    }
}
