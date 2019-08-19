/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quarkus.myfaces.showcase.view;

import java.io.Serializable;
import java.util.Set;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;
import org.apache.myfaces.cdi.util.AnyLiteral;

/**
 *
 * @author andraschko
 */
@Named
@SessionScoped
public class InputController implements Serializable{
    private String val;

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        BeanManager bm = CDI.current().getBeanManager();
        Set<Bean<?>> beans = bm.getBeans(Object.class, new AnyLiteral());
        for (Bean<?> bean : beans) {
            System.err.println("_--------------------------");
            System.out.println(bean);
            System.out.println(bean.getBeanClass());
            System.out.println(bean.getBeanClass().getName());
            System.out.println(bean.getQualifiers());
        }
        
        this.val = val;
    }
    
    
}
