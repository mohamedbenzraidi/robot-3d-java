package org.example.robot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.math.Vector3D;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class Robot {
    Vector3D position;
    float speed;
    Orientation orientation;

    public void move(Vector3D newPos){
//        this.setPosition(newPos);//not totally sure about this one;
    }

    public void stop(){
        //TODO: something to stop it;
    }
}
