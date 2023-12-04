package jlox;

import java.util.HashMap;
import java.util.Map;

class Environment {
    private final Map<String, Object> values = new HashMap<String, Object>();

    void define(String name, Object initializer){
        values.put(name, initializer);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        throw new RuntimeError(name, String.format("Undefined variable %s.", name.lexeme));

        
     }
}
