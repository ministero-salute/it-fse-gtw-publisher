package it.finanze.sanita.fse2.ms.gtwpublisher.config.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.DefaultErrorHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaConsumerCFG {

    /**
     * Kafka consumer properties.
     */
    @Autowired
    private KafkaConsumerPropertiesCFG kafkaConsumerPropCFG;

    @Autowired
    private KafkaPropertiesCFG kafkaProps;

    protected void addCommonsProperties(Map<String, Object> props) {
        
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaConsumerPropCFG.getConsumerKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConsumerPropCFG.getConsumerValueDeserializer());
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, kafkaConsumerPropCFG.getIsolationLevel());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaConsumerPropCFG.getAutoCommit());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerPropCFG.getAutoOffsetReset());

        // SSL
        if (kafkaConsumerPropCFG.isEnableSsl()) {
            props.put("security.protocol", kafkaProps.getProtocol());
            props.put("sasl.mechanism", kafkaProps.getMechanism());
            props.put("sasl.jaas.config", kafkaProps.getConfigJaas());
            props.put("ssl.truststore.location", kafkaProps.getTrustoreLocation());
            props.put("ssl.truststore.password", String.valueOf(kafkaProps.getTrustorePassword()));
        }
    }

    protected void setClassification(final DefaultErrorHandler sceh) {
		List<Class<? extends Exception>> out = getExceptionsConfig();

		for (Class<? extends Exception> ex : out) {
			log.warn("Found a non retryable Exception: {}", ex.getCanonicalName());
			sceh.addNotRetryableExceptions(ex);
		}
		
	}

	/**
	 * @return	exceptions list
	 */
	@SuppressWarnings("unchecked")
	protected List<Class<? extends Exception>> getExceptionsConfig() {
		List<Class<? extends Exception>> out = new ArrayList<>();
		String temp = null;
		try {
			for (String excs : kafkaConsumerPropCFG.getDeadLetterExceptions()) {
				temp = excs;
				Class<? extends Exception> s = (Class<? extends Exception>) Class.forName(excs, false, Thread.currentThread().getContextClassLoader());
				out.add(s);
			}
		} catch (Exception e) {
			log.error("Error retrieving the exception with fully qualified name: <{}>", temp);
			log.error("Error : ", e);
		}
		
		return out;
	}

}
