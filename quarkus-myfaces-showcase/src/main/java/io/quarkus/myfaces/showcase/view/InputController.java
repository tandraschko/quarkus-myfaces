package io.quarkus.myfaces.showcase.view;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.annotation.ManagedProperty;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@SessionScoped
public class InputController implements Serializable{
    
    @Inject
    @ManagedProperty(value = "#{carService}")
    private CarService carService;
    
    private String val;

    public String getVal() {
        System.err.println("carService: " + carService);
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
    
}
