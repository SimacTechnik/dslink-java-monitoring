package cz.simac.monitoring;

import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.DSLinkHandler;
import org.dsa.iot.dslink.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringDSLink extends DSLinkHandler {
    public static final Logger LOGGER = LoggerFactory.getLogger(MonitoringDSLink.class);

    private DSLink link;

    private Node superRoot;

    public MonitoringDSLink() {
        super();
    }

    @Override
    public boolean isResponder() {
        return true;
    }

    @Override
    public void onResponderConnected(final DSLink link) {
        LOGGER.info("Connected");
        this.link = link;
    }
    @Override
    public void onResponderInitialized(final DSLink link) {
        LOGGER.info("Initialized");
        superRoot = link.getNodeManager().getSuperRoot();

        Node processes = superRoot.createChild(MonitoringConstants.PROCESSES, true)
                .setDisplayName(MonitoringConstants.PROCESSES)
                .setSerializable(true)
                .build();

        Node ports = superRoot.createChild(MonitoringConstants.PORTS, true)
                .setDisplayName(MonitoringConstants.PORTS)
                .setSerializable(true)
                .build();

        new Thread(new ProcessMonitor(processes, 1000)).start();
    }

    @Override
    public void onResponderDisconnected(DSLink link) {
        LOGGER.info("Disconnected");
    }
}
