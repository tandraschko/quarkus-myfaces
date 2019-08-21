
package io.quarkus.myfaces.showcase.view;

import java.util.List;
import javax.faces.model.FacesDataModel;
import javax.faces.model.ListDataModel;

@FacesDataModel(forClass = MyCollection.class)
public class MyCollectionModel<E> extends ListDataModel<E> {
    
    public MyCollectionModel()
    {
        super();
        System.err.println("+++++++++++++++");
    }

    public MyCollectionModel(List<E> list)
    {
        super(list);
        System.err.println("+++++++++++++++2");
    }
}
