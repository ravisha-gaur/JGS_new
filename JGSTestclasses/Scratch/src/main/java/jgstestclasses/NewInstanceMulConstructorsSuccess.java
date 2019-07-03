package jgstestclasses;

import de.unifreiburg.cs.proglang.jgs.support.*;

public class NewInstanceMulConstructorsSuccess {

    @Sec("?")
    static int a;
    @Sec("HIGH")
    static double b;


    @Constraints({"@0 <= ?", "@1 <= HIGH"})
    @Effects({"HIGH", "?", "LOW"})
    NewInstanceMulConstructorsSuccess(int a, double b){
        this.a = a;
        this.b = b;
    }

    NewInstanceMulConstructorsSuccess(){

    }

    @Constraints("LOW <= @0")
    @Effects({"LOW", "?"})
    public static void main(String[] args) {
        int x = Casts.cast("HIGH ~> ?", 5);
        double y = Casts.cast("? ~> HIGH", 7.0);

        NewInstanceMulConstructorsSuccess newInstance = new NewInstanceMulConstructorsSuccess(x, y);
        newInstance = Casts.cast("LOW ~> ?", newInstance);
        IOUtils.printSecret(newInstance);

        NewInstanceMulConstructorsSuccess newInstance1 = new NewInstanceMulConstructorsSuccess();
        newInstance1 = Casts.cast("? ~> HIGH", newInstance1);
        IOUtils.printSecret(newInstance1);


        NewInstanceMulConstructorsSuccess newInstance2 = new NewInstanceMulConstructorsSuccess(10, 9.3);
        newInstance2 = Casts.cast("LOW ~> ?", newInstance2);
        IOUtils.printSecret(newInstance2);

    }


}
