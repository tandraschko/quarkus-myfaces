package io.quarkus.myfaces.showcase.view;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ApplicationScoped
public class MethodHandleELResolverBean implements Serializable, PhaseListener {

    @Inject
    private CarService service;
    
    private List<Car> cars;
    
    private int test;
    private String test2;
    
    private long start;

    @PostConstruct
    public void init() {
        cars = service.createCars(20000);
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }
    
    public int getTest() {
        return test;
    }

    public void setTest(int test) {
        this.test = test;
    }

    public String getTest2() {
        return test2;
    }

    public void setTest2(String test2) {
        this.test2 = test2;
    }
 
    @Override
    public void afterPhase(PhaseEvent event) {
        System.err.println((System.currentTimeMillis() - start) + "ms");
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        start = System.currentTimeMillis();
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }
}
