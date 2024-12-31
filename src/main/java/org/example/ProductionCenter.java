package org.example;

import java.util.*;

public class ProductionCenter {
    private String id;
    private String name;
    private int maxEmployees;
    private double processingTime;
    private Queue<Detail> buffer;
    private List<Employee> workers;
    private List<ProductionCenter> nextCenters;

    public ProductionCenter(String id, String name, double processingTime, int maxEmployees) {
        this.id = id;
        this.name = name;
        this.processingTime = processingTime;
        this.maxEmployees = maxEmployees;
        this.buffer = new LinkedList<>();
        this.workers = new ArrayList<>();
        this.nextCenters = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getBufferCount() {
        return buffer.size();
    }

    public int getMaxEmployees() {
        return maxEmployees;
    }

    public int getCurrentWorkers() {
        return workers.size();
    }

    public void addWorker(Employee employee) {
        if (workers.size() < maxEmployees) {
            workers.add(employee);
        }
    }

    public void removeWorker(Employee employee) {
        workers.remove(employee);
        employee.release();
    }

    public void addNextCenter(ProductionCenter center) {
        nextCenters.add(center);
    }

    public void addDetail(Detail detail) {
        buffer.add(detail);
    }

    public void show(){
        for(ProductionCenter n : nextCenters){
            System.out.println(n.name);
        }
    }

    public void processDetails(double time) {
        List<Detail> processedDetails = new ArrayList<>();
        for (int i = 0; i < workers.size(); i++) {
            if (!buffer.isEmpty()) {
                processedDetails.add(buffer.poll());
            }
        }
        for (Detail detail : processedDetails) {
            for (ProductionCenter nextCenter : nextCenters) {
                nextCenter.addDetail(detail);
            }
        }
    }
}
