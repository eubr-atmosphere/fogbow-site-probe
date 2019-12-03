package cloud.fogbow.probes.core.models;

import cloud.fogbow.probes.core.services.DataProviderService;
import eu.atmosphere.tmaf.monitor.client.BackgroundClient;
import eu.atmosphere.tmaf.monitor.message.Data;
import eu.atmosphere.tmaf.monitor.message.Message;
import eu.atmosphere.tmaf.monitor.message.Observation;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

public abstract class Probe implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Probe.class);

    protected BackgroundClient client;
    protected Message message;
    protected Properties properties;
    protected Timestamp lastTimestampAwake;
    protected Integer resourceId;
    protected Integer probeId;
    protected boolean firstTimeAwake;
    protected Integer descriptionId;

    @Autowired
    protected DataProviderService providerService;

    private static int messageId = 0;

    protected void setup() {
        this.client = new BackgroundClient("https://158.42.104.30:32025/monitor");
        client.authenticate(probeId, "pass".getBytes());
        boolean startFlag = client.start();

        if(!startFlag) {
            System.out.println("failed on starting");
        }
    }

    protected void sendMessage(List<List<Pair<Number, Timestamp>>> dataValues) {
        createMessage();

        this.message.setResourceId(resourceId);
        this.message.setMessageId(messageId++);

        List<Observation> observations = new ArrayList<>();


        for(List<Pair<Number, Timestamp>> obs: dataValues) {
            for(Pair<Number, Timestamp> ob : obs) {
                observations.add(new Observation((ob.getValue().getTime())/1000L, ob.getKey().doubleValue()));
            }

            this.message.addData(new Data(
                    Data.Type.MEASUREMENT,
                    probeId,
                    observations
                )
            );

            observations.clear();
        }

        this.message.setSentTime((Calendar.getInstance().getTimeInMillis())/1000L);
        client.send(message);
    }

    protected boolean hasData(List<List<Pair<Number, Timestamp>>> dataSet) {
        boolean hasData = false;

        for(int i = 0; i < 2; i++) {
            List<Pair<Number, Timestamp>> current = dataSet.get(i);
            for(Pair<Number, Timestamp> pair : current) {
                if(pair.getKey().doubleValue() != 0) {
                    hasData = true;
                }
            }
        }

        return hasData;
    }

    private void createMessage() {
        this.message = this.client.createMessage();
    }

    protected void sleep(int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
