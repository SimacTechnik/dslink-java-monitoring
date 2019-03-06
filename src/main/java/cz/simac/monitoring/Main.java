package cz.simac.monitoring;

import org.dsa.iot.dslink.DSLinkFactory;

public class Main {
    public static void main(String args[]) {
        DSLinkFactory.start(new String[] {"-d", "../dslink.json", "-b", "http://127.0.0.1:37829/conn"}, new MonitoringDSLink());
    }
}
