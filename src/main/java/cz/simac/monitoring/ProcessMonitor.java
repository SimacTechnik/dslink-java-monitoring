package cz.simac.monitoring;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;

import java.util.Map;
import java.util.stream.Collectors;

public class ProcessMonitor implements Runnable{

    private Node rootNode;

    private Map<Long, ProcessHandle> currProcesses = null;

    private int interval;

    public ProcessMonitor(Node node, int interval) {
        this.rootNode = node;
        this.interval = interval;
    }

    @Override
    public void run() {
           while(true) {
                handleProcesses(ProcessHandle.allProcesses().collect(Collectors.toMap(ProcessHandle::pid, a -> a)));
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ignored) { }
           }
    }

    private void handleProcesses(Map<Long, ProcessHandle> processes) {
        for(ProcessHandle process : processes.values()) {
            updateOrCreateNode(process);
        }
        if(currProcesses != null) {
            for(Long pid : currProcesses.keySet()) {
                if(!processes.containsKey(pid)) {
                    rootNode.removeChild(Long.toString(pid), true);
                }
            }
        }
        currProcesses = processes;
    }

    private void updateOrCreateNode(ProcessHandle process) {
        if(!process.isAlive())
            return;
        Node child = rootNode.getChild(Long.toString(process.pid()), true);
        if(child == null) {
            child = rootNode.createChild(Long.toString(process.pid()), true)
                    .setDisplayName(String.format("%d: %s", process.pid(), !process.info().command().isPresent() ? "" : process.info().command().get()))
                    .setSerializable(false)
                    .build();
        }
        updateOrCreateValue(child, MonitoringConstants.PID, new Value(process.pid()), ValueType.NUMBER);
        updateOrCreateValue(child, MonitoringConstants.PPID, new Value(!process.parent().isPresent() ? 0 : process.parent().get().pid()), ValueType.NUMBER);
        updateOrCreateValue(child, MonitoringConstants.ALIVE, new Value(process.isAlive()), ValueType.BOOL);
        updateOrCreateValue(child, MonitoringConstants.USER, new Value(!process.info().user().isPresent() ? "" : process.info().user().get()), ValueType.STRING);
        updateOrCreateValue(child, MonitoringConstants.STRART, new Value(!process.info().startInstant().isPresent() ? 0 : process.info().startInstant().get().toEpochMilli()), ValueType.NUMBER);
        updateOrCreateValue(child, MonitoringConstants.COMMAND, new Value(!process.info().command().isPresent() ? "" : process.info().command().get()), ValueType.STRING);
        updateOrCreateValue(child, MonitoringConstants.ARGS, new Value(!process.info().arguments().isPresent() ? "" : String.join(" ",process.info().arguments().get())), ValueType.STRING);
    }

    private void updateOrCreateValue(Node parent, String name, Value value, ValueType valueType) {
        Node n = parent.getChild(name, true);
        if(n == null) {
            n = parent.createChild(name, true)
                    .setDisplayName(name)
                    .setSerializable(false)
                    .build();
        }
        n.setValue(value);
        n.setValueType(valueType);
    }
}
