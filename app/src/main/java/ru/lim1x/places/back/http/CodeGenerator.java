package ru.lim1x.places.back.http;

public  class CodeGenerator {
    static private int generateIntCode() {
        double rand = Math.random();
        long result = Math.round(rand * 1000000);
        return (int) result;
    }
    public static String generateCode(){
        int code = generateIntCode();
        String result = Integer.toString(code);
        if (result.length() < 6)
            result = "0"+result;
        return result;
    }
}
