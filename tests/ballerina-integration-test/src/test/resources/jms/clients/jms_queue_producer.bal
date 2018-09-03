import ballerina/jms;
import ballerina/io;

// Initialize a JMS connection with the provider
jms:Connection jmsConnection = new ({
        initialContextFactory: "bmbInitialContextFactory",
        providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5772'"
    });

// Initialize a JMS session on top of the created connection
jms:Session jmsSession = new (jmsConnection, {
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

endpoint jms:QueueSender queueSender {
    session: jmsSession,
    queueName: "MyQueue4"
};

public function main (string... args) {
    // Create a Text message.
    jms:Message m = check jmsSession.createTextMessage("Test Text");
    // Send the Ballerina message to the JMS provider.
    _ = queueSender -> send(m);

    io:println("Message successfully sent by QueueSender");
}
