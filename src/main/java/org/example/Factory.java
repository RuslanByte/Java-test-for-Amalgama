package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Factory {
    private List<ProductionCenter> centers;
    private List<Employee> employees;
    private Queue<Detail> initialDetails;

    public Factory(List<ProductionCenter> centers, int workersCount, int detailsCount) {
        this.centers = centers;
        this.employees = new ArrayList<>();
        this.initialDetails = new LinkedList<>();

        for (int i = 0; i < workersCount; i++) {
            employees.add(new Employee(i + 1));
        }

        for (int i = 0; i < detailsCount; i++) {
            initialDetails.add(new Detail());
        }

        if (!centers.isEmpty()) {
            ProductionCenter firstCenter = centers.get(0);
            while (!initialDetails.isEmpty()) {
                firstCenter.addDetail(initialDetails.poll());
            }
        }
    }

    public void show(){
        for(ProductionCenter e : centers){
            System.out.println(e.getName());

        }
        System.out.println("\n");
        for(ProductionCenter e : centers){
            e.show();
        }
    }

    public void simulate(double totalTime) {
        float currentTime = 0;
        try (FileWriter writer = new FileWriter("simulation_output.csv")) {
            writer.write("Time; ProductionCenter; WorkersCount; BufferCount\n");
            show();
            while (currentTime < totalTime) {
                for (ProductionCenter center : centers) {
                    center.processDetails(1.0);
                    writer.write(String.format("%.1f; %s; %d; %d\n",
                            currentTime, center.getName(), center.getCurrentWorkers(), center.getBufferCount()));
                }
                redistributeEmployees();

                currentTime += 1.0;
            }
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void redistributeEmployees() {
        centers.sort((c1, c2) -> Integer.compare(c2.getBufferCount(), c1.getBufferCount()));

        for (ProductionCenter center : centers) {
            int requiredWorkers = Math.min(center.getMaxEmployees(), center.getBufferCount());
            int currentWorkers = center.getCurrentWorkers();

            if (currentWorkers < requiredWorkers) {
                int needed = requiredWorkers - currentWorkers;
                for (Employee employee : employees) {
                    if (!employee.isBusy()) {
                        employee.assignTo(center);
                        needed--;
                        if (needed == 0) break;
                    }
                }
            }
        }
    }
}
