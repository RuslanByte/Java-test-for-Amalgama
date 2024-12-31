package org.example;

public class Employee {
    private int id;
    private boolean busy;
    private ProductionCenter currentCenter;

    public Employee(int id) {
        this.id = id;
        this.busy = false;
        this.currentCenter = null;
    }

    public int getId() {
        return id;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public void assignTo(ProductionCenter center) {
        if (currentCenter != null) {
            currentCenter.removeWorker(this);
        }
        currentCenter = center;
        center.addWorker(this);
        setBusy(true);
    }

    public void release() {
        this.busy = false;
        this.currentCenter = null;
    }


}
