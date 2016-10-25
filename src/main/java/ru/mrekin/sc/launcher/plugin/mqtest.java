package ru.mrekin.sc.launcher.plugin;

import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.ConnectionFactory;
import com.sun.messaging.Queue;
import com.sun.messaging.Topic;

import javax.jms.*;


public class mqtest {

    /**
     * Main method.
     *
     * @param args	not used
     *
     */


        public static void main( String[] args )
        {
            try{
            ConnectionFactory connFactory = new ConnectionFactory();
            connFactory.setProperty(ConnectionConfiguration.imqAddressList, "192.168.164.57:7676");

            Topic topic = new Topic("scl");
            Queue queue = new Queue("test");



                Connection connection = connFactory.createConnection();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                connection.start();
                MessageProducer producer = session.createProducer(queue);
                MessageConsumer consumer = session.createConsumer(queue);

                Message message = session.createTextMessage("this is my test message111");

                producer.send(message);
                Message msg = consumer.receive();
                msg.getJMSMessageID();
                producer.close();
                connection.close();
                session.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

}
