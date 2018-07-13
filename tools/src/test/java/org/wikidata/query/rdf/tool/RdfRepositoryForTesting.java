package org.wikidata.query.rdf.tool;

import static org.wikidata.query.rdf.tool.HttpClientUtils.buildHttpClient;
import static org.wikidata.query.rdf.tool.HttpClientUtils.buildHttpClientRetryer;
import static org.wikidata.query.rdf.tool.HttpClientUtils.getHttpProxyHost;
import static org.wikidata.query.rdf.tool.HttpClientUtils.getHttpProxyPort;
import static org.wikidata.query.rdf.tool.Update.getRdfClientTimeout;

import java.net.URI;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openrdf.query.TupleQueryResult;
import org.wikidata.query.rdf.common.uri.WikibaseUris;
import org.wikidata.query.rdf.tool.rdf.RdfRepository;
import org.wikidata.query.rdf.tool.rdf.client.RdfClient;

import com.google.common.annotations.VisibleForTesting;

/**
 * RdfRepository extension used for testing. We don't want to anyone to
 * accidentally use clear() so we don't put it in the repository.
 */
public class RdfRepositoryForTesting extends RdfRepository implements TestRule {

    /**
     * The namespace of the local RDF repository, e.g. "kb" or "wdq".
     */
    private final String namespace;

    public RdfRepositoryForTesting(String namespace) {
        super(
                WikibaseUris.WIKIDATA,
                new RdfClient(
                        buildHttpClient(getHttpProxyHost(), getHttpProxyPort()), url("/namespace/" + namespace + "/sparql"),
                        buildHttpClientRetryer(),
                        getRdfClientTimeout()
                )
        );
        this.namespace = namespace;
    }

    /**
     * Take a relative path and create a URL with the full path to Blazegraph on
     * localhost.
     */
    private static URI url(String path) {
        return URI.create("http://localhost:9999/bigdata" + path);
    }

    /**
     * Clear's the whole repository.
     */
    public void clear() {
        rdfClient.update("CLEAR ALL");
    }

    /**
     * Loads a uri into this rdf repository. Uses Blazegraph's update with
     * uri's feature.
     */
    public int loadUrl(String uri) {
        return rdfClient.loadUrl(uri);
    }

    /**
     * Updates the repository.
     */
    public int update(String query) {
        return rdfClient.update(query);
    }

    /**
     * Delete the given namespace from the test Blazegraph server.
     */
    protected void deleteNamespace() {
//            HttpDelete delete = new HttpDelete(url("/namespace/" + namespace));
//            submit(delete, 200);
    }

    /**
     * Create the given namespace in the test Blazegraph server.
     */
    protected void createNamespace() {
//            HttpPost post = new HttpPost(url("/namespace"));
//            post.setHeader(new BasicHeader("Content-Type", "application/xml; charset=UTF-8"));
//            StringBuilder body = new StringBuilder();
//            body.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
//            body.append("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">");
//            body.append("<properties>");
//            body.append("<entry key=\"com.bigdata.rdf.sail.namespace\">").append(namespace).append("</entry>");
//            body.append("<entry key=\"com.bigdata.rdf.store.AbstractTripleStore.textIndex\">false</entry>");
//            body.append("<entry key=\"com.bigdata.rdf.sail.truthMaintenance\">true</entry>");
//            body.append("<entry key=\"com.bigdata.rdf.store.AbstractTripleStore.quads\">false</entry>");
//            body.append("<entry key=\"com.bigdata.rdf.store.AbstractTripleStore.statementIdentifiers\">false</entry>");
//            body.append("<entry key=\"com.bigdata.rdf.store.AbstractTripleStore.axiomsClass\">com.bigdata.rdf.axioms.NoAxioms</entry>");
//            body.append("</properties>");
//            post.setEntity(new StringEntity(body.toString(), "UTF-8"));
//            submit(post, 201);
    }

    /** {@inheritDoc} */
    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                } finally {
                    after();
                }
            }
        };
    }

    /**
     * Clear repository before tests.
     */
    public void before() {
        clear();
    }

    /**
     * Clear and close repository after test.
     */
    public void after() throws Exception {
        clear();
        rdfClient.httpClient.stop();
    }

    /**
     * @deprecated tests should be refactored to test RdfClient directly
     */
    @Deprecated
    @VisibleForTesting
    public TupleQueryResult query(String sparql) {
        return rdfClient.query(sparql);
    }

    /**
     * @deprecated tests should be refactored to test RdfClient directly
     */
    @Deprecated
    @VisibleForTesting
    public boolean ask(String sparql) {
        return rdfClient.ask(sparql);
    }
}
