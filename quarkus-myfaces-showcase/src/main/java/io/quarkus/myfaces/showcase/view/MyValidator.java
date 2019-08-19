/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quarkus.myfaces.showcase.view;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author andraschko
 */
@FacesValidator(value = "myVal", managed = true)
public class MyValidator implements Validator<String>{
    
    public MyValidator()
    {
        System.err.println("###############");
    }
    
    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        System.err.println("###############validate");
    }
    
}
