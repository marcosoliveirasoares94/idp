package br.gov.sp.prodesp.idp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.jboss.logging.Logger;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.theme.Theme;
import org.keycloak.theme.ThemeSelectorProvider;

import java.util.Properties;
import java.util.UUID;

public class IdpSPProvider implements ThemeSelectorProvider, EventListenerProvider {

    private static final Logger LOG = Logger.getLogger( IdpSPProvider.class );

    KafkaProducer<String, String> producer;

    public IdpSPProvider( KeycloakSession session ) {

        String bootStrap = System.getenv( "KAFKA_BOOTSTRAP_SERVERS" );

        Properties properties = new Properties( );
        properties.put( "bootstrap.servers", bootStrap );
        properties.put( "client.id", String.valueOf( session.hashCode( ) ) );
        properties.put( "key.serializer", "org.apache.kafka.common.serialization.StringSerializer" );
        properties.put( "value.serializer", "org.apache.kafka.common.serialization.StringSerializer" );
        properties.put( "retries", 5 );
        properties.put( "retry.backoff.ms", 150 );

        LOG.infov( "BOOTSTRAP: {0}", bootStrap );

        // Vixe: sem a linha abaixo ele não consegue encontrar 'org.apache.kafka.common.serialization.StringSerializer'
        Thread.currentThread().setContextClassLoader(null);
        producer = new KafkaProducer<>( properties );
    }

    @Override
    public String getThemeName( Theme.Type type ) {
        return "idpsp-theme";
    }

    @Override
    public void close( ) {
    }

    @Override
    public void onEvent( Event event ) {
        String messageStr = toJson( event );
        send( "IDPSP_EVENT", messageStr  );
    }

    @Override
    public void onEvent( AdminEvent adminEvent, boolean b ) {
        String messageStr = toJson( adminEvent );
        send( "IDPSP_ADMIN_EVENT", messageStr  );
    }

    private void send( String topic, String messageStr ) {
        long startTime = System.currentTimeMillis( );
        String messageKey = UUID.randomUUID( ).toString( );

        LOG.infov( "Topic {0} [{1}]: ({2}) ({3})", startTime, topic, messageKey, messageStr );
        producer.send(
            new ProducerRecord<>( topic, messageKey, messageStr  ),
            new TimedCallBack( startTime, messageKey, messageStr )
        );
    }
    private String toJson( Object event ) {
        ObjectMapper mapper = new ObjectMapper( );
        try {
            return mapper.writeValueAsString( event );
        } catch ( JsonProcessingException e ) {
            throw new RuntimeException( e );
        }
    }
}

class TimedCallBack implements Callback {

    private static final Logger LOG = Logger.getLogger( TimedCallBack.class );

    private final long startTime;
    private final String key;
    private final String message;

    public TimedCallBack( long startTime, String key, String message ) {
        this.key = key;
        this.message = message;
        this.startTime = startTime;
    }

    /**
     * onCompletion method will be called when the record sent to the Kafka Server has been acknowledged.
     *
     * @param metadata  The metadata contains the partition and offset of the record. Null if an error occurred.
     * @param exception The exception thrown during processing of this record. Null if no error occurred.
     */
    public void onCompletion( RecordMetadata metadata, Exception exception ) {
        long elapsedTime = System.currentTimeMillis( ) - startTime;
        if ( metadata != null ) {
            LOG.infov("message({0}, {1}) sent to partition({2}) offset({3}) {4}ms", key, message,
                metadata.partition( ), metadata.offset( ), elapsedTime );
        } else {
            LOG.errorv("message({0}, {1}) {2}", key, message, exception );
            // TODO aqui qdo der erro tem que implementr uma fila em memoria de reenvio.
            //      o client esta configurado com retry, mas caso o kafka esteja fora do ar,
            //      precisa tentar reenviar depois
        }
    }
}
