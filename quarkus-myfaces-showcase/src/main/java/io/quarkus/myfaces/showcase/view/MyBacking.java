/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quarkus.myfaces.showcase.view;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class MyBacking {
    
    @Inject private CarService carService;
    
    private MyCollection<Car> cars;
    
    @PostConstruct
    public void init()
    {
        cars = new MyCollection<>();
        for (Car car : carService.createCars(100))
        {
            cars.add(car);
        }
    }

    public MyCollection<Car> getCars() {
        return cars;
    }

    public void setCars(MyCollection<Car> cars) {
        this.cars = cars;
    }
    
    
}
