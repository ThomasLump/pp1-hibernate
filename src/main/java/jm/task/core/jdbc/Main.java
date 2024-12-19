package jm.task.core.jdbc;

import jm.task.core.jdbc.service.UserService;
import jm.task.core.jdbc.service.UserServiceImpl;
import jm.task.core.jdbc.util.Util;

public class Main {
    public static void main(String[] args) {
        // реализуйте алгоритм здесь
        Util ut = new Util();
        Util.setup();
        UserService service = new UserServiceImpl();
        //service.createUsersTable();
        service.dropUsersTable();
        //service.saveUser("ALEXEY","FEFELOV",(byte) 56);



    }
}
