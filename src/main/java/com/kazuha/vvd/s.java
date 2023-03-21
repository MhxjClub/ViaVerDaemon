package com.kazuha.vvd;

public class s {
    public static void main(String[] args){
        for(int i = 1000; i>=0;i--){
            if(sb(i)=="6")continue;
            System.out.println("{\""+i+"\": \""+sb(i)+"\"},");
        }
    }
    public static String sb(int e){
        switch (e){
            case 4:
                return "1.7.5";
            case 5:
                return "1.7.10";
            case 47:
                return "1.8.8";
            case 107:
                return "1.9";
            case 108:
                return "1.9.1";
            case 109:
                return "1.9.2";
            case 110:
                return "1.9.4";
            case 210:
                return "1.10.2";
            case 315:
                return "1.11";
            case 316:
                return "1.11.2";
            case 335:
                return "1.12";
            case 338:
                return "1.12.1";
            case 340:
                return "1.12.2";
            case 393:
                return "1.13";
            case 401:
                return "1.13.1";
            case 404:
                return "1.13.2";
            case 477:
                return "1.14";
            case 480:
                return "1.14.1";
            case 485:
                return "1.14.2";
            case 490:
                return "1.14.3";
            case 498:
                return "1.14.4";
            case 573:
                return "1.15";
            case 575:
                return "1.15.1";
            case 578:
                return "1.15.2";
            case 735:
                return "1.16";
            case 736:
                return "1.16.1";
            case 751:
                return "1.16.2";
            case 753:
                return "1.16.3";
            case 754:
                return "1.16.5";
            case 755:
                return "1.17";
            case 756:
                return "1.17.1";
            case 757:
                return "1.18.1";
            case 758:
                return "1.18.2";
            case 759:
                return "1.19";
        }
        return "6";
    }
}
