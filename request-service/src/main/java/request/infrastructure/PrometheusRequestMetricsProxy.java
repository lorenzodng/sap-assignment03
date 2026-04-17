package request.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import request.application.RequestMetrics;

//raccoglie le metriche di prestazione
@Adapter
public class PrometheusRequestMetricsProxy implements RequestMetrics {

    private final Histogram requestLatency; //consente di suddividere le misurazioni in gruppi
    private final HTTPServer server;

    public PrometheusRequestMetricsProxy(int port) throws Exception {
        requestLatency = Histogram.builder()
                .name("request_orchestration_duration_seconds")
                .help("Time taken to orchestrate a shipment request")
                .classicUpperBounds(0.1, 0.3, 0.5, 1.0, 2.0) //gruppi di misurazioni - ogni qualvolta si verifica un valore <= ad uno di questi, viene incrementato il contatore di quel gruppo
                .register(); //crea la metrica di latenza
        server = HTTPServer.builder().port(port).buildAndStart(); //espone le metriche su una porta dedicata
    }

    //incrementa la metrica
    @Override
    public void observeLatency(double seconds) {
        requestLatency.observe(seconds);
    }

    //ferma il server e libera la porta
    public void stop() {
        server.close();
    }
}