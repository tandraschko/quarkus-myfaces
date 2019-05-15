package io.quarkus.myfaces.showcase.view;

import java.util.Comparator;
import org.primefaces.model.SortOrder;

public class LazySorter implements Comparator<Car> {
 
    private String sortField;
    private SortOrder sortOrder;
     
    public LazySorter(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }
 
    @Override
    public int compare(Car car1, Car car2) {
        try {
            Object value1 = Car.class.getField(this.sortField).get(car1);
            Object value2 = Car.class.getField(this.sortField).get(car2);
 
            int value = ((Comparable) value1).compareTo(value2);
             
            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
        } 
        catch(Exception e) {
            throw new RuntimeException();
        }
    }
}
