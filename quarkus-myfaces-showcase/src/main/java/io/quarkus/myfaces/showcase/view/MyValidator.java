package io.quarkus.myfaces.showcase.view;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

@FacesValidator(value = "myVal", managed = true)
public class MyValidator implements Validator<String>{
    
    @Inject private CarService carService;
    
    public MyValidator()
    {
    }
    
    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        System.err.println("############### validate: " + value + " (carService=" + carService + ")");
    }
    
}
